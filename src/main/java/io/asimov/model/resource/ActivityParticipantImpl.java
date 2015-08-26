package io.asimov.model.resource;

import io.asimov.agent.resource.GenericResourceManagementWorld;
import io.asimov.agent.resource.ResourceManagementWorld;
import io.asimov.agent.scenario.ScenarioManagementWorld;
import io.asimov.db.Datasource;
import io.asimov.microservice.negotiation.ResourceReadyNotification;
import io.asimov.model.ASIMOVResourceDescriptor;
import io.asimov.model.ActivityParticipation;
import io.asimov.model.ActivityParticipation.ActivityParticipant;
import io.asimov.model.ActivityParticipation.Request;
import io.asimov.model.ActivityParticipationResourceInformation;
import io.asimov.model.CoordinationUtil;
import io.asimov.model.events.EventType;
import io.asimov.model.resource.DirectoryLookup.LookupInitiator;
import io.asimov.model.resource.RouteLookup.RouteInitiator;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.capability.configure.ConfiguringCapability;
import io.coala.enterprise.fact.CoordinationFact;
import io.coala.enterprise.role.AbstractExecutor;
import io.coala.enterprise.role.AbstractInitiator;
import io.coala.enterprise.role.Executor;
import io.coala.invoke.ProcedureCall;
import io.coala.invoke.Schedulable;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.time.SimTime;
import io.coala.time.SimTimeFactory;
import io.coala.time.TimeUnit;
import io.coala.time.Trigger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;

