package io.asimov.messaging;

import io.coala.agent.AgentID;
import io.coala.agent.BasicAgent;
import io.coala.bind.Binder;
import io.coala.capability.interact.ReceivingCapability;
import io.coala.lifecycle.ActivationType;
import io.coala.log.InjectLogger;
import io.coala.log.LogUtil;
import io.coala.message.Message;
import io.coala.model.ModelComponent;
import io.coala.time.SimTime;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.functions.Action1;

/**
 * {@link A4EEBasicAgent}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 * 
 * @param <THIS>
 */
public abstract class A4EEBasicAgent extends BasicAgent implements ModelComponent<AgentID>
{

	// sub-types: resource (equipment, room, occupant, etc.)

	/** */
	private static final long serialVersionUID = -8697203058486189448L;

	@InjectLogger
	private Logger LOG;

	@Inject
	protected A4EEBasicAgent(final Binder binder)
	{
		super(binder);
	}

	@Override
	public void initialize()
	{
		if (LOG == null)
		{
			LOG = LogUtil.getLogger(A4EEBasicAgent.class, this);

			new NullPointerException("LOG was not injected??")
					.printStackTrace();
		}
		LOG.info("A4EEAgent is initializing.");

		getBinder().inject(ReceivingCapability.class).getIncoming()
				.subscribe(new Action1<Message<?>>()
				{
					@Override
					public void call(Message<?> m)
					{
						processMessage(m);
					}
				});
		// initialize roles here
	}

	protected void processMessage(Message<?> m)
	{
		throw new IllegalStateException(
				"processMessage not yet implemented! Failed to process message:"
						+ m);
	}

	/** @see BasicAgent#getActivationType() */
	@Override
	public ActivationType getActivationType()
	{
		return ActivationType.ACTIVATE_ONCE;
	}

	@Override
	public void finish()
	{
		if (LOG != null)
			LOG.info("A4EEAgent is finalizing.");
	}

	/** @see ModelComponent#getOwnerID() */
	@Override
	public AgentID getOwnerID()
	{
		return getID();
	}

	/** @see ModelComponent#getTime() */
	@Override
	public SimTime getTime()
	{
		return getSimulator().getTime();
	}
}
