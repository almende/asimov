package io.asimov.test.sim;

import io.asimov.agent.gui.SemanticAgentGui;
import io.asimov.messaging.ASIMOVBasicAgent;
import io.asimov.messaging.ASIMOVMessage;
import io.asimov.reasoning.sl.SLParsable;
import io.coala.agent.BasicAgent;
import io.coala.bind.Binder;
import io.coala.capability.interact.ReceivingCapability;
import io.coala.capability.know.ReasoningCapability;
import io.coala.capability.know.ReasoningCapability.Query;
import io.coala.lifecycle.ActivationType;
import io.coala.log.InjectLogger;

import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observer;
import rx.Subscription;

/**
 * {@link ASIMOVReasoningMessageReceiverAgent}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class ASIMOVReasoningMessageReceiverAgent extends ASIMOVBasicAgent
{

	@InjectLogger
	private Logger LOG;

	/** */
	private static final long serialVersionUID = -6762004824573457285L;

	public final SemanticAgentGui myGui;

	private ReasoningCapability reasonerService;

	private Subscription querySubscription;

	private static final long NOF_MESSAGES = 10;

	private static class SLParsableMessageBelief implements SLParsable
	{
		private static final String GOT_ASIMOV_MESSAGE_BELEIVE_STRING = "(B ??agent (message ??sender ??content ??time))";

		@Override
		public String toString()
		{
			return GOT_ASIMOV_MESSAGE_BELEIVE_STRING;
		}

	}

	private static class SLParsableMessageCountQuery implements SLParsable
	{
		private static final String ALL_MESSAGES_RECEIVED_QUERY_STRING = "(and (message ??sender ??content ??time) (card (some ?a (message ??sender (count ?a) ??time)) "
				+ NOF_MESSAGES + ")))";

		@Override
		public String toString()
		{
			return ALL_MESSAGES_RECEIVED_QUERY_STRING;
		}
	}

	private static final SLParsableMessageBelief GOT_ASIMOV_MESSAGE_BELEIVE = new SLParsableMessageBelief();
	private static final SLParsableMessageCountQuery ALL_MESSAGES_RECEIVED_QUERY = new SLParsableMessageCountQuery();

	/**
	 * {@link ASIMOVReasoningMessageReceiverAgent} constructor
	 * 
	 * @param binder
	 * @throws Exception
	 */
	@Inject
	public ASIMOVReasoningMessageReceiverAgent(final Binder binder)
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
	protected synchronized ReasoningCapability getReasoner()
	{
		if (this.reasonerService == null)
		{
			this.reasonerService = getBinder().inject(ReasoningCapability.class);
			try
			{
				final Query query = this.reasonerService
						.toQuery(ALL_MESSAGES_RECEIVED_QUERY);
				this.querySubscription = this.reasonerService.queryToKBase(
						query).subscribe(

				new Observer<Map<String, Object>>()
				{

					@Override
					public void onCompleted()
					{
						LOG.info("Done!");
						die();
					}

					@Override
					public void onError(final Throwable t)
					{
						t.printStackTrace();
					}

					@Override
					public void onNext(Map<String, Object> args)
					{
						if (args == null)
							return;
						LOG.info("Received query match on: " + query);
					}
				});
			} catch (Exception e)
			{
				LOG.error("Error in subscription to query.", e);
			}
		}
		return this.reasonerService;
	}

	@Override
	public void initialize()
	{
		LOG.info("ASIMOVMessageReceiverAgent is initializing.");
		getBinder().inject(ReceivingCapability.class).getIncoming()
				.ofType(ASIMOVMessage.class)
				.subscribe(new Observer<ASIMOVMessage>()
				{
					@Override
					public void onNext(final ASIMOVMessage msg)
					{
						processASIMOVMessage(msg);
					}

					@Override
					public void onCompleted()
					{

					}

					@Override
					public void onError(Throwable t)
					{
						t.printStackTrace();
					}
				});
	}

	protected void processASIMOVMessage(final ASIMOVMessage message)
	{
		getReasoner().addBeliefToKBase(
				getReasoner().toBelief(
						GOT_ASIMOV_MESSAGE_BELEIVE,
						"agent",
						getReasoner().toBelief(getID()),
						"time",
						getReasoner().toBelief(
								Long.valueOf(System.currentTimeMillis())),
						"sender",
						getReasoner().toBelief(message.getSenderID()),
						"content", getReasoner().toBelief(new SLParsable()
						{
							public String toString()
							{
								return message.content.toString();
							}
						})));
	}

	@Override
	public void finish()
	{
		this.myGui.dispose();
		if (this.querySubscription != null)
			this.querySubscription.unsubscribe();
		super.finish();
	}
}
