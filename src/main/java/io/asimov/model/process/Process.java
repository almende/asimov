package io.asimov.model.process;

import io.asimov.graph.GraphUtil;
import io.asimov.model.AbstractEntity;
import io.asimov.model.Resource;
import io.asimov.model.ResourceAllocation;
import io.asimov.model.ResourceRequirement;
import io.asimov.model.ResourceType;
import io.asimov.model.Time;
import io.asimov.model.XMLConvertible;
import io.asimov.model.resource.ResourceSubtype;
import io.asimov.model.sl.ASIMOVTerm;
import io.asimov.model.sl.ASIMOVTermSequenceNode;
import io.asimov.model.sl.SL;
import io.asimov.model.xml.XmlUtil;
import io.asimov.reasoning.sl.KBase;
import io.asimov.xml.TProcessType;
import io.asimov.xml.TSkeletonActivityType;
import io.asimov.xml.TSkeletonActivityType.NextActivityRef;
import io.asimov.xml.TSkeletonActivityType.PreviousActivityRef;
import io.asimov.xml.TSkeletonActivityType.UsedResource;
import io.coala.log.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Embedded;
import javax.persistence.ManyToOne;
import javax.xml.datatype.Duration;

import org.apache.log4j.Logger;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.kernel.Uniqueness;

/**
 * {@link Process}
 * 
 * @date $Date: 2014-11-24 17:57:34 +0100 (ma, 24 nov 2014) $
 * @version $Revision: 1123 $
 * @author <a href="mailto:rick@almende.org">Rick</a>
 * @author <a href="mailto:suki@almende.org">Suki</a>
 */
