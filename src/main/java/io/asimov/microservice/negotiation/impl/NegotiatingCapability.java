package io.asimov.microservice.negotiation.impl;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import rx.Observer;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;
import io.asimov.db.Datasource;
import io.asimov.microservice.negotiation.AgentServiceProxy;
import io.asimov.model.TraceService;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.Event;
import io.asimov.model.events.EventType;
import io.coala.bind.Binder;
import io.coala.capability.BasicCapability;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.log.LogUtil;
import io.coala.time.SimTime;

public class NegotiatingCapability extends BasicCapability {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2911404735178698550L;
	
	private static Logger LOG = LogUtil.getLogger(NegotiatingCapability.class);
	
	protected final AgentServiceProxy agentServiceProxy;

	private Subject<ActivityEvent, ActivityEvent> allocation = PublishSubject
			.create();


	protected NegotiatingCapability(Binder binder) {
		this(new AgentServiceProxy(binder));
	}


	public NegotiatingCapability(AgentServiceProxy agent)
	{
		super(agent.getBinder());
		this.agentServiceProxy = agent;
	}

	protected AgentServiceProxy getAgentServiceProxy()
	{
		return agentServiceProxy;
	}
	

	public void performAllocationChange(final String resourceName,
			final EventType eventType) throws Exception {
		LOG.info("fire ASIMOV resource unavailability event!");
		if (eventType.equals(EventType.ALLOCATED) || eventType.equals(EventType.DEALLOCATED))
			fireAndForget(eventType, Collections.singletonList(resourceName),
				this.allocation);
		else
			LOG.error("Unsupported event type for (de-)allocation");
	}

	@SuppressWarnings("unchecked")
	protected <T extends Event<?>> void fireAndForget(
			final EventType eventType, final List<String> involvedResources,
			final Observer<T> publisher) {
		final SimTime now = getBinder().inject(ReplicatingCapability.class)
				.getTime();
		publisher.onNext((T) TraceService.getInstance(
				getAgentServiceProxy().getID().getModelID().getValue()).saveEvent(
				getBinder().inject(Datasource.class), null, null, null, null,
				involvedResources, eventType, now));
	}
}
