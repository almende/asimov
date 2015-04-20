package io.asimov.model.usecase;

import io.asimov.agent.scenario.ScenarioManagementWorld;
import io.asimov.db.Datasource;
import io.asimov.model.ASIMOVResourceDescriptor;
import io.asimov.model.CoordinationUtil;
import io.asimov.model.resource.RouteLookup;
import io.asimov.model.resource.RouteLookup.Result;
import io.asimov.xml.TConnectedResource;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.enterprise.fact.CoordinationFact;
import io.coala.enterprise.role.AbstractExecutor;
import io.coala.enterprise.role.Executor;
import io.coala.log.InjectLogger;
import io.coala.model.ModelComponentIDFactory;

import java.io.File;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.Traversal;

/**
 * {@link RouteProviderImpl}
 * 
 * @version $Revision: 1083 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class RouteProviderImpl extends AbstractExecutor<RouteLookup.Request>
		implements RouteLookup.RouteProvider
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	@InjectLogger
	private Logger LOG;

	private File temp;

	private static GraphDatabaseService graphDb;
	private Transaction tx;

	private static final int MAX_SEARCH_DEPTH = 15;

	private static enum ConnectionTypes implements RelationshipType
	{
		UNKNOWN, LEADS_TO
	}

	/**
	 * {@link RouteProviderImpl} constructor
	 * 
	 * @param binder
	 */
	@Inject
	private RouteProviderImpl(final Binder binder)
	{
		super(binder);
		try
		{
			temp = File.createTempFile("pathFinder", null);
			temp.delete();
			temp.mkdir();
			graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(temp
					.getAbsolutePath());
		} catch (final Exception e)
		{
			LOG.error("Could not create pathFinder temporary database.");
		}
	}

	@Override
	public void activate()
	{
		updateRoute();
	}

	/**
	 * Read all connections between building elements from world and update the
	 * service
	 */
	private void updateRoute()
	{
		LOG.info("Updating RouteProvider directory name: "
				+ temp.getAbsolutePath());

		Iterable<ASIMOVResourceDescriptor> resourceDescriptions = null;
		
		try
		{
			resourceDescriptions = getWorld().getResourceDescriptors();
		} catch (Exception e)
		{
			LOG.error(
					"Agent failed to retrieve the bean it represents from the db!",
					e);
			return;
		}

		
		Set<String> connectionTokens = new HashSet<String>();

		for (ASIMOVResourceDescriptor resource : resourceDescriptions)
		{
			
			final AgentID target = resource.getAgentID();
			for (TConnectedResource otherResource : resource.getConnectedResources())
			{
				
				final AgentID subject = getBinder().inject(ModelComponentIDFactory.class)
				.createAgentID(otherResource.getConnectedResourceId());
				LOG.trace("Parsing resource:" + subject.getValue());
					if (subject == target)
						continue;
					String token = getConnectionToken(target, subject);
					if (!connectionTokens.contains(token))
					{
						addLeadsToConnection(target, subject);
						connectionTokens.add(token);
					}
				
			}
		}


		System.err.println("Updated RouteProvider");

	}

	private String getConnectionToken(AgentID a, AgentID b)
	{
		return a + "-->" + b;
	}

	// @Override
	protected ScenarioManagementWorld getWorld()
	{
		return getBinder().inject(ScenarioManagementWorld.class);
	}

	private void createRelationshipsBetween(final ConnectionTypes type,
			final Node... nodes)
	{
		for (int i = 0; i < nodes.length - 1; i++)
		{
			nodes[i].createRelationshipTo(nodes[i + 1], type);
		}
	}

	public void addLeadsToConnection(AgentID subject, AgentID target)
	{
		LOG.info("Route provider knows about the relation between: "
				+ subject.getValue() + " and " + target.getValue());
		addConnection(ConnectionTypes.LEADS_TO, subject, target);
	}

	/*
	 * WARNING: Actually removes node from the mapping, must be relased again
	 * */
	@SuppressWarnings("deprecation")
	private Node retainNodeForAgentID(AgentID bodyAgentID)
			throws ConcurrentModificationException
	{
		Node result = null;
		for (Node n : graphDb.getAllNodes())
			if (n.hasProperty("agentID")
					&& n.getProperty("agentID").equals(bodyAgentID.getValue()))
				result = n;
		if (result == null)
		{
			result = graphDb.createNode();
			if (!result.hasProperty("agentID"))
				result.setProperty("agentID", bodyAgentID.getValue());
			// LOG.trace("Created node:"+result.getId());
		}
		// LOG.trace("Retained node:"+result.getId());
		return result;
	}

	private Node getNodeValueForAgentID(AgentID body)
	{
		Node result = retainNodeForAgentID(body);
		return result;
	}

	private void addConnection(ConnectionTypes type, AgentID subject,
			AgentID target)
	{
		tx = graphDb.beginTx();
		try
		{
			Node startNode = retainNodeForAgentID(subject);
			if (!startNode.hasProperty("agentID"))
				startNode.setProperty("agentID", subject.getValue());
			Node endNode = retainNodeForAgentID(target);
			if (!endNode.hasProperty("agentID"))
				endNode.setProperty("agentID", target.getValue());
			createRelationshipsBetween(type, startNode, endNode);
			tx.success();
			tx.finish();
		} catch (ConcurrentModificationException ce)
		{
			tx.failure();
			tx.finish();
			LOG.error(ce.getMessage(), ce);
		}
	}

	private synchronized Iterable<Path> getPathsBetweenBodies(
			AgentID startBody, AgentID destBody)
	{
		tx = graphDb.beginTx();
		PathFinder<Path> finder = GraphAlgoFactory.shortestPath(
				Traversal.expanderForTypes(ConnectionTypes.LEADS_TO,
						Direction.OUTGOING), MAX_SEARCH_DEPTH);
		// LOG.trace("Calculating path for "+startBody+" to "+destBody);
		Iterable<Path> paths = finder.findAllPaths(
				getNodeValueForAgentID(startBody),
				getNodeValueForAgentID(destBody));
		tx.success();
		tx.finish();
		return paths;
	}

	private synchronized Path getBestPathsBetweenBodies(AgentID startBody,
			AgentID destBody)
	{
		Iterable<Path> result = getPathsBetweenBodies(startBody, destBody);
		if (result == null)
			return null;
		Iterator<Path> it = result.iterator();
		if (it.hasNext())
			return it.next();
		else
			return null;
	}

	private LinkedHashMap<AgentID, List<Number>> getPathOfAgentIDs(
			AgentID startBody, AgentID destBody) throws Exception
	{
		final LinkedHashMap<AgentID, List<Number>> agentIDPath = new LinkedHashMap<AgentID, List<Number>>();
		Path path = getBestPathsBetweenBodies(startBody, destBody);
		if (path == null)
			throw new Exception("Could not find path between " + startBody
					+ " and " + destBody);
		for (Node nextNode : path.nodes())
		{
			AgentID agentID = newAgentID(nextNode.getProperty("agentID")
					.toString());
			agentIDPath.put(agentID, CoordinationUtil
					.getCoordinatesForNonMovingElement(
							getBinder().inject(Datasource.class), agentID));
		}
		return agentIDPath;
	}

	//
	// private AgentID getNextAgentIDForPath(AgentID startBody, AgentID
	// destBody) throws Exception {
	// Path path = getBestPathsBetweenBodies(startBody, destBody);
	// if (path == null)
	// throw new
	// Exception("Could not find path between "+startBody+" and "+destBody);
	// for (Node nextNode : path.nodes())
	// if (!path.startNode().equals(nextNode))
	// for (Entry<AgentID,Node> entry : agentIDNodeMapping.entrySet())
	// if (entry.getValue().equals(nextNode))
	// return entry.getKey();
	// return null;
	// }

	/** @see Executor#onRequested(CoordinationFact) */
	@Override
	public void onRequested(final RouteLookup.Request request)
	{
		try
		{
			LOG.info("Requested route to: " + request.getDestinationResource());
			// FIXME: Actually calculate the route:
			final LinkedHashMap<AgentID, List<Number>> routeOfRepresentativesWithCoordidnates = getPathOfAgentIDs(
					request.getCurrentlyOccpupiedResource(),
					request.getDestinationResource());
			final Result result = RouteLookup.Result.Builder.forProducer(this,
					request, routeOfRepresentativesWithCoordidnates).build();
			LOG.info("Sending route: " + result);
			send(result);
		} catch (final Exception e)
		{
			e.printStackTrace();
			LOG.error("Problem handling directory lookup request: " + request,
					e);
		}
	}

}