@NoSql(dataType = "processes")
public class Process extends AbstractEntity<Process> implements
		XMLConvertible<TProcessType, Process> // SLConvertible<Process>,
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private static final Logger LOG = LogUtil.getLogger(Process.class);

	/** */
	@ManyToOne
	@Field(name = "type")
	private ProcessType type;

	@Embedded
	@Field(name = "activities")
	/** the sequence/log of activities that have taken place in this process */
	private List<Activity> activities = new ArrayList<Activity>();

	@Embedded
	@Field(name = "allocations")
	/** the resource allocations for this process instance */
	private Set<ResourceAllocation> allocations = new HashSet<ResourceAllocation>();

	// CAP TEST

	@Embedded
	@Field(name = "tasks")
	/** */
	private Set<Task> pTasks = new HashSet<Task>();

	@Embedded
	@Field(name = "transitions")
	/** */
	private Set<Transition> pTransitions = new HashSet<Transition>();

	@Embedded
	@Field(name = "requiredResources")
	/** */
	private Map<String, ResourceRequirement> pRequiredResources = new HashMap<String, ResourceRequirement>();

	/** */
	@Embedded
	@Field(name = "replicationID")
	private String replicationID;

	/** */
	private static long counter = 0;

	/**
	 * {@link Process} constructor
	 */
	public Process() {
		// withTask(new
		// Task().withName(Task.START_OF_PROCESS.getName())).withTask(new
		// Task().withName(Task.END_OF_PROCESS.getName()));
		super.setName("process" + (++counter));
	}



	/** the {@link ResourceRequirement}s for this {@link Process} type */
	public Map<String, ResourceRequirement> getRequiredResources() {
		return this.pRequiredResources;
	}

	/** @return the {@link Task}s for this {@link Process} type */
	public Set<Task> getTasks() {
		return this.pTasks;
	}

	/** @return the {@link Transition}s for this {@link Process} type */
	public Set<Transition> getTransitions() {
		return this.pTransitions;
	}

	/** updates the case/trace identifiers associated with each task */
	public void updateTasks()// Set<Task> tasks)
	{
		// List<Task> taskList = new ArrayList<Task>(tasks);
		for (Transition transition : getTransitions()) {
			final Set<Task> allTransitionTasks = new HashSet<Task>(
					transition.getFromTasks());
			allTransitionTasks.addAll(transition.getToTasks());

			// update the Tasks
			for (Task task : getTasks()) {
				Map<String, Object> taskMR = new KBase().matchNode(
						Task.PATTERN, task.toSL());
				for (Task compareTask : allTransitionTasks) {
					final ASIMOVTerm compareTaskTerm = compareTask.toSL();
					if (compareTaskTerm.equals(taskMR.get(Task.TASK_NAME))) {
						// Node caseIdsNode =
						// transitionTaskMR.term(Task.CASE_IDS);
						for (String traceID : transition.getTraceIDs())
							task.withCaseIDs(traceID);
					}
				}
			}
		}
		// return new HashSet<Task>(taskList);
	}

	public Process withTask(final Task task) {
		getTasks().add(task);
		return this;
	}

	public Process withTask(String taskName, String... resourceIDs)
			throws Exception {
		return withTaskWithDescription(taskName, null, resourceIDs);
	}

	public Process withTaskWithDescription(String taskName,
			String taskDescription, String... resourceIDs) throws Exception {
		Task newTask = new Task().withName(taskName);// .withResourceRequirements(new
														// ResourceRequirement());
		if (taskDescription != null)
			newTask.withDescription(taskDescription);
		if (resourceIDs != null)
			for (String resourceID : resourceIDs) {
				ResourceRequirement resourceObject = getRequiredResources()
						.get(resourceID);
				if (resourceObject == null)
					LOG.warn("An unknown resource has been referenced in task "
							+ taskName
							+ "; specify resources for the Process first, "
							+ "then for the Tasks, and finally for Transitions");
				else
					newTask.withResourceRequirements(resourceObject);
			}
		return withTask(newTask);
	}

	private Task findTaskByName(String name) {
		for (Task task : getTasks())
			if (task.getName().equals(name))
				return task;
		return null;
	}

	private Transition findTransitionByTasks(String[] inputTaskNames,
			String[] outputTaskNames) {
		ASIMOVTerm[] iTaskTerms = new ASIMOVTerm[inputTaskNames.length];
		for (int i = 0; i < inputTaskNames.length; i++) {
			iTaskTerms[i] = SL.string(inputTaskNames[i]);
		}
		ASIMOVTermSequenceNode inputTaskNamesTermSequence = new ASIMOVTermSequenceNode(
				Arrays.asList(iTaskTerms));

		ASIMOVTerm[] oTaskTerms = new ASIMOVTerm[outputTaskNames.length];
		for (int i = 0; i < outputTaskNames.length; i++) {
			oTaskTerms[i] = SL.string(outputTaskNames[i]);
		}
		ASIMOVTermSequenceNode outputTaskNamesTermSequence = new ASIMOVTermSequenceNode(
				Arrays.asList(oTaskTerms));

		return findTransitionByTasks(inputTaskNamesTermSequence,
				outputTaskNamesTermSequence);
	}

	private Transition findTransitionByTasks(
			ASIMOVTermSequenceNode inputTaskNames,
			ASIMOVTermSequenceNode outputTaskNames) {
		for (Transition transition : getTransitions()) {
			Map<String, Object> transitionMR = new KBase().matchNode(
					Transition.PATTERN, transition.toSL());
			if (transitionMR.get(Transition.INPUT_TASK_NAMES).equals(
					inputTaskNames))
				if (transitionMR.get(Transition.OUTPUT_TASK_NAMES).equals(
						outputTaskNames))
					return transition;
		}
		return null;
	}

	public Process withResource(String resourceName, final String resourceType,
			ResourceSubtype resourceSubType, Time duration) {
		final ResourceType resource = new ResourceType().withName(resourceType);
		return withResource(new ResourceRequirement().withResource(
				new Resource().withName(resourceName).withTypeID(resource)
						.withSubTypeID(resourceSubType), 1, duration));
	}

	public Process withResource(final String resourceType,
			ResourceSubtype resourceSubType, Time duration) {
		final ResourceType resource = new ResourceType().withName(resourceType);
		return withResource(new ResourceRequirement().withResource(
				new Resource().withTypeID(resource).withSubTypeID(
						resourceSubType), 1, duration));
	}

	public Process withResource(ResourceRequirement resource) {
		String key = resource.getResource().getName();
		// if (getRequiredResources().containsKey(key))
		// getRequiredResources().remove(key);
		getRequiredResources().put(key, resource);
		/*
		 * Task startOfProcessTask = findTaskByName(START_OF_PROCESS);
		 * pTasks.remove(startOfProcessTask);
		 * pTasks.add(startOfProcessTask.withResourceRequirements(resource));
		 * 
		 * Task endOfProcessTask = findTaskByName(END_OF_PROCESS);
		 * pTasks.remove(endOfProcessTask);
		 * pTasks.add(endOfProcessTask.withResourceRequirements(resource));
		 */
		return this;
	}

	public Process withTransition(String[] sourceTasks, String[] targetTasks,
			String... caseIds) {
		Transition theTransition = getTransition(sourceTasks, targetTasks);
		// if (pTransitions.contains(theTransition))
		getTransitions().remove(theTransition); // FIXME keep old case/traceIDs?
		getTransitions().add(theTransition.withTraceIDs(caseIds));
		return this;
	}

	public Transition getTransition(String[] sourceTasks, String[] targetTasks) {
		Transition newTransition = findTransitionByTasks(sourceTasks,
				targetTasks);
		if (newTransition != null)
			return newTransition;
		// Otherwise create new one
		newTransition = new Transition();
		for (int i = 0; i < sourceTasks.length; i++) {
			if (findTaskByName(sourceTasks[i]) == null)
				getTasks().add(new Task().withName(sourceTasks[i]));
			newTransition.withFromTasks(findTaskByName(sourceTasks[i]));
		}
		for (int i = 0; i < targetTasks.length; i++) {
			if (findTaskByName(targetTasks[i]) == null)
				getTasks().add(new Task().withName(targetTasks[i]));
			newTransition.withToTasks(findTaskByName(targetTasks[i]));
		}
		return newTransition;
	}

	public Process withTransition(String sourceTask, String targetTask,
			String... caseIds) {
		String[] sTasks = { sourceTask };
		String[] tTasks = { targetTask };
		return withTransition(sTasks, tTasks, caseIds);
	}

	public Process withTransition(String sourceTask, String targetTask) {
		String[] sTasks = { sourceTask };
		String[] tTasks = { targetTask };
		return withTransition(sTasks, tTasks);
	}

	public Process withTransition(String[] sourceTasks, String[] targetTasks) {
		getTransitions().add(getTransition(sourceTasks, targetTasks));
		return this;
	}

	public Process withTransition(Transition transition) {
		getTransitions().add(transition);
		return this;
	}

	// END CAP TEST

	/**
	 * @param type
	 *            the {@link ProcessType} to set
	 */
	protected void setType(final ProcessType type) {
		this.type = type;
	}

	/** @return the type */
	public ProcessType getType() {
		return this.type;
	}

	/**
	 * @param name
	 *            the (new) {@link ProcessType}
	 * @return this {@link Process} object
	 */
	public Process withType(final ProcessType type) {
		setType(type);
		return this;
	}

	/** @return the activities */
	public List<Activity> getActivities() {
		return this.activities;
	}

	/**
	 * @param type
	 *            the {@link ProcessType} to set
	 */
	protected void setActivities(final List<Activity> activities) {
		this.activities = activities;
	}

	/**
	 * @param name
	 *            the (new) {@link List} of {@link Activity}
	 * @return this {@link Process} object
	 */
	public Process withActivities(final List<Activity> activities) {
		setActivities(activities);
		return this;
	}

	/**
	 * @param name
	 *            the (new) {@link List} of {@link Activity}
	 * @return this {@link Process} object
	 */
	public Process withActivities(final Activity... activities) {
		setActivities(Arrays.asList(activities));
		return this;
	}

	/** @return the allocations */
	public Set<ResourceAllocation> getAllocations() {
		return this.allocations;
	}

	/**
	 * @param allocations
	 *            the allocations to set
	 */
	protected void setAllocations(final Set<ResourceAllocation> allocations) {
		this.allocations = allocations;
	}

	/**
	 * @param agentID
	 *            the allocatedAgentID to set
	 * @param resourceID
	 *            the activity's resourceRequirementID to set
	 */
	public Process withAllocations(final ResourceAllocation... allocations) {
		if (allocations != null)
			for (ResourceAllocation allocation : allocations)
				getAllocations().add(allocation);
		return this;
	}

	/**
	 * @param agentID
	 *            the allocatedAgentID to set
	 * @param resourceID
	 *            the activity's resourceRequirementID to set
	 */
	public Process withAllocation(final String resourceID, final String agentID) {
		return withAllocations(new ResourceAllocation()
				.withResourceRequirementID(resourceID).withAllocatedAgentID(
						agentID));
	}

	@Override
	public void setName(String name) {
		for (Task t : this.getTasks()) {
			if (t.getName().startsWith(Task.START_OF_PROCESS.getName())) {
				t = t.withName(Task.START_OF_PROCESS.getName() + "_" + name);
			} else if (t.getName().startsWith(Task.END_OF_PROCESS.getName())) {
				t = t.withName(Task.END_OF_PROCESS.getName() + "_" + name);
			}
		}
		super.setName(name);
	}

	/** @see XMLConvertible#toXML() */
	@Override
	public TProcessType toXML() {
		TProcessType resultProcess = new TProcessType();
		resultProcess.setName(getName());
		for (Task task : this.getTasks()) {
			TSkeletonActivityType activity = new TSkeletonActivityType();
			resultProcess.getActivity().add(activity);
			activity.setId(task.getName());
			activity.setName(task.getDescription());
			for (ResourceRequirement resource : task.getResources().values()) {
				activity.setExecutionTime(XmlUtil.durationFromLong(resource
						.getDuration().getMillisecond()));

				UsedResource ur = new UsedResource();
				ur.setResourceTypeRef(resource.getResource().getTypeID()
						.getName());
				ur.setResourceSubTypeRef(resource.getResource().getSubTypeID()
						.getName());
				activity.getUsedResource().add(ur);
				ur.setTimeOfUse(XmlUtil.durationFromLong(resource.getDuration()
						.getMillisecond()));
			}
			// de-normalize
			deNormalizeLikelihoods(resultProcess);
			for (Transition transition : this.getTransitions()) {
				if (transition.getToTasks().contains(task)) {
					for (Task otherTask : transition.getFromTasks())
						if (!otherTask.getName().equals(
								Task.START_OF_PROCESS.getName() + "_"
										+ this.getName())) {
							PreviousActivityRef prevRef = null;
							for (PreviousActivityRef ref : activity
									.getPreviousActivityRef()) {
								if (ref.getValue().equals(otherTask.getValue())) {
									ref.setLikelihood(ref.getLikelihood() + 1);
								prevRef = ref;
								}
							}
							if (prevRef == null) {
								prevRef = new PreviousActivityRef();
								prevRef.setLikelihood(1.0);
								prevRef.setValue(otherTask.getValue());
								activity.getPreviousActivityRef().add(prevRef);
							}
						}
				}
				if (transition.getFromTasks().contains(task)) {

					for (Task otherTask : transition.getToTasks())
						if (!otherTask.getName().equals(
								Task.END_OF_PROCESS.getName() + "_"
										+ this.getName())) {
							NextActivityRef nextRef = null;
							for (NextActivityRef ref : activity
									.getNextActivityRef()) {
								if (ref.getValue().equals(otherTask.getValue())) {
									ref.setLikelihood(ref.getLikelihood() + 1);
									nextRef = ref;
								}
							}
							if (nextRef == null) {
								nextRef = new NextActivityRef();
								nextRef.setLikelihood(1.0);
								nextRef.setValue(otherTask.getValue());
								activity.getNextActivityRef().add(nextRef);
							}

						}
				}
			}

		}
		normalizeLikelihoods(resultProcess);
		return resultProcess;

	}

	// public Set<Task> getTasksWithResources(String resources ...) {
	//
	// }

	/** @see XMLConvertible#fromXML(Object) */
	@Override
	public Process fromXML(final TProcessType xmlBean) {
		String CASE_ID_PREFIX = "importFromXML";
		if (xmlBean == null || xmlBean.getActivity().size() == 0)
			return null;
		// Add start and end activities
		TSkeletonActivityType startActivity = new TSkeletonActivityType();
		TSkeletonActivityType endActivity = new TSkeletonActivityType();

		TProcessType processType = (TProcessType) xmlBean;
		// Set the name of the process
		String name = processType.getName();
		startActivity.setId(Task.START_OF_PROCESS.getName() + "_" + name);
		endActivity.setId(Task.END_OF_PROCESS.getName() + "_" + name);

		deNormalizeLikelihoods(processType);
		final List<TSkeletonActivityType> activities = processType
				.getActivity();

		this.withName(name);
		for (TSkeletonActivityType skeletonActivityType : activities) {
			ArrayList<String> taskResources = new ArrayList<String>();
			// Define resource requirements first:
			for (final UsedResource usedResource : skeletonActivityType
					.getUsedResource()) {
				Duration resourceTimeOfUse = usedResource.getTimeOfUse();
				if (resourceTimeOfUse == null) // Default resource usage time
												// equal to activity execution
												// time
					resourceTimeOfUse = skeletonActivityType.getExecutionTime();
				Time t = new Time().withMillisecond(XmlUtil
						.gDurationToLong(resourceTimeOfUse));
				this.withResource(usedResource.getResourceTypeRef(),
						new ResourceSubtype().withName(usedResource
								.getResourceSubTypeRef()), t);
				taskResources.add(usedResource.getResourceSubTypeRef()
						.toString());
			}
			// After all resource requirements have been added the tasks can be
			// added
			String[] taskResourcesStringArray = new String[taskResources.size()];
			taskResources.toArray(taskResourcesStringArray);
			try {
				this.withTaskWithDescription(skeletonActivityType.getId(),
						skeletonActivityType.getName(),
						taskResourcesStringArray);
			} catch (Exception e) {
				LOG.error(
						"Failed to create task: "
								+ skeletonActivityType.getName(), e);
			}
		}

		for (TSkeletonActivityType skeletonActivityType : processType
				.getActivity()) {
			if (skeletonActivityType.getNextActivityRef().size() == 0
					|| skeletonActivityType.getNextActivityRef().get(0)
							.equals("")) {
				if (skeletonActivityType.getNextActivityRef().size() > 0
						&& skeletonActivityType.getNextActivityRef().get(0)
								.equals("")) {
					PreviousActivityRef prevRef = new PreviousActivityRef();
					prevRef.setValue(skeletonActivityType.getId());
					endActivity.getPreviousActivityRef().add(prevRef);
					NextActivityRef nextRef = new NextActivityRef();
					nextRef.setValue(endActivity.getId());
					skeletonActivityType.getNextActivityRef().add(nextRef);
				} else {
					PreviousActivityRef prevRef = new PreviousActivityRef();
					prevRef.setValue(skeletonActivityType.getId());
					endActivity.getPreviousActivityRef().add(prevRef);
					NextActivityRef nextRef = new NextActivityRef();
					nextRef.setValue(endActivity.getId());
					skeletonActivityType.getNextActivityRef().add(nextRef);
				}
			}
			if (skeletonActivityType.getPreviousActivityRef().size() == 0
					|| skeletonActivityType.getPreviousActivityRef().get(0)
							.equals("")) {
				if (skeletonActivityType.getPreviousActivityRef().size() > 0
						&& skeletonActivityType.getPreviousActivityRef().get(0)
								.equals("")) {
					NextActivityRef nextRef = new NextActivityRef();
					nextRef.setValue(skeletonActivityType.getId());
					startActivity.getNextActivityRef().add(nextRef);
					PreviousActivityRef prevRef = new PreviousActivityRef();
					prevRef.setValue(startActivity.getId());
					skeletonActivityType.getPreviousActivityRef().add(prevRef);
				} else {
					NextActivityRef nextRef = new NextActivityRef();
					nextRef.setValue(skeletonActivityType.getId());
					startActivity.getNextActivityRef().add(nextRef);
					PreviousActivityRef prevRef = new PreviousActivityRef();
					prevRef.setValue(startActivity.getId());
					skeletonActivityType.getPreviousActivityRef().add(prevRef);
				}
			}
		}

		processType.getActivity().add(startActivity);
		processType.getActivity().add(endActivity);

		// now all transitions can be added
		for (TSkeletonActivityType skeletonActivityType : processType
				.getActivity()) {
			if (skeletonActivityType.getNextActivityRef().size() > 0
					&& !skeletonActivityType.getNextActivityRef().get(0)
							.equals(""))
				for (int i = 0; i < skeletonActivityType.getNextActivityRef()
						.size(); i++)
					GraphUtil.getInstance().addNextConnection(
							getName(),
							skeletonActivityType,
							getTSkeletonActivityTypeForId(processType,
									skeletonActivityType.getNextActivityRef()
											.get(i).getValue()));
			if (skeletonActivityType.getPreviousActivityRef().size() > 0
					&& !skeletonActivityType.getPreviousActivityRef().get(0)
							.equals(""))
				for (int i = 0; i < skeletonActivityType
						.getPreviousActivityRef().size(); i++)
					GraphUtil.getInstance().addNextConnection(
							getName(),
							getTSkeletonActivityTypeForId(processType,
									skeletonActivityType
											.getPreviousActivityRef().get(i)
											.getValue()), skeletonActivityType);
		}

		// now all transitions can be added
		try {
			int caseNr = 0;
			Set<List<String>> cases = GraphUtil.getInstance()
					.getPathOfActivities(getName(), startActivity, endActivity);
			if (cases.isEmpty()) {
				LOG.warn("Can not determine start of cyclic process, "
						+ "assuming longest path.");
				Node endNode = GraphUtil.getInstance().getNodeValueForActivity(
						this.getName(), endActivity);
				Iterable<Path> paths = null;
				for (int l = activities.size() + 1; l > 0
						&& (paths == null || !paths.iterator().hasNext()); l--)
					paths = GraphUtil.getInstance().reverserdProcessPathTraverser
							.evaluator(Evaluators.includingDepths(l, l))
							.uniqueness(Uniqueness.NODE_PATH).traverse(endNode);
				if (paths == null || !paths.iterator().hasNext())
					throw new IllegalStateException(
							"Failed to find start node in process "
									+ this.getName());
				for (TSkeletonActivityType firstActivity : activities)
					if (firstActivity.getId().equals(
							paths.iterator().next().lastRelationship()
									.getEndNode().getProperty("activityName")
									.toString()))
						GraphUtil.getInstance().addNextConnection(
								this.getName(), startActivity, firstActivity);
				cases = GraphUtil.getInstance().getPathOfActivities(getName(),
						startActivity, endActivity);

			}
			for (List<String> path : cases) {
				List<Task> tasks = new ArrayList<Task>();
				caseNr++;
				String previousSkeletonActivityType = null;
				for (String nextSkeletonActivityType : path) {
					if (previousSkeletonActivityType != null) {
						int numberOf = 1;
						for (NextActivityRef aRef : getTSkeletonActivityTypeForId(
								processType, previousSkeletonActivityType)
								.getNextActivityRef())
							if (aRef.getValue()
									.equals(nextSkeletonActivityType))
								numberOf = (aRef.getLikelihood() == null) ? 1
										: aRef.getLikelihood().intValue();
						for (int i = 0; i < numberOf; i++)
							this.withTransition(previousSkeletonActivityType,
									nextSkeletonActivityType, CASE_ID_PREFIX
											+ caseNr);
					}
					for (Task task : this.getTasks()) {
						if (task.getName().equals(nextSkeletonActivityType))
							tasks.add(task);
					}
					previousSkeletonActivityType = nextSkeletonActivityType;
				}
				GraphUtil.getInstance().allPossiblePathsOfTasks.put(
						CASE_ID_PREFIX + "_" + getName() + "_" + caseNr, tasks);
			}
			this.updateTasks();
		} catch (final Exception e) {
			LOG.error("Problem parsing xml: " + xmlBean, e);
			return null;
		}
		normalizeLikelihoods(processType);
		return this;
	}

	public static Double deNormalizeFactorForDistribution(
			final ArrayList<Double> denormalizedValues) {
		return deNormalizeFactorForDistribution(denormalizedValues, 1);
	}

	private static Double deNormalizeFactorForDistribution(
			final ArrayList<Double> denormalizedValues, int factor) {
		Iterator<Double> it = denormalizedValues.iterator();
		Double min = Double.MAX_VALUE;
		while (it.hasNext()) {
			Double value = it.next() * factor;
			if (!Double.isInfinite(value) && value == Math.floor(value)) {
				continue;
			}
			min = Math.min(min, value * factor);
		}
		if (min.equals(Double.MAX_VALUE))
			return Double.valueOf(factor);
		else
			return deNormalizeFactorForDistribution(denormalizedValues,
					factor + 1);
	}

	public static synchronized void deNormalizeLikelihoods(
			TProcessType processType) {
		 // de-normalize
		 for (TSkeletonActivityType a : processType.getActivity())
		 {
		 ArrayList<Double> denormalizedPrevValues = new ArrayList<Double>();
		 for (PreviousActivityRef r : a.getPreviousActivityRef())
		 {
		 denormalizedPrevValues.add(r.getLikelihood());
		 }
		 Double prevFactor =
		 deNormalizeFactorForDistribution(denormalizedPrevValues);
		 ArrayList<Double> denormalizedNextValues = new ArrayList<Double>();
		 for (NextActivityRef r : a.getNextActivityRef())
		 {
		 denormalizedNextValues.add(r.getLikelihood());
		 }
		 Double nextFactor =
		 deNormalizeFactorForDistribution(denormalizedNextValues);
		
		 for (PreviousActivityRef r : a.getPreviousActivityRef())
		 {
		 if (r.getLikelihood() == null)
		 r.setLikelihood(1.0);
		 else
		 r.setLikelihood(r.getLikelihood() * prevFactor);
		 }
		 for (NextActivityRef r : a.getNextActivityRef())
		 {
		 if (r.getLikelihood() == null)
		 r.setLikelihood(1.0);
		 else
		 r.setLikelihood(r.getLikelihood() * nextFactor);
		 }
		 }
	}

	public static synchronized void normalizeLikelihoods(
			TProcessType processType) {
		// normalize
//		for (TSkeletonActivityType a : processType.getActivity()) {
//			Double prevSum = 0.0;
//			for (PreviousActivityRef r : a.getPreviousActivityRef()) {
//				if (r.getLikelihood() == null)
//					r.setLikelihood(1.0);
//				prevSum += r.getLikelihood();
//			}
//			for (PreviousActivityRef r : a.getPreviousActivityRef()) {
//				r.setLikelihood(r.getLikelihood() / prevSum);
//			}
//			Double nextSum = 0.0;
//			for (NextActivityRef r : a.getNextActivityRef()) {
//				if (r.getLikelihood() == null)
//					r.setLikelihood(1.0);
//				nextSum += r.getLikelihood();
//			}
//			for (NextActivityRef r : a.getNextActivityRef()) {
//				r.setLikelihood(r.getLikelihood() / nextSum);
//			}
//		}
	}

	private TSkeletonActivityType getTSkeletonActivityTypeForId(TProcessType p,
			String id) {
		for (TSkeletonActivityType skeletonActivityType : p.getActivity())
			if (skeletonActivityType.getId().equals(id))
				return skeletonActivityType;
		LOG.warn("Did not find '" + id + "' for process: " + p.getName());
		return null;
	}

	/**
	 * @return the replicationID
	 */
	public String getReplicationID() {
		return this.replicationID;
	}

	/**
	 * @param replicationID
	 *            the replication to set
	 */
	public void setReplicationID(final String replicationID) {
		this.replicationID = replicationID;
	}

}
