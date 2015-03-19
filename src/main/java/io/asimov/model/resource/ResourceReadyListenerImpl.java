package io.asimov.model.resource;

import io.asimov.agent.resource.GenericResourceManagementWorld;
import io.asimov.microservice.negotiation.ResourceReadyNotification;
import io.asimov.microservice.negotiation.ResourceReadyNotification.Request;
import io.asimov.microservice.negotiation.ResourceReadyNotification.ResourceReadyListener;
import io.asimov.model.ActivityParticipation.ActivityParticipant;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.EventType;
import io.coala.bind.Binder;
import io.coala.enterprise.role.AbstractExecutor;
import io.coala.log.InjectLogger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observer;

/**
 * {@link ResourceReadyListenerImpl}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public class ResourceReadyListenerImpl extends
		AbstractExecutor<ResourceReadyNotification.Request> implements
		ResourceReadyListener
{

	/** */
	private static final long serialVersionUID = 1L;

	@InjectLogger
	private Logger LOG;

	/**
	 * {@link ResourceReadyListenerImpl} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected ResourceReadyListenerImpl(Binder binder)
	{
		super(binder);
	}

	@Override
	public void initialize() throws Exception
	{
		super.initialize();

		getBinder().inject(GenericResourceManagementWorld.class).onActivityEvent()
				.subscribe(new Observer<ActivityEvent>()
				{

					@Override
					public void onCompleted()
					{

					}

					@Override
					public void onError(final Throwable e)
					{
						e.printStackTrace();
					}

					@Override
					public void onNext(final ActivityEvent event)
					{
						if (event.getType() == EventType.RESOURCE_READY_FOR_ACTIVITY)
							checkReadiness();
					}
				});
	}

	// @Schedulable(DONE_WALKING)
	// public void doneWalking(final ResourceReadyNotification request)
	// throws Exception
	// {
	// LOG.info("Done walking, ready for participation...");
	// send(ResourceReadyNotification.Result.Builder.forProducer(this,
	// this.cause).build());
	// }

	private Set<Request> pending = Collections
			.synchronizedSet(new HashSet<Request>());
	
	/** @see io.coala.enterprise.role.Executor#onRequested(io.coala.enterprise.fact.CoordinationFact) */
	@Override
	public void onRequested(final Request request)
	{
		this.pending.add(request);
		// TODO Create event based callback mechanism to listen to these events
		try {
			getBinder().inject(GenericResourceManagementWorld.class)
				.performActivityChange(
						request.getResourceInfo().getProcessID(), 
						request.getResourceInfo().getProcessInstanceId(), 
						request.getResourceInfo().getActivityName(), 
						request.getResourceInfo().getActivityInstanceId(), 
						request.getResourceInfo().getResourceName(), 
						EventType.RESOURCE_READY_FOR_ACTIVITY);
		} catch (Exception e) {
			LOG.warn("Failed to fire resource ready event, so calling method directly",e);
			checkReadiness();
		}
	}

	/**
	 * 
	 */
	private void checkReadiness()
	{
		final Set<Request> sent = new HashSet<>();
		synchronized (this.pending)
		{
			for (Request request : this.pending)
				if (getBinder().inject(ActivityParticipant.class)
								.isReadyForActivity(request))
					// if (not occupant or arrived at priority location) confirm
					// ready
					try
					{
						send(ResourceReadyNotification.Result.Builder
								.forProducer(this, request).build());
						sent.add(request);
					} catch (Exception e)
					{
						LOG.error("Failed to claim availibility", e);
					}
			this.pending.removeAll(sent);
		}
		
	}
}