/**
 * {@link ActivityParticipantImpl}
 * 
 * @version $Revision: 1083 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class ActivityParticipantImpl extends
		AbstractExecutor<ActivityParticipation.Request> implements
		ActivityParticipant {

	/** */
	private static final long serialVersionUID = 1L;

	private static final Integer NORMAL_PRIORITY = new Integer(1);

	protected Logger LOG = LogUtil.getLogger(ActivityParticipantImpl.class);

	private ResouceReadyInitiatorImpl resourceReadyinitiator;

	private Set<String> activityBacklog = new HashSet<String>();
	
	private Set<String> processBacklog = new HashSet<String>();

	private AgentID scenarioReplicatorID = null;

	/**
	 * {@link ActivityParticipantImpl} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected ActivityParticipantImpl(final Binder binder) {
		super(binder);
		new LookupInitiatorImpl(binder);
		resourceReadyinitiator = new ResouceReadyInitiatorImpl(binder);
	}

	private static Set<String> readyResources = Collections
			.synchronizedSet(new HashSet<String>());

	private Map<ActivityParticipation.Request, Integer> priorities = new HashMap<ActivityParticipation.Request, Integer>();

	private final Set<AgentID> highestPriorityActivityLocationRepr = new HashSet<AgentID>();

	public Set<AgentID> getHighestPriorityActivityLocations() {
		synchronized (this.highestPriorityActivityLocationRepr) {
			return new HashSet<AgentID>(
					this.highestPriorityActivityLocationRepr);
		}
	}

	protected void updateHighestPriorityActivityLocation() {
		synchronized (this.highestPriorityActivityLocationRepr) {
			this.highestPriorityActivityLocationRepr.clear();
			int maxPriority = Integer.MIN_VALUE; // lowest
			activities: for (Entry<Request, Integer> entry : this.priorities
					.entrySet()) {
				if (entry.getValue().intValue() > maxPriority)
					this.highestPriorityActivityLocationRepr.clear();
				maxPriority = Math
						.max(maxPriority, entry.getValue().intValue());
				boolean targetFound = false;
				for (ActivityParticipationResourceInformation otherResource : entry
						.getKey().getOtherResourceInfo())
					if (!targetFound && !otherResource.isMoveable()
							&& otherResource.isInfrastructural()) {
						targetFound = true;
						if (maxPriority <= entry.getValue().intValue()) {
							this.highestPriorityActivityLocationRepr
									.add(otherResource.getResourceAgent());
							continue activities;
						}
					}
				if (!targetFound)
					LOG.error(
							"No target resource specified among other resources: "
									+ JsonUtil.toPrettyJSON(entry.getKey()
											.getOtherResourceInfo()),
							new NullPointerException());
			}
			LOG.info("New priority locations: "
					+ this.highestPriorityActivityLocationRepr);
		}
	}

	protected <T extends ResourceManagementWorld<?>> T getWorld(
			final Class<T> clazz) {
		return getBinder().inject(clazz);
	}

	/** @see Executor#onRequested(CoordinationFact) */
	@Override
	@Schedulable(RETRY_REQUEST)
	public void onRequested(final ActivityParticipation.Request request) {
		if (activityBacklog.contains(request.getResourceInfo()
				.getActivityInstanceId()))
			return;
		activityBacklog.add(request.getResourceInfo().getActivityInstanceId());
		updateUnavailable(request);
		if (request.getResourceInfo().isMoveable()) {
			if (!getWorld(GenericResourceManagementWorld.class)
					.getCurrentLocation().getValue().equalsIgnoreCase("world")
					&& getBinder().inject(ScenarioManagementWorld.class)
							.onSiteDelay(getTime()).toMilliseconds()
							.getMillis() != 0) {
				// leave the site and come back tomorrow to continue
				updateHighestPriorityActivityLocation();
				String target = null;
				for (ActivityParticipationResourceInformation otherResource : request
						.getOtherResourceInfo()) {
					if (!otherResource.isMoveable()
							&& otherResource.isInfrastructural()) {
						target = otherResource.getResourceName();
					}
				}
				getBinder().inject(RouteInitiator.class).initiate(this,
						request, target, true);
				LOG.warn("Postponed activity participation request until tomorrow: "
						+ request);
				return;
			}
			// assuming we promise to execute the request!
			this.priorities.put(request, NORMAL_PRIORITY);
			this.scenarioReplicatorID = request.getScenarioReplicatorID();
		}
		LOG.info("Handling activity participation request: " + request);
		if (request.getResourceInfo().isMoveable()) {
			ActivityParticipationResourceInformation targetInfo = null;
			for (ActivityParticipationResourceInformation otherResource : request
					.getOtherResourceInfo())
				if (!otherResource.isMoveable()
						&& otherResource.isInfrastructural())
					targetInfo = otherResource;

			if (!getWorld(GenericResourceManagementWorld.class)
					.getCurrentLocation().getValue()
					.equals(targetInfo.getResourceAgent().getValue())) {
				LOG.info("Resource not yet at target, moving will be required.");
			} else {
				this.resourceReadyinitiator.forProducer(this, request);
			}

		} else
			this.resourceReadyinitiator.forProducer(this, request);

		if (request.getResourceInfo().isMoveable()) {
			ActivityParticipationResourceInformation targetInfo = null;
			for (ActivityParticipationResourceInformation otherResource : request
					.getOtherResourceInfo())
				if (!otherResource.isMoveable()
						&& otherResource.isInfrastructural())
					targetInfo = otherResource;

			if (!getWorld(GenericResourceManagementWorld.class)
					.getCurrentLocation().getValue()
					.equals(targetInfo.getResourceAgent().getValue())) {
				LOG.info(request.getResourceInfo().getResourceName()
						+ " is not yet at target "
						+ targetInfo.getResourceName()
						+ " starts moving now on " + getTime());
				updateHighestPriorityActivityLocation();
				getBinder().inject(RouteInitiator.class).initiate(this,
						request, targetInfo.getResourceName(), false);
			}
		}
	}
	


	/**
	 * @param entity
	 * @throws Exception
	 */
	protected void doResourceParticipation(final ActivityParticipation request,
			SimTime scheduledExecutiontime) throws Exception {
		updateUnavailable((Request)request);
		final ASIMOVResourceDescriptor resource = (ASIMOVResourceDescriptor) getWorld(
				GenericResourceManagementWorld.class).getEntity();
		// Find corresponding assemblyLine for this activity
		ActivityParticipationResourceInformation targetInfo = null;
		for (ActivityParticipationResourceInformation otherResource : request
				.getOtherResourceInfo())
			if (!otherResource.isMoveable()
					&& otherResource.isInfrastructural())
				targetInfo = otherResource;

		if (getBinder().inject(ConfiguringCapability.class)
				.getProperty("walkingDisabled").getBoolean(false).booleanValue()
				&& resource.isMoveable()
				&& !getWorld(GenericResourceManagementWorld.class)
						.getCurrentLocation().equals(
								targetInfo.getResourceAgent())) {
			ProcedureCall<?> enter = ProcedureCall.create(this, this,
					ActivityParticipant.TRANSIT_TO_RESOURCE, request,
					targetInfo.getResourceAgent());
			getSimulator().schedule(enter, Trigger.createAbsolute(getTime()));
		}

		if (getBinder().inject(ScenarioManagementWorld.class)
				.onSiteDelay(getTime()).longValue() != 0) {
			LOG.warn("Resource is in after operating hours, will exit the site first.");
			activityBacklog.remove(request.getResourceInfo().getActivityInstanceId());
			ProcedureCall<?> j = ProcedureCall.create(this, this,
					RETRY_REQUEST, request);
			getSimulator().schedule(
					j,
					Trigger.createAbsolute(getTime().plus(
							getBinder().inject(ScenarioManagementWorld.class)
									.onSiteDelay(getTime()))));
			return;
		}
		LOG.info("Scheduling participation of resource: " + resource.getName());
		/*
		 * pseudo-code
		 * 
		 * - lookup route - schedule walk: - schedule/perform assemblyLine enter
		 * event - schedule/perform assemblyLine leave event - wait for other
		 * resources - schedule/perform activity start event - schedule/perform
		 * activity stop event
		 */
		
		final ProcedureCall<?> job = ProcedureCall.create(this, this,
				START_EXECUTING_ACTIVITY, request);

		getSimulator().schedule(job,
				Trigger.createAbsolute(scheduledExecutiontime.max(getTime())));

	}

	public final static String START_EXECUTING_ACTIVITY = "startExecutionActivity";

	@Schedulable(START_EXECUTING_ACTIVITY)
	public void startExecutionOfActivity(final ActivityParticipation request)
			throws Exception {
		LOG.info("Starts participating in activity: "
				+ request.getResourceInfo().getActivityName());
		
		ActivityParticipationResourceInformation resourceInfo = request
				.getResourceInfo();
		// Find other involved resources for this activity
		List<String> involvedResources = new ArrayList<String>();
		involvedResources.add(resourceInfo.getResourceName());
		for (ActivityParticipationResourceInformation otherResource : request
				.getOtherResourceInfo())
			involvedResources.add(otherResource.getResourceName());
		getWorld(GenericResourceManagementWorld.class).performActivityChange(
				resourceInfo.getProcessID(),
				resourceInfo.getProcessInstanceId(),
				resourceInfo.getActivityName(),
				resourceInfo.getActivityInstanceId(), involvedResources,
				EventType.START_ACTIVITY);

		ProcedureCall<?> job = ProcedureCall.create(this, this,
				STOP_EXECUTING_ACTIVITY, request);
		getSimulator().schedule(
				job,
				Trigger.createAbsolute(getTime().plus(
						request.getResourceInfo().getResourceUsageDuration())));
	}

	public final static String STOP_EXECUTING_ACTIVITY = "stopExecutionActivity";

	@Schedulable(STOP_EXECUTING_ACTIVITY)
	public void stopExecutionOfActivity(
			final ActivityParticipation.Request request) throws Exception {
		LOG.info("Stops executing activity: "
				+ request.getResourceInfo().getActivityName());
		ActivityParticipationResourceInformation resourceInfo = request
				.getResourceInfo();
		// Find other involved resources for this activity
		List<String> involvedResources = new ArrayList<String>();
		involvedResources.add(resourceInfo.getResourceName());
		for (ActivityParticipationResourceInformation otherResource : request
				.getOtherResourceInfo())
			involvedResources.add(otherResource.getResourceName());

		getWorld(GenericResourceManagementWorld.class).performActivityChange(
				resourceInfo.getProcessID(),
				resourceInfo.getProcessInstanceId(),
				resourceInfo.getActivityName(),
				resourceInfo.getActivityInstanceId(), involvedResources,
				EventType.STOP_ACTIVITY);
		updateAvailable(request);
		send(ActivityParticipation.Result.Builder.forProducer(this, request)
				.build());
	}
	
	private void updateUnavailable(ActivityParticipation.Request request) {
		if (getWorld(GenericResourceManagementWorld.class).isAvailable()) {
			getWorld(GenericResourceManagementWorld.class).setUnavailable();
			LOG.info("Resource becomes unavailable");
		}
	}
	
	private void updateAvailable(ActivityParticipation.Request request) {
		if (getWorld(GenericResourceManagementWorld.class).getEntity().getMaxNofUsesInProcess() != null) {
			if (!processBacklog.contains(request.getResourceInfo().getProcessInstanceId())){
				getWorld(GenericResourceManagementWorld.class).getEntity().setNofUsesInProcess(getWorld(GenericResourceManagementWorld.class).getEntity().getNofUsesInProcess()+1);
				if (getWorld(GenericResourceManagementWorld.class).getEntity().getNofUsesInProcess() >= getWorld(GenericResourceManagementWorld.class).getEntity().getMaxNofUsesInProcess()) {
					LOG.info("Resource stays unavailable because it reached maximum participations in process");
					getWorld(GenericResourceManagementWorld.class).setUnavailable();
				} else {
					getWorld(GenericResourceManagementWorld.class).setAvailable();
					LOG.info("Resource becomes available");
				}
			} else if (getWorld(GenericResourceManagementWorld.class).getEntity().getNofUsesInProcess() < getWorld(GenericResourceManagementWorld.class).getEntity().getMaxNofUsesInProcess()){
				getWorld(GenericResourceManagementWorld.class).setAvailable();
				LOG.info("Resource becomes available");
			} else {
				getWorld(GenericResourceManagementWorld.class).setUnavailable();
				LOG.info("Resource becomes Resource stays unavailable because it reached maximum participations in process");
			}
		} else if (!getWorld(GenericResourceManagementWorld.class).isAvailable()) {
			if (getWorld(GenericResourceManagementWorld.class).getEntity().getMaxNofUsesInActivity() != null) {
				getWorld(GenericResourceManagementWorld.class).getEntity().setNofUsesInActivity(getWorld(GenericResourceManagementWorld.class).getEntity().getNofUsesInActivity()+1);
				if (getWorld(GenericResourceManagementWorld.class).getEntity().getNofUsesInActivity() >= getWorld(GenericResourceManagementWorld.class).getEntity().getMaxNofUsesInActivity()) {
					getWorld(GenericResourceManagementWorld.class).setUnavailable();
					LOG.info("Resource stays unavailable because it reached maximum participations in activity");
				} else {
					getWorld(GenericResourceManagementWorld.class).setAvailable();
					LOG.info("Resource becomes available");
				}
			} else {
				getWorld(GenericResourceManagementWorld.class).setAvailable();
				LOG.info("Resource becomes available");
			}
		}
	}

	@Schedulable(TRANSIT_TO_RESOURCE)
	public void transitToResource(final ActivityParticipation.Request request,
			final AgentID targetAgentID) throws Exception {
		ActivityParticipationResourceInformation personInfo = request
				.getResourceInfo();
		if (getWorld(GenericResourceManagementWorld.class).getCurrentLocation()
				.getValue().equals("world"))
			getWorld(GenericResourceManagementWorld.class).enteredSite(
					getTime());
		List<String> fromActorAndTargetResource = new ArrayList<String>();
		fromActorAndTargetResource.add(getOwnerID().getValue());
		fromActorAndTargetResource.add(getWorld(
				GenericResourceManagementWorld.class).getCurrentLocation()
				.getValue());
		getWorld(GenericResourceManagementWorld.class).performActivityChange(
				personInfo.getProcessID(), personInfo.getProcessInstanceId(),
				personInfo.getActivityName(),
				personInfo.getActivityInstanceId(), fromActorAndTargetResource,
				EventType.TRANSIT_FROM_RESOURCE);
		LOG.info(personInfo.getResourceName()
				+ " transits from resource "
				+ getWorld(GenericResourceManagementWorld.class)
						.getCurrentLocation().getValue() + " for activity: "
				+ personInfo.getActivityName());
		List<String> toActorAndTargetResource = new ArrayList<String>();
		toActorAndTargetResource.add(getOwnerID().getValue());
		toActorAndTargetResource.add(targetAgentID.getValue());
		getWorld(GenericResourceManagementWorld.class).performActivityChange(
				personInfo.getProcessID(), personInfo.getProcessInstanceId(),
				personInfo.getActivityName(),
				personInfo.getActivityInstanceId(), toActorAndTargetResource,
				EventType.TRANSIT_TO_RESOURCE);
		if (getWorld(GenericResourceManagementWorld.class).getCurrentLocation()
				.getValue().equals("world"))
			getWorld(GenericResourceManagementWorld.class).leftSite(getTime());
		LOG.info(personInfo.getResourceName() + " transits to resource "
				+ targetAgentID.getValue() + " for activity: "
				+ personInfo.getActivityName());
	}

	/**
	 * {@link LookupInitiator}
	 * 
	 * @version $Revision: 1083 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public static class LookupInitiatorImpl extends
			AbstractInitiator<DirectoryLookup.Result> implements
			LookupInitiator {

		/** */
		private static final long serialVersionUID = 1L;

		/**
		 * {@link LookupInitiator} constructor
		 * 
		 * @param binder
		 */
		protected LookupInitiatorImpl(final Binder binder) {
			super(binder);
		}

		@Override
		public void onStated(final DirectoryLookup.Result state) {
			new IllegalStateException("NOT IMPLEMENTED").printStackTrace();
		}

	}

	/** @see ActivityParticipant#notifyResourcesReady(ActivityParticipation.Request) */
	@Override
	public void notifyResourcesReady(final Request request) {
		SimTime scheduledExecutionTime = getBinder().inject(
				SimTimeFactory.class).create(request.getTime().longValue(),
				TimeUnit.MILLIS);
		try {
			doResourceParticipation(request, scheduledExecutionTime);
		} catch (final Exception e) {
			LOG.error("Problem handling activity participation request: "
					+ request, e);
		}
	}

	/** @see io.asimov.model.resource.ActivityParticipation.ActivityParticipant#getCurrentlyOccupiedResource() */
	@Override
	public AgentID getCurrentlyOccupiedResource(
			final ActivityParticipation.Request cause) {
		if (cause.getResourceInfo().isMoveable())
			return getWorld(GenericResourceManagementWorld.class)
					.getCurrentLocation();
		throw new IllegalStateException(
				"Only resources with the isMoveable() property set to true can return this value");
	}

	/** @see io.asimov.model.resource.ActivityParticipation.ActivityParticipant#getScenarioAgentID() */
	@Override
	public AgentID getScenarioAgentID() {
		return this.scenarioReplicatorID;
	}

	/** @see io.asimov.model.resource.ActivityParticipation.ActivityParticipant#isReadyForActivity() */
	@Override
	public boolean isReadyForActivity(
			final ResourceReadyNotification.Request request) {
		if (!request.getResourceInfo().isMoveable()
				|| getBinder().inject(ConfiguringCapability.class)
						.getProperty("walkingDisabled").getBoolean(false))
			return emitWhenTrue(true, request);
		final AgentID here = getBinder().inject(
				GenericResourceManagementWorld.class).getCurrentLocation();
		LOG.info("Current location = " + here);
		for (final ActivityParticipationResourceInformation resc : request
				.getOtherResourceInfo())
			if (!resc.isMoveable() && resc.isInfrastructural()) {
				Iterator<AgentID> hp = getHighestPriorityActivityLocations()
						.iterator();
				return emitWhenTrue(
						resc.getResourceAgent().equals(here)
								&& hp.hasNext()
								&& hp.next().equals(here)
								&& (getBinder()
										.inject(ScenarioManagementWorld.class)
										.onSiteDelay(getTime()).longValue() == 0),
						request);
			}
		return false;
	}

	private boolean emitWhenTrue(final boolean bool,
			final ResourceReadyNotification.Request request) {
		if (bool)
			try {
				final String token = request.getResourceInfo()
						.getResourceAgent().getValue()
						+ request.getResourceInfo().getActivityInstanceId();
				if (!readyResources.contains(token)) {
					readyResources.add(token);
					getBinder()
							.inject(GenericResourceManagementWorld.class)
							.performActivityChange(
									request.getResourceInfo().getProcessID(),
									request.getResourceInfo()
											.getProcessInstanceId(),
									request.getResourceInfo().getActivityName(),
									request.getResourceInfo()
											.getActivityInstanceId(),
									Collections.singletonList(request
											.getResourceInfo()
											.getResourceName()),
									EventType.RESOURCE_READY_FOR_ACTIVITY);
				}
			} catch (Exception e) {
				LOG.error("Failed to send resource ready notification event", e);
			}
		return bool;
	}

	/** @see io.asimov.model.resource.ActivityParticipation.ActivityParticipant#walkRoute(io.asimov.model.resource.ActivityParticipation.Result) */
	@Override
	public void walkRoute(RouteLookup.Result state,
			ActivityParticipation.Request cause, final boolean tommorow) {
		LOG.info("Intending to walk route: "
				+ state.getRouteOfRepresentativesWithCoordidnates());
		updateUnavailable(cause);
		AgentID currentBE = null;
		SimTime planning = getTime();
		for (AgentID routeEntry : state
				.getRouteOfRepresentativesWithCoordidnates().keySet()) {
			if (currentBE == null) {
				currentBE = routeEntry;
				continue;
			}
			planning = planning.plus(CoordinationUtil.calculateTravelTime(
					SimTime.ZERO, CoordinationUtil
							.getCoordinatesForNonMovingElement(getBinder()
									.inject(Datasource.class), currentBE),
					CoordinationUtil.getCoordinatesForNonMovingElement(
							getBinder().inject(Datasource.class), routeEntry)));
			ProcedureCall<?> enter = ProcedureCall.create(this, this,
					ActivityParticipant.TRANSIT_TO_RESOURCE, cause, routeEntry);
			getSimulator().schedule(enter, Trigger.createAbsolute(planning));
		}
		activityBacklog.remove(cause.getResourceInfo().getActivityInstanceId());
		
		if (tommorow) {
			planning = planning.plus(getBinder().inject(
					ScenarioManagementWorld.class).onSiteDelay(planning));
			updateUnavailable(cause);
			ProcedureCall<?> continueProcessOfYesterday = ProcedureCall.create(
					this, this, ActivityParticipant.RETRY_REQUEST, cause);
			getSimulator().schedule(continueProcessOfYesterday,
					Trigger.createAbsolute(planning));
		} else {
			updateUnavailable(cause);
			ProcedureCall<?> continueProcess = ProcedureCall.create(this, this,
					ActivityParticipant.RETRY_REQUEST, cause);
			getSimulator().schedule(continueProcess,
					Trigger.createAbsolute(planning));
		}
	}

}