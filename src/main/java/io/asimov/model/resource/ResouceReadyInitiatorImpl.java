package io.asimov.model.resource;

import io.asimov.microservice.negotiation.ResourceReadyNotification;
import io.asimov.microservice.negotiation.ResourceReadyNotification.ResourceReadyNotificationInitiator;
import io.asimov.model.ActivityParticipation;
import io.asimov.model.ActivityParticipation.ActivityParticipant;
import io.asimov.model.ActivityParticipation.Request;
import io.asimov.model.ActivityParticipationResourceInformation;
import io.coala.bind.Binder;
import io.coala.capability.BasicCapabilityStatus;
import io.coala.enterprise.role.AbstractInitiator;
import io.coala.log.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;

/**
 * {@link ResouceReadyInitiatorImpl}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
public class ResouceReadyInitiatorImpl extends
		AbstractInitiator<ResourceReadyNotification.Result> implements
		ResourceReadyNotificationInitiator
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private static final Logger LOG = LogUtil
			.getLogger(ResouceReadyInitiatorImpl.class);

	private static final Map<ActivityParticipation,ResouceReadyInitiatorHandler> instances = 
			Collections
			.synchronizedMap(
					new HashMap<ActivityParticipation,ResouceReadyInitiatorImpl
						.ResouceReadyInitiatorHandler>());

	private static ResouceReadyInitiatorHandler getInstance(final ActivityParticipation cause) {
		return instances.get(cause);
	}
	
	/**
	 * {@link ResouceReadyInitiatorImpl} constructor
	 * 
	 * @param binder
	 * @param activityParticipatorImpl TODO
	 */
	@Inject
	public ResouceReadyInitiatorImpl(Binder binder)
	{
		super(binder);
	}
	
	private class ResouceReadyInitiatorHandler {
		
		public ResouceReadyInitiatorHandler(final ActivityParticipation cause, 
				final ActivityParticipant initiator){
			this.cause = cause;
			this.initiator = initiator;
		}
		
		private final ActivityParticipation cause;
		
		private boolean done = false;
		
		private final ActivityParticipant initiator;

		private synchronized List<ActivityParticipationResourceInformation> getPendingResourcesForActivity(
				final String activityName, final String processInstanceID)
		{
			final String key = activityName+"_"+processInstanceID;
			if (this.pendingResources.containsKey(key))
				return this.pendingResources.get(key);
			else
			{
				this.pendingResources.put(key,
						new ArrayList<ActivityParticipationResourceInformation>());
				return getPendingResourcesForActivity(activityName, processInstanceID);
			}
		}
		
		private Map<String, List<ActivityParticipationResourceInformation>> pendingResources = new HashMap<String, List<ActivityParticipationResourceInformation>>();
		public void handle(ResourceReadyNotification.Result result) {
			if (!result.getResourceInfo().getActivityName()
					.equals(cause.getResourceInfo().getActivityName()) || 
				!result.getResourceInfo().getProcessInstanceId()
					.equals(cause.getResourceInfo().getProcessInstanceId()))
				return;
			if (!getPendingResourcesForActivity(
					cause.getResourceInfo().getActivityName(), cause.getResourceInfo().getProcessInstanceId()).remove(
					result.getResourceInfo()))
			{
//				LOG.error(cause.getResourceInfo().getActivityName()
//						+ " Unknown resource is ready: "
//						+ result.getResourceInfo().getResourceName()
//						+ " was not in "
//						+ JsonUtil.toJSONString(this.pendingResources));
				// This means that the notification was already received
				return;
			} else
			{
				LOG.info(getBinder().getID()
						+ " got a valid resource ready notifaction from: "
						+ result.getResourceInfo().getResourceAgent());
			}
			if (getPendingResourcesForActivity(
					cause.getResourceInfo().getActivityName(), cause.getResourceInfo().getProcessInstanceId()).isEmpty())
			{
				LOG.info("All resources are ready to participate in activity "
						+ result.getResourceInfo().getActivityName() + "!");
				final String key = cause.getResourceInfo().getActivityName()+"_"+cause.getResourceInfo().getProcessInstanceId();
				this.pendingResources.remove(key);
				getBinder().inject(ActivityParticipant.class).notifyResourcesReady(
						(Request) cause);
				done = true;
			} else
			{
				LOG.info(getBinder().getID() + "Still waiting for "
						+ getPendingResourcesForActivity(
								cause.getResourceInfo().getActivityName(), 
								cause.getResourceInfo().getProcessInstanceId()
						  ).size()
						+ " resources to be ready to participate in activity "
						+ result.getResourceInfo().getActivityName() + "...");
			}
		}
		
		public void checkOtherResources()
		{
			if (cause.getOtherResourceInfo().isEmpty())
			{
				LOG.info("All (just one in this case) resources are ready to participate in activity "
						+ cause.getResourceInfo().getActivityName() + "!");
				getBinder().inject(ActivityParticipant.class).notifyResourcesReady(
						(Request) cause);
				done = true;
				// try
				// {
				// getBinder().bind(FinalizerService.class).terminate(this);
				// } catch (Exception e)
				// {
				// LOG.error("Failed to terminate service with id "+getID(),e);
				// }
				return;
			}

			for (ActivityParticipationResourceInformation participator : cause
					.getOtherResourceInfo())
			{
				getPendingResourcesForActivity(
						cause.getResourceInfo().getActivityName(),cause.getResourceInfo().getProcessInstanceId())
						.add(participator);
			}
			for (ActivityParticipationResourceInformation participator : getPendingResourcesForActivity(cause
					.getResourceInfo().getActivityName(), cause.getResourceInfo().getProcessInstanceId()))
			{
				try
				{
					List<ActivityParticipationResourceInformation> others = new ArrayList<ActivityParticipationResourceInformation>();
					for (ActivityParticipationResourceInformation other : cause.getOtherResourceInfo())
						if (!cause.getOtherResourceInfo().equals(participator))
							others.add(other);
					if (!cause.getResourceInfo().equals(participator))
						others.add(cause.getResourceInfo());
					send(ResourceReadyNotification.Request.Builder
							.forProducer(initiator, cause)
							.withReceiverID(participator.getResourceAgent())
							.withResourceInfo(participator)
							.withOtherResourceInfo(others).build());
				} catch (Exception e)
				{
					LOG.error(
							"Failed to wait for resource "
									+ participator.getResourceName()
									+ " in activity "
									+ participator.getActivityName(), e);
				}
			}
		}

		/**
		 * @return
		 */
		public boolean isDone()
		{
			return done;
		}
	}

	
	public synchronized ResouceReadyInitiatorImpl forProducer(ActivityParticipant initiator,
			ActivityParticipation cause)
	{
		ResouceReadyInitiatorHandler handler = getInstance(cause);
		if (handler == null) {
			handler = new ResouceReadyInitiatorHandler(cause, initiator);
			instances.put(cause, handler);
		}
		handler.checkOtherResources();
		return this;
	}

	

	/** @see io.coala.enterprise.role.Initiator#onStated(io.coala.enterprise.fact.CoordinationFact) */
	@Override
	public synchronized void onStated(final ResourceReadyNotification.Result result)
	{
		List<ActivityParticipation> toClean = new ArrayList<ActivityParticipation>();
		for (ActivityParticipation cause : instances.keySet()) {
			ResouceReadyInitiatorHandler handler = getInstance(cause);
			handler.handle(result);
			if (handler.isDone())
				toClean.add(cause);
		}
		for (ActivityParticipation cause : toClean)
			instances.remove(cause);
	}

	@Override
	public void activate()
	{
		setStatus(BasicCapabilityStatus.STARTED);
	}

	@Override
	public void deactivate()
	{
		LOG.info("ResourceReadyInitiator is completed!");
	}

}