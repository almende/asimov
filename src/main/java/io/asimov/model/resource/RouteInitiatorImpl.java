package io.asimov.model.resource;

import io.asimov.agent.process.NonSkeletonActivityCapability;
import io.asimov.model.ActivityParticipation;
import io.asimov.model.ActivityParticipation.ActivityParticipant;
import io.asimov.model.resource.RouteLookup.Result;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.enterprise.role.AbstractInitiator;
import io.coala.log.InjectLogger;

import javax.inject.Inject;

import org.apache.log4j.Logger;

public class RouteInitiatorImpl extends
		AbstractInitiator<RouteLookup.Result> implements
		RouteLookup.RouteInitiator
{

	/** */
	private static final long serialVersionUID = 1L;

	@InjectLogger
	private Logger LOG;

	private ActivityParticipation.Request cause;
	
	private boolean walkTomorrow;
	
	
	/**
	 * {@link RouteInitiatorImpl} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected RouteInitiatorImpl(Binder binder)
	{
		super(binder);

	}

	/** @see io.coala.enterprise.role.AbstractInitiator#onStated(io.coala.enterprise.fact.CoordinationFact) */
	@Override
	protected void onStated(Result state)
	{
		LOG.info("Handling route lookup result: " + state);
		getBinder().inject(ActivityParticipant.class).walkRoute(state, cause, walkTomorrow);
//		ProcedureCall<?> doneWalkingJob = ProcedureCall.create(this,
//				getBinder().inject(ResourceReadyNotificationInitiator.class),
//				ResourceReadyNotification.ResourceReadyNotificationInitiator.DONE_WALKING, cause);
//		getSimulator().schedule(doneWalkingJob, Trigger.createAbsolute(planning));
	}
	
	

	/**
	 * @see io.asimov.model.scenario.RouteLookup.RouteInitiator#initiate(io.asimov.model.resource.ActivityParticipation.ActivityParticipant,
	 *      io.asimov.model.resource.ActivityParticipation.Request)
	 */
	@Override
	public RouteInitiatorImpl initiate(ActivityParticipant producer,
			ActivityParticipation.Request cause, final String targetResourceName, final boolean tomorrow)
	{
		this.walkTomorrow = tomorrow;
		this.cause = cause;
		try
		{
			send(RouteLookup.Request.Builder.forProducer(producer, cause, targetResourceName, tomorrow)
					.build());
		} catch (Exception e)
		{
			LOG.error("Failed to send route lookup request", e);
		}
		return this;
	}

	/** @see io.asimov.model.scenario.RouteLookup.RouteInitiator#initiate(io.asimov.model.process.NonSkeletonActivityCapability) */
	@Override
	public RouteInitiatorImpl initiate(
			NonSkeletonActivityCapability initiator, final AgentID currentLocation, final AgentID scenarioAgentID)
	{
		try
		{
			send(RouteLookup.Request.Builder.forProducer(initiator, currentLocation,scenarioAgentID)
					.build());
		} catch (Exception e)
		{
			LOG.error("Failed to send route lookup request", e);
		}
		return this;
	}




}