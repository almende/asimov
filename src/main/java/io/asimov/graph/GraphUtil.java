package io.asimov.graph;

import io.asimov.model.process.Process;
import io.asimov.model.process.Task;
import io.asimov.xml.TSkeletonActivityType;
import io.asimov.xml.TSkeletonActivityType.RoleInvolved;
import io.asimov.xml.TSkeletonActivityType.UsedComponent;
import io.coala.log.LogUtil;
import io.coala.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.Traversal;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@link GraphUtil}
 * 
 * @date $Date: 2014-11-06 15:23:37 +0100 (do, 06 nov 2014) $
 * @version $Revision: 1113 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class GraphUtil implements Util
{

	private static final boolean logEnabled = false;
	
	/** */
	public static enum ConnectionTypes implements RelationshipType
	{
		/** */
		NEXT,

		/** */
		DO_NOT_USE,

		/** */
		HAS_RESOURCE,

		;
	}

	/** */
	private static final Logger LOG = LogUtil.getLogger(GraphUtil.class);


	private static final GraphUtil INSTANCE = new GraphUtil();

	/** */
	private transient File temp;

	/** */
	private transient GraphDatabaseService graphDb;

	/** */
	private transient int MAX_SEARCH_DEPTH = 15;

	// PATH Finding stuff...

	private transient Map<String, Node> termNodeMapping = new HashMap<String, Node>();

	private transient Map<String, Node> resourceNodeMapping = new HashMap<String, Node>();

	private transient Map<String, Integer> activityRetainCount = new HashMap<String, Integer>();

	private transient Map<String, Integer> resourceRetainCount = new HashMap<String, Integer>();

	@JsonIgnore
	public Map<String, List<Task>> allPossiblePathsOfTasks = new LinkedHashMap<String, List<Task>>();

	public TraversalDescription processPathTraverser = Traversal.description()
			.depthFirst()
			.relationships(ConnectionTypes.NEXT, Direction.OUTGOING);
	public TraversalDescription reverserdProcessPathTraverser = Traversal
			.description().depthFirst()
			.relationships(ConnectionTypes.NEXT, Direction.INCOMING);

	/** @throws IOException */
	private GraphUtil()
	{
		try
		{
			this.temp = File.createTempFile("pathFinder", null);
			this.temp.delete();
			this.temp.mkdir();
			this.graphDb = new GraphDatabaseFactory()
					.newEmbeddedDatabase(this.temp.getAbsolutePath());
			if (logEnabled) LOG.info("NEO4JDB directory location: "
					+ this.temp.getAbsolutePath());
		} catch (final IOException e)
		{
			LOG.error("Problem setting up NEO4J database in file system", e);
		}
	}

	public static synchronized GraphUtil getInstance()
	{
		return INSTANCE;
	}

	protected Set<Process> processes;

	private int pMaxSize;

	/**
	 * @param processTypes
	 */
	public void initializeProcessTypes(final Set<Process> processTypes)
	{
		this.processes = processTypes;
		this.pMaxSize = 1;
		for (Process p : processTypes)
		{
			this.pMaxSize = Math.max(p.toXML().getActivity().size() + 2,
					this.pMaxSize);
		}
	}

	private Node retainNodeForResource(String resourceSubType)
			throws ConcurrentModificationException
	{
		final Transaction tx = graphDb.beginTx();
		Node result;
		String resource = resourceSubType;
		boolean containsKey = false;
		for (String otherResource : resourceNodeMapping.keySet())
			if (otherResource.equals(resourceSubType))
			{
				resource = otherResource;
				containsKey = true;
			}
		Integer retainCount = resourceRetainCount.get(resource);
		if (retainCount == null)
			retainCount = new Integer(0);
		int iRetain = retainCount.intValue();
		iRetain++;
		if (containsKey)
		{
			if (retainCount == null || retainCount.intValue() > 0)
			{
				throw new ConcurrentModificationException(resource
						+ " is already retained, please release it first");
			} else
				result = resourceNodeMapping.remove(resource);
			resourceRetainCount.remove(resource);
			resourceRetainCount.put(resource, new Integer(iRetain));
			// LOG.trace("Re-using node:"+result.getId());
		} else
		{
			resourceRetainCount.put(resource, new Integer(iRetain));
			result = graphDb.createNode();
			result.setProperty("name", resourceSubType);
			// LOG.trace("Created node:"+result.getId());
		}
		tx.success();
		tx.finish();
		// LOG.trace("Retained node:"+result.getId());
		return result;
	}

	private void releaseNodeForResource(String resource, Node node)
			throws ConcurrentModificationException
	{
		String theResource = resource;
		boolean containsKey = false;
		for (String otherResource : resourceRetainCount.keySet())
			if (otherResource.equals(resource))
			{
				containsKey = true;
				theResource = otherResource;
			}

		if (containsKey)
		{
			resourceNodeMapping.put(theResource, node);
			// LOG.trace("Released node:"+node.getId());
			resourceRetainCount.put(
					theResource,
					new Integer(Integer.valueOf(resourceRetainCount.remove(
							theResource).intValue() - 1)));
		} else
			throw new ConcurrentModificationException(
					theResource
							+ " is not yet retained, please retain it first before releasing");
	}

	@JsonIgnore
	public Node getNodeValueForResource(String resource)
	{
		Node result = retainNodeForResource(resource);
		releaseNodeForResource(resource, result);
		return result;
	}

	private static Map<String, String> activityIdToName = null;

	private synchronized static Map<String, String> getActivtyIdToNameMap()
	{
		if (activityIdToName == null)
			activityIdToName = Collections
					.synchronizedMap(new HashMap<String, String>());
		return activityIdToName;
	}

	public String getNameForActivityId(String id)
	{
		String result = null;
		Map<String, String> idToNameMap = getActivtyIdToNameMap();
		synchronized (idToNameMap)
		{
			result = idToNameMap.get(id);
		}
		return result;
	}

	/*
	 * WARNING: Actually removes node from the mapping, must be relased again
	 * */
	private Node retainNodeForActivity(final String processID,
			TSkeletonActivityType bodyActivity)
			throws ConcurrentModificationException
	{
		Node result;
		String activity = bodyActivity.getId().toString();
		boolean containsKey = false;
		for (String otherActivity : termNodeMapping.keySet())
			if (otherActivity.equals(bodyActivity.getId()))
			{
				activity = otherActivity;
				containsKey = true;
			}
		Integer retainCount = activityRetainCount.get(activity);
		if (retainCount == null)
			retainCount = new Integer(0);
		int iRetain = retainCount.intValue();
		iRetain++;
		if (containsKey)
		{
			if (retainCount == null || retainCount.intValue() > 0)
			{
				throw new ConcurrentModificationException(activity
						+ " is already retained, please release it first");
			} else
				result = termNodeMapping.remove(activity);
			activityRetainCount.remove(activity);
			activityRetainCount.put(activity, new Integer(iRetain));
			// LOG.trace("Re-using node:"+result.getId());
		} else
		{
			try
			{
				graphDb.getNodeById(Long.valueOf(bodyActivity.getId()));
				LOG.warn("BARFU!");
				return null;
			} catch (Exception e)
			{

				activityRetainCount.put(activity, new Integer(iRetain));
				final Transaction tx = graphDb.beginTx();
				result = graphDb.createNode();
				result.setProperty("processName", processID);
				result.setProperty("activityName", bodyActivity.getId());
				tx.success();
				tx.finish();
				Map<String, String> idToNameMap = getActivtyIdToNameMap();
				synchronized (idToNameMap)
				{
					idToNameMap.put(bodyActivity.getId(),
							bodyActivity.getName());
				}
				// LOG.trace("Created node:"+result.getId());
			}

		}

		Transaction tx = null;
		if (!containsKey)
			tx = graphDb.beginTx();
		for (RoleInvolved role : bodyActivity.getRoleInvolved())
		{
			boolean alreadyConnected = false;
			Node roleNode = retainNodeForResource(role.getRoleRef());
			for (Relationship rel : roleNode
					.getRelationships(ConnectionTypes.HAS_RESOURCE))
			{
				if (alreadyConnected)
					break;
				for (Node evaluatedNode : rel.getNodes())
				{
					if (alreadyConnected)
						break;
					if (evaluatedNode.equals(result))
						alreadyConnected = true;
				}
			}
			if (!alreadyConnected)
				result.createRelationshipTo(roleNode,
						ConnectionTypes.HAS_RESOURCE);
			releaseNodeForResource(roleNode.getProperty("name").toString(),
					roleNode);
		}

		for (UsedComponent material : bodyActivity.getUsedComponent())
		{
			boolean alreadyConnected = false;
			Node materialNode = retainNodeForResource(material
					.getComponentRef().toString());
			for (Relationship rel : materialNode
					.getRelationships(ConnectionTypes.HAS_RESOURCE))
			{
				if (alreadyConnected)
					break;
				for (Node evaluatedNode : rel.getNodes())
				{
					if (alreadyConnected)
						break;
					if (evaluatedNode.equals(result))
						alreadyConnected = true;
				}
			}
			if (!alreadyConnected)
				result.createRelationshipTo(materialNode,
						ConnectionTypes.HAS_RESOURCE);
			releaseNodeForResource(
					materialNode.getProperty("name").toString(), materialNode);
		}

		for (String assemblyLineType : bodyActivity
				.getUsedAssemlyLineType())
		{
			boolean alreadyConnected = false;
			Node assemblyLineNode = retainNodeForResource(assemblyLineType.toString());
			for (Relationship rel : assemblyLineNode
					.getRelationships(ConnectionTypes.HAS_RESOURCE))
			{
				if (alreadyConnected)
					break;
				for (Node evaluatedNode : rel.getNodes())
				{
					if (alreadyConnected)
						break;
					if (evaluatedNode.equals(result))
						alreadyConnected = true;
				}
			}
			if (!alreadyConnected)
				result.createRelationshipTo(assemblyLineNode,
						ConnectionTypes.HAS_RESOURCE);
			releaseNodeForResource(assemblyLineNode.getProperty("name").toString(),
					assemblyLineNode);
		}
		if (!containsKey)
		{
			tx.success();
			tx.finish();
		}
		// LOG.trace("Retained node:"+result.getId());
		return result;
	}

	private void releaseNodeForActivity(TSkeletonActivityType bodyActivity,
			Node node) throws ConcurrentModificationException
	{
		String activity = bodyActivity.getId();
		boolean containsKey = false;
		for (String otherActivity : activityRetainCount.keySet())
			if (otherActivity.equals(activity))
			{
				containsKey = true;
				activity = otherActivity;
			}

		if (containsKey)
		{
			termNodeMapping.put(activity, node);
			// LOG.trace("Released node:"+node.getId());
			activityRetainCount.put(
					activity,
					new Integer(Integer.valueOf(activityRetainCount.remove(
							activity).intValue() - 1)));
		} else
			throw new ConcurrentModificationException(
					activity
							+ " is not yet retained, please retain it first before releasing");
	}

	public Node getNodeValueForActivity(final String processID,
			TSkeletonActivityType body)
	{
		Node result = retainNodeForActivity(processID, body);
		releaseNodeForActivity(body, result);
		return result;
	}

	private void addConnection(final String processID, ConnectionTypes type,
			TSkeletonActivityType subject, TSkeletonActivityType target)
	{
		final Transaction tx = graphDb.beginTx();
		try
		{
			Node startNode = retainNodeForActivity(processID, subject);
			Node endNode = retainNodeForActivity(processID, target);
			Iterable<Path> paths = GraphUtil.getInstance().processPathTraverser
					.evaluator(Evaluators.includingDepths(1, 1))
					// .evaluator(Evaluators.atDepth(1))
					// .evaluator(Evaluators.toDepth(1))
					.evaluator(Evaluators.includeWhereEndNodeIs(endNode))
					.traverse(startNode);
			if (paths == null || !paths.iterator().hasNext())
			{
				createRelationshipsBetween(type, startNode, endNode);
				tx.success();
			} else
			{
				tx.failure();
			}
			releaseNodeForActivity(subject, startNode);
			releaseNodeForActivity(target, endNode);
		} catch (ConcurrentModificationException ce)
		{
			tx.failure();
			LOG.error(ce.getMessage(), ce);
		}
		tx.finish();
	}

	private synchronized Iterable<Path> getPathsBetweenActivities(
			final String processID, TSkeletonActivityType startBody,
			TSkeletonActivityType destBody)
	{
		final Transaction tx = graphDb.beginTx();
		PathFinder<Path> finder = GraphAlgoFactory.allPaths(Traversal
				.expanderForTypes(ConnectionTypes.NEXT, Direction.OUTGOING),
				MAX_SEARCH_DEPTH);
		if (logEnabled) LOG.info("Calculating path for " + startBody.getId() + " to "
				+ destBody.getId());
		Iterable<Path> paths = finder.findAllPaths(
				getNodeValueForActivity(processID, startBody),
				getNodeValueForActivity(processID, destBody));
		tx.success();
		tx.finish();
		return paths;
	}

	@JsonIgnore
	public Set<List<Task>> getAllPossiblePathsOfTasks()
	{
		LinkedHashSet<List<Task>> result = new LinkedHashSet<List<Task>>();
		LOG.error("MAP:" + allPossiblePathsOfTasks);
		result.addAll(allPossiblePathsOfTasks.values());
		return result;
	}

	// Find all possible paths
	public Set<List<String>> getPathOfActivities(final String processID,
			TSkeletonActivityType startBody, TSkeletonActivityType destBody)
			throws Exception
	{
		final Set<List<String>> paths = new LinkedHashSet<List<String>>();
		for (Path path : getPathsBetweenActivities(processID, startBody,
				destBody))
		{
			if (path == null)
				throw new Exception("Could not find path between " + startBody
						+ " and " + destBody);
			final List<String> activityPath = new ArrayList<String>();
			for (Node nextNode : path.nodes())
				for (Entry<String, Node> entry : termNodeMapping.entrySet())
					if (entry.getValue().equals(nextNode))
						activityPath.add(entry.getKey());
			paths.add(activityPath);
		}
		return paths;
	}

	private void createRelationshipsBetween(final ConnectionTypes type,
			final Node... nodes)
	{
		for (int i = 0; i < nodes.length - 1; i++)
		{
			if (nodes[i].getId() == nodes[i + 1].getId())
			{
				LOG.warn("Connection with self not allowed today!");
			} else
			{
				nodes[i].createRelationshipTo(nodes[i + 1], type);
			}
		}
	}

	public void addNextConnection(final String processID,
			TSkeletonActivityType subject, TSkeletonActivityType target)
	{
		if (subject.equals(target))
			return;
		if (logEnabled) LOG.info("Adding next between: " + subject.getId() + " and "
				+ target.getId());
		addConnection(processID, ConnectionTypes.NEXT, subject, target);
	}


	boolean hasLoop(final List<Set<String>> list)
	{

		if (list == null) // list does not exist..so no loop either.
			return false;

		Iterator<Set<String>> slowIterator = list.iterator();
		Iterator<Set<String>> fastIterator = list.iterator();

		Set<String> slow, fast; // create two references.
		if (!slowIterator.hasNext())
			return false;

		slow = fast = slowIterator.next(); // make both refer to the start of
											// the list.

		while (slowIterator.hasNext() && fastIterator.hasNext())
		{

			slow = slowIterator.next(); // 1 hop.
			fast = fastIterator.next();
			if (fastIterator.hasNext())
				fast = fastIterator.next(); // 2 hops.
			else
				return false; // next node null => no loop.

			if (slow == null || fast == null) // if either hits null..no loop.
				return false;

			for (String s : slow)
			{
				boolean found = false;
				for (String c : fast)
					if (!found && c.equals(s))
					{
						found = true;
						break;
					}
				if (!found)
				{
					return false;
				}
			}
			for (String f : fast)
			{
				boolean found = false;
				for (String c : slow)
					if (!found && c.equals(f))
					{
						found = true;
						break;
					}
				if (!found)
				{
					return false;
				}
			}
			// if the two ever meet...we must have a loop.
			return true;

		}
		// LOG.warn("Should not be here...");
		return false;
	}

	
}
