/* $Id: ActivityParticipantImpl.java 1083 2014-09-28 12:42:17Z krevelen $
 * $URL: https://redmine.almende.com/svn/a4eesim/trunk/adapt4ee-newsim/src/main/java/eu/a4ee/model/resource/impl/ActivityParticipantImpl.java $
 * 
 * Part of the EU project Adapt4EE, see http://www.adapt4ee.eu/
 * 
 * @license
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright (c) 2010-2014 Almende B.V. 
 */
package io.arum.model.resource;

import io.arum.model.resource.DirectoryLookup.LookupInitiator;
import io.arum.model.resource.RouteLookup.RouteInitiator;
import io.arum.model.resource.assemblyline.AssemblyLine;
import io.arum.model.resource.assemblyline.AssemblyLineResourceManagementWorld;
import io.arum.model.resource.person.Person;
import io.arum.model.resource.person.PersonResourceManagementWorld;
import io.arum.model.resource.supply.Material;
import io.arum.model.resource.supply.MaterialResourceManagementWorld;
import io.asimov.agent.resource.ResourceManagementWorld;
import io.asimov.db.Datasource;
import io.asimov.microservice.negotiation.ResourceReadyNotification;
import io.asimov.model.ActivityParticipation;
import io.asimov.model.ActivityParticipation.ActivityParticipant;
import io.asimov.model.ActivityParticipation.Request;
import io.asimov.model.ActivityParticipationResourceInformation;
import io.asimov.model.CoordinationUtil;
import io.asimov.model.events.EventType;
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
import io.coala.log.InjectLogger;
import io.coala.time.SimTime;
import io.coala.time.SimTimeFactory;
import io.coala.time.TimeUnit;
import io.coala.time.Trigger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
		ActivityParticipant
{

	/** */
	private static final long serialVersionUID = 1L;

	private static final Integer NORMAL_PRIORITY = new Integer(1);

	@InjectLogger
	private Logger LOG;

	private ResouceReadyInitiatorImpl resourceReadyinitiator;
	
	
	private AgentID scenarioReplicatorID = null;

	/**
	 * {@link ActivityParticipantImpl} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected ActivityParticipantImpl(final Binder binder)
	{
		super(binder);
		new LookupInitiatorImpl(binder);
		resourceReadyinitiator = new ResouceReadyInitiatorImpl(binder);
	}

	private Map<ActivityParticipation.Request, Integer> priorities = new HashMap<ActivityParticipation.Request, Integer>();

	private final Set<AgentID> highestPriorityActivityLocationRepr = new HashSet<AgentID>();

	public Set<AgentID> getHighestPriorityActivityLocations()
	{
		synchronized (this.highestPriorityActivityLocationRepr)
		{
			return new HashSet<AgentID>(
					this.highestPriorityActivityLocationRepr);
		}
	}

	protected void updateHighestPriorityActivityLocation()
	{
		synchronized (this.highestPriorityActivityLocationRepr)
		{
			this.highestPriorityActivityLocationRepr.clear();
			int maxPriority = Integer.MIN_VALUE; // lowest
			activities: for (Entry<Request, Integer> entry : this.priorities
					.entrySet())
			{
				if (entry.getValue().intValue() > maxPriority)
					this.highestPriorityActivityLocationRepr.clear();
				maxPriority = Math
						.max(maxPriority, entry.getValue().intValue());
				boolean assemblyLineFound = false;
				for (ActivityParticipationResourceInformation otherResource : entry
						.getKey().getOtherResourceInfo())
					if (otherResource.getResourceType().equals(
							ARUMResourceType.ASSEMBLY_LINE))
					{
						assemblyLineFound = true;
						if (maxPriority <= entry.getValue().intValue())
						{
							this.highestPriorityActivityLocationRepr
									.add(otherResource.getResourceAgent());
							continue activities;
						}
					}
				if (!assemblyLineFound)
					LOG.error(
							"No assemblyLine resource specified among other resources: "
									+ JsonUtil.toPrettyJSON(entry.getKey()
											.getOtherResourceInfo()),
							new NullPointerException());
			}
			LOG.info("New priority locations: "
					+ this.highestPriorityActivityLocationRepr);
		}
	}

	protected <T extends ResourceManagementWorld<?>> T getWorld(
			final Class<T> clazz)
	{
		return getBinder().inject(clazz);
	}

	/** @see Executor#onRequested(CoordinationFact) */
	@Override
	@Schedulable(RETRY_REQUEST)
	public void onRequested(final ActivityParticipation.Request request)
	{
		if (!getWorld(PersonResourceManagementWorld.class)
				.getCurrentLocation().getValue().equalsIgnoreCase("world")
				&& getWorld(PersonResourceManagementWorld.class)
						.onSiteDelay(getTime()).toMilliseconds()
						.getMillis() != 0)
		{
			// leave the building and come back tomorrow to continue
			updateHighestPriorityActivityLocation();
			getBinder().inject(RouteInitiator.class).initiate(this,request,
					true);
			LOG.warn("Postponed activity participation request until tomorrow: "
					+ request);
			return;
		}
		// assuming we promise to execute the request!
		this.priorities.put(request, NORMAL_PRIORITY);
		this.scenarioReplicatorID = request.getScenarioReplicatorID();
		LOG.info("Handling activity participation request: " + request);
		this.resourceReadyinitiator.forProducer(this, request);

		if (request.getResourceInfo().getResourceType() == ARUMResourceType.PERSON)
		{
			ActivityParticipationResourceInformation assemblyLineInfo = null;
			for (ActivityParticipationResourceInformation otherResource : request
					.getOtherResourceInfo())
				if (otherResource.getResourceType().equals(
						ARUMResourceType.ASSEMBLY_LINE))
					assemblyLineInfo = otherResource;

			if (!getWorld(PersonResourceManagementWorld.class)
					.getCurrentLocation().equals(assemblyLineInfo.getResourceAgent()))
			{
				LOG.info(request.getResourceInfo().getResourceName()
						+ " is not in assemblyLine " + assemblyLineInfo.getResourceName()
						+ " starts walking now on " + getTime());
				updateHighestPriorityActivityLocation();
				getBinder().inject(RouteInitiator.class).initiate(this,
						request, false);
			}
		}
	}

	/**
	 * @param entity
	 * @throws Exception
	 */
	protected void doPersonParticipation(final ActivityParticipation request, SimTime scheduledExecutiontime)
			throws Exception
	{
		final Person person = (Person) getWorld(
				PersonResourceManagementWorld.class).getEntity();
		// Find corresponding assemblyLine for this activity
		ActivityParticipationResourceInformation assemblyLineInfo = null;
		for (ActivityParticipationResourceInformation otherResource : request
				.getOtherResourceInfo())
			if (otherResource.getResourceType().equals(ARUMResourceType.ASSEMBLY_LINE))
				assemblyLineInfo = otherResource;

		if (getWorld(PersonResourceManagementWorld.class)
				.getCurrentLocation().equals(assemblyLineInfo.getResourceAgent())){
			if (getBinder().inject(PersonResourceManagementWorld.class)
					.onSiteDelay(getTime()).longValue() != 0) {
				LOG.warn("Person is in after working hours, will exit the building first.");
				ProcedureCall<?> j = ProcedureCall.create(this, this, RETRY_REQUEST, request);
				getSimulator().schedule(j, Trigger.createAbsolute(getTime().plus(getBinder().inject(PersonResourceManagementWorld.class)
					.onSiteDelay(getTime()))));
				return;
			}
			LOG.info("Scheduling participation of person: "
					+ person.getName());
			/* pseudo-code
			 * 
			 * - lookup route
			 * - schedule walk:
			 * 		- schedule/perform assemblyLine enter event
			 * 		- schedule/perform assemblyLine leave event
			 * - wait for other resources
			 * - schedule/perform activity start event
			 * - schedule/perform activity stop event
			 */
			
			final ProcedureCall<?> job = ProcedureCall.create(this, this,
					START_EXECUTING_ACTIVITY, request);
			
			getSimulator().schedule(job, Trigger.createAbsolute(scheduledExecutiontime.max(getTime())));
					
			
		} else
		{
			LOG.info("Person is not yet in the correct building element to perform activity "
					+ request.getResourceInfo().getActivityName());
		}
	}

	public final static String START_EXECUTING_ACTIVITY = "startExecutionActivity";

	@Schedulable(START_EXECUTING_ACTIVITY)
	public void startExecutionOfActivity(final ActivityParticipation request)
			throws Exception
	{
		LOG.info("Starts executing activity: "
				+ request.getResourceInfo().getActivityName());

		ActivityParticipationResourceInformation personInfo = request
				.getResourceInfo();
		// Find corresponding assemblyLine for this activity
		ActivityParticipationResourceInformation assemblyLineInfo = null;
		boolean foundAssemblyLine = false;
		for (ActivityParticipationResourceInformation otherResource : request
				.getOtherResourceInfo())
			if (otherResource.getResourceType().equals(ARUMResourceType.ASSEMBLY_LINE))
			{
				assemblyLineInfo = otherResource;
				foundAssemblyLine = true;
			}
		// getWorld(PersonResourceManagementWorld.class).performEvent(agentID,
		// eventType, timeStamp, refID, buildingElementName);
		getWorld(PersonResourceManagementWorld.class).performActivityChange(
				personInfo.getProcessID(),
				personInfo.getProcessInstanceId(),
				personInfo.getActivityName(), assemblyLineInfo.getResourceName(),
				EventType.START_ACTIVITY);
		// TraceService.getInstance(getID().getModelID().getValue()).saveEvent(
		// personInfo.getResourceAgent(), EventType.START_ACTIVITY,
		// getTime(), personInfo.getActivityName(),
		// assemblyLineInfo.getResourceName());

		ProcedureCall<?> job = ProcedureCall.create(this, this,
				STOP_EXECUTING_ACTIVITY, request);
		if (foundAssemblyLine)
			getSimulator().schedule(
					job,
					Trigger.createAbsolute(getTime().plus(
							request.getResourceInfo()
									.getResourceUsageDuration())));
		else
			LOG.error("No assemblyLine found to perform activity "
					+ personInfo.getActivityName() + " by person: "
					+ getOwnerID().getValue() + " for resources: "
					+ JsonUtil.toPrettyJSON(request.getOtherResourceInfo()));
	}

	public final static String STOP_EXECUTING_ACTIVITY = "stopExecutionActivity";

	@Schedulable(STOP_EXECUTING_ACTIVITY)
	public void stopExecutionOfActivity(
			final ActivityParticipation.Request request) throws Exception
	{
		LOG.info("Stops executing activity: "
				+ request.getResourceInfo().getActivityName());
		ActivityParticipationResourceInformation personInfo = request
				.getResourceInfo();
		// Find corresponding assemblyLine for this activity
		ActivityParticipationResourceInformation assemblyLineInfo = null;
		for (ActivityParticipationResourceInformation otherResource : request
				.getOtherResourceInfo())
			if (otherResource.getResourceType().equals(ARUMResourceType.ASSEMBLY_LINE))
				assemblyLineInfo = otherResource;
		getWorld(PersonResourceManagementWorld.class).performActivityChange(
				personInfo.getProcessID(),
				personInfo.getProcessInstanceId(),
				personInfo.getActivityName(), assemblyLineInfo.getResourceName(),
				EventType.STOP_ACTIVITY);
		// TraceService.getInstance(getID().getModelID().getValue()).saveEvent(
		// personInfo.getResourceAgent(), EventType.STOP_ACTIVITY,
		// getTime(), personInfo.getActivityName(),
		// assemblyLineInfo.getResourceName());
		send(ActivityParticipation.Result.Builder.forProducer(this, request)
				.build());
	}

	/**
	 * @param entity
	 * @throws Exception
	 */
	protected void doMaterialParticipation(final ActivityParticipation request, SimTime scheduledExecutionTime)
			throws Exception
	{
		final Material material = (Material) getWorld(MaterialResourceManagementWorld.class)
				.getEntity();
		
		
		LOG.info(getOwnerID()
				+ " Scheduling participation of material: "
				+ material.getName());
		ProcedureCall<?> job = ProcedureCall.create(this, this,
				START_USING_MATERIAL, request);
		
		getSimulator().schedule(job,
				Trigger.createAbsolute(scheduledExecutionTime.max(getTime())));
				
			
	}

	public final static String START_USING_MATERIAL = "STARTS_USING_MATERIAL";

	@Schedulable(START_USING_MATERIAL)
	public void startsMaterialUsageForActivity(
			final ActivityParticipation request) throws Exception
	{
		LOG.info("Started use of material: "
				+ request.getResourceInfo().getResourceName());
		ActivityParticipationResourceInformation materialInfo = request
				.getResourceInfo();
		boolean foundPerson = false;

		for (ActivityParticipationResourceInformation personInfo : request
				.getOtherResourceInfo())
			if (personInfo.getResourceType()
					.equals(ARUMResourceType.PERSON))
			{

				getWorld(MaterialResourceManagementWorld.class)
						.performUsageChange(materialInfo.getProcessID(),
								materialInfo.getProcessInstanceId(),
								materialInfo.getActivityName(),
								materialInfo.getResourceInstanceName(),
								personInfo.getResourceAgent().getValue(),
								EventType.START_USE_MATERIAL);
				foundPerson = true;
			}
		// for (ActivityParticipationResourceInformation otherResource : request
		// .getOtherResourceInfo())
		// if (otherResource.getResourceType().equals(ARUMResourceType.ASSEMBLY_LINE))
		// assemblyLineInfo = otherResource;

		ProcedureCall<?> job = ProcedureCall.create(this, this,
				STOP_USING_MATERIAL, request);

		if (!foundPerson)
			LOG.error("No person found to use material "
					+ materialInfo.getResourceName() + " in material agent: "
					+ getOwnerID().getValue() + " for resources: "
					+ JsonUtil.toPrettyJSON(request.getOtherResourceInfo()));
		else
			getSimulator().schedule(
					job,
					Trigger.createAbsolute(getTime().plus(
							request.getResourceInfo()
									.getResourceUsageDuration())));

	}

	public final static String STOP_USING_MATERIAL = "STOPS_USING_MATERIAL";

	@Schedulable(STOP_USING_MATERIAL)
	public void stopsMaterialUsageForActivity(
			final ActivityParticipation request) throws Exception
	{
		LOG.info("Stopped use of material: "
				+ request.getResourceInfo().getResourceName());
		
		ActivityParticipationResourceInformation materialInfo = request
				.getResourceInfo();
		// for (ActivityParticipationResourceInformation otherResource : request
		// .getOtherResourceInfo())
		// if (otherResource.getResourceType().equals(ARUMResourceType.ASSEMBLY_LINE))
		// assemblyLineInfo = otherResource;
		for (ActivityParticipationResourceInformation personInfo : request
				.getOtherResourceInfo())
			if (personInfo.getResourceType()
					.equals(ARUMResourceType.PERSON))
				getWorld(MaterialResourceManagementWorld.class)
						.performUsageChange(materialInfo.getProcessID(),
								materialInfo.getProcessInstanceId(),
								materialInfo.getActivityName(),
								materialInfo.getResourceInstanceName(),
								personInfo.getResourceAgent().getValue(),
								EventType.STOP_USE_MATERIAL);
		send(ActivityParticipation.Result.Builder.forProducer(this,
				(Request) request).build());
	}

	public final static String ROOM_IN_USE = "ROOM_IN_USE";

	@Schedulable(ROOM_IN_USE)
	public void assemblyLineInUseForActivity(final ActivityParticipation request)
	{
		LOG.info("Starts using assemblyLine "
				+ request.getResourceInfo().getResourceName()
				+ " for activity: "
				+ request.getResourceInfo().getActivityName());
		ProcedureCall<?> job = ProcedureCall.create(this, this, ROOM_IS_USED,
				request);
		getSimulator().schedule(
				job,
				Trigger.createAbsolute(getTime().plus(
						request.getResourceInfo().getResourceUsageDuration())));
	}

	public final static String ROOM_IS_USED = "ROOM_IS_USED";

//	private static final String RE_NOTIFY = null;

	@Schedulable(ROOM_IS_USED)
	public void assemblyLineWasUsedForActivity(final ActivityParticipation request)
			throws Exception
	{
		LOG.info("Stops using assemblyLine "
				+ request.getResourceInfo().getResourceName() + " ("
				+ " for activity: "
				+ request.getResourceInfo().getActivityName());
		// Here we can check if everybody is ready...
		send(ActivityParticipation.Result.Builder.forProducer(this,
				(Request) request).build());
	}

	@Schedulable(ARRIVE_AT_ASSEMBLY)
	public void enterBE(final ActivityParticipation.Request request,
			final AgentID beAGentID) throws Exception
	{
		ActivityParticipationResourceInformation personInfo = request
				.getResourceInfo();
		if (getWorld(PersonResourceManagementWorld.class)
				.getCurrentLocation().getValue().equals("world"))
			getWorld(PersonResourceManagementWorld.class).enteredSite(
					getTime());
		getWorld(PersonResourceManagementWorld.class).performOccupancyChange(
				personInfo.getProcessID(),
				personInfo.getProcessInstanceId(),
				personInfo.getActivityName(),
				getWorld(PersonResourceManagementWorld.class)
						.getCurrentLocation().getValue(), EventType.LEAVE_ASSEMBLY);
		LOG.info(personInfo.getResourceName()
				+ " left building element "
				+ getWorld(PersonResourceManagementWorld.class)
						.getCurrentLocation().getValue() + " for activity: "
				+ personInfo.getActivityName());
		getWorld(PersonResourceManagementWorld.class).performOccupancyChange(
				personInfo.getProcessID(),
				personInfo.getProcessInstanceId(),
				personInfo.getActivityName(), beAGentID.getValue(),
				EventType.ARIVE_AT_ASSEMBLY);
		if (getWorld(PersonResourceManagementWorld.class)
				.getCurrentLocation().getValue().equals("world"))
			getWorld(PersonResourceManagementWorld.class).leftSite(getTime());
		LOG.info(personInfo.getResourceName() + " entered building element "
				+ beAGentID.getValue() + " for activity: "
				+ personInfo.getActivityName());
		notifyResourcesReady(request);

	}

	

	/**
	 * @param entity
	 * @throws Exception
	 */
	protected void doAssemblyLineParticipation(final ActivityParticipation request, SimTime scheduledExecutionTime)
			throws Exception
	{
		final AssemblyLine assemblyLine = (AssemblyLine) getWorld(AssemblyLineResourceManagementWorld.class)
				.getEntity();
		if (assemblyLine.getName() != null)
			LOG.info("Scheduling participation of assemblyLine: "
					+ assemblyLine.getName());
		else
			LOG.info("Scheduling participation of assemblyLine: " + assemblyLine.getName());
		ProcedureCall<?> job = ProcedureCall.create(this, this, ROOM_IN_USE,
				request);
		getSimulator().schedule(job, Trigger.createAbsolute(scheduledExecutionTime.max(getTime())));

		/* pseudo-code
		 * 
		 * - wait for other resources
		 * - schedule/perform material usage start event
		 * - schedule/perform material usage stop event
		 */
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
			LookupInitiator
	{

		/** */
		private static final long serialVersionUID = 1L;

		/**
		 * {@link LookupInitiator} constructor
		 * 
		 * @param binder
		 */
		protected LookupInitiatorImpl(final Binder binder)
		{
			super(binder);
		}

		@Override
		public void onStated(final DirectoryLookup.Result state)
		{
			new IllegalStateException("NOT IMPLEMENTED").printStackTrace();
		}

	}
	/** @see ActivityParticipant#notifyResourcesReady(ActivityParticipation.Request) */
	@Override
	public void notifyResourcesReady(final Request request)
	{
		SimTime scheduledExecutionTime = getBinder().inject(SimTimeFactory.class).create(request.getTime().longValue(), TimeUnit.MILLIS);
		try
		{
			switch (request.getResourceInfo().getResourceType())
			{
			case PERSON:
				doPersonParticipation(request, scheduledExecutionTime);
				break;
			case ASSEMBLY_LINE:
				doAssemblyLineParticipation(request, scheduledExecutionTime);
				break;
			case MATERIAL:
				doMaterialParticipation(request, scheduledExecutionTime);
				break;
			default:
				LOG.error("Resource type can't participate: "
						+ request.getResourceInfo().getResourceType());
			}
			} catch (final Exception e)
			{
				LOG.error("Problem handling activity participation request: "
						+ request, e);
			}
	}

	/** @see eu.a4ee.model.resource.ActivityParticipation.ActivityParticipant#getCurrentlyOccupiedResource() */
	@Override
	public AgentID getCurrentlyOccupiedResource(
			final ActivityParticipation.Request cause)
	{
		if (cause.getResourceInfo().getResourceType()
				.equals(ARUMResourceType.PERSON))
			return getWorld(PersonResourceManagementWorld.class)
					.getCurrentLocation();
		throw new IllegalStateException(
				"Only moving elements such as persons can return this value");
	}

	/** @see eu.a4ee.model.resource.ActivityParticipation.ActivityParticipant#getScenarioAgentID() */
	@Override
	public AgentID getScenarioAgentID()
	{
		return this.scenarioReplicatorID;
	}

	/** @see eu.a4ee.model.resource.ActivityParticipation.ActivityParticipant#isReadyForActivity() */
	@Override
	public boolean isReadyForActivity(
			final ResourceReadyNotification.Request request)
	{
		if (getBinder().inject(ConfiguringCapability.class)
				.getProperty("walkingDisabled").getBoolean(false))
			return true;
		final AgentID here = getBinder().inject(
				PersonResourceManagementWorld.class).getCurrentLocation();
		LOG.info("Current location = " + here);
		for (final ActivityParticipationResourceInformation resc : request
				.getOtherResourceInfo())
			if (resc.getResourceType() == ARUMResourceType.ASSEMBLY_LINE)
			{
				Iterator<AgentID> hp = getHighestPriorityActivityLocations().iterator();
				return resc.getResourceAgent().equals(here)
						&& hp.hasNext() && hp.next().equals(here)
						&& (getBinder().inject(PersonResourceManagementWorld.class)
								.onSiteDelay(getTime()).longValue() == 0);
			}
		return false;
	}

	/** @see eu.a4ee.model.resource.ActivityParticipation.ActivityParticipant#walkRoute(eu.a4ee.model.resource.ActivityParticipation.Result) */
	@Override
	public void walkRoute(RouteLookup.Result state,
			ActivityParticipation.Request cause, final boolean tommorow)
	{
		LOG.info("Intending to walk route: "
				+ state.getRouteOfRepresentativesWithCoordidnates());
		AgentID currentBE = null;
		SimTime planning = getTime();
		for (AgentID routeEntry : state
				.getRouteOfRepresentativesWithCoordidnates().keySet())
		{
			if (currentBE == null)
			{
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
					ActivityParticipant.ARRIVE_AT_ASSEMBLY, cause, routeEntry);
			getSimulator().schedule(enter, Trigger.createAbsolute(planning));
		}
		if (tommorow)
		{
			planning = planning.plus(getWorld(
					PersonResourceManagementWorld.class)
					.onSiteDelay(planning));
			ProcedureCall<?> continueProcessOfYesterday = ProcedureCall.create(
					this, this, ActivityParticipant.RETRY_REQUEST, cause);
			getSimulator().schedule(continueProcessOfYesterday,
					Trigger.createAbsolute(planning));
		}
	}

}
