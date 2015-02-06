package io.asimov.test.sim;

import io.asimov.agent.gui.SemanticAgentGui;
import io.asimov.messaging.ASIMOVBasicAgent;
import io.asimov.messaging.ASIMOVMessage;
import io.coala.agent.AgentID;
import io.coala.agent.BasicAgent;
import io.coala.bind.Binder;
import io.coala.capability.admin.CreatingCapability;
import io.coala.capability.interact.SendingCapability;
import io.coala.lifecycle.ActivationType;
import io.coala.log.InjectLogger;
import io.coala.time.SimTime;
import io.coala.time.SimTimeFactory;
import io.coala.time.TimeUnit;

import javax.inject.Inject;

import org.apache.log4j.Logger;

/**
 * {@link ASIMOVMessageSenderAgent}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class ASIMOVMessageSenderAgent extends ASIMOVBasicAgent
{

	@InjectLogger
	private Logger LOG;

	/** */
	private static final long serialVersionUID = -6762004824573457285L;

	public final SemanticAgentGui myGui;

	private SendingCapability messengerService;

	private AgentID receiverAgentId;

	private static final long NOF_MESSAGES = 10;

	/**
	 * {@link ASIMOVMessageSenderAgent} constructor
	 * 
	 * @param binder
	 * @throws Exception
	 */
	@Inject
	public ASIMOVMessageSenderAgent(final Binder binder)
	{
		super(binder);
		this.myGui = new SemanticAgentGui(getBinder(), null, true, true);
	}

	/** @see BasicAgent#getActivationType() */
	@Override
	public ActivationType getActivationType()
	{
		return ActivationType.ACTIVATE_ONCE;
	}

	@Override
	public void initialize()
	{
		LOG.info("ASIMOVgent is executing.");

		receiverAgentId = new AgentID(getID().getModelID(), "receivingAgent");
		try
		{
			getBinder().inject(CreatingCapability.class).createAgent(receiverAgentId,
					ASIMOVReasoningMessageReceiverAgent.class);
		} catch (Exception e1)
		{
			LOG.error("Failed to boot agent", e1);
		}
		messengerService = getBinder().inject(SendingCapability.class);

		// start roles/handlers here
	}

	@Override
	public void activate()
	{
		for (int i = 0; i < NOF_MESSAGES; i++)
		{
			try
			{
				messengerService.send(new ASIMOVMessage(
						(SimTime) getBinder().inject(SimTimeFactory.class)
								.create(i, TimeUnit.TICKS), getID(),
						receiverAgentId, "(count " + i + ")"));
			} catch (Exception e)
			{
				LOG.error("Failed to send message", e);
			}
		}
		die();
	}

	@Override
	public void finish()
	{
		this.myGui.dispose();
		super.finish();
	}

}
