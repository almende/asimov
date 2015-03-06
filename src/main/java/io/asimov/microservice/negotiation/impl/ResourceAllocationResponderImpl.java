package io.asimov.microservice.negotiation.impl;

import io.asimov.messaging.ASIMOVMessageID;
import io.asimov.microservice.negotiation.AgentServiceProxy;
import io.asimov.microservice.negotiation.ResourceAllocationResponder;
import io.asimov.microservice.negotiation.messages.AvailabilityCheck;
import io.asimov.microservice.negotiation.messages.AvailabilityReply;
import io.asimov.microservice.negotiation.messages.Claim;
import io.asimov.microservice.negotiation.messages.Claimed;
import io.asimov.microservice.negotiation.messages.Proposal;
import io.asimov.microservice.negotiation.messages.ProposalRequest;
import io.asimov.model.ResourceAllocation;
import io.asimov.reasoning.sl.SLParsableSerializable;
import io.coala.bind.Binder;
import io.coala.capability.BasicCapability;
import io.coala.capability.interact.ReceivingCapability;
import io.coala.capability.interact.SendingCapability;
import io.coala.capability.know.ReasoningCapability;
import io.coala.capability.know.ReasoningCapability.Query;
import io.coala.log.LogUtil;
import io.coala.message.AbstractMessage;
import io.coala.message.Message;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observer;

/**
 * 
 * The {@link ResourceAllocationResponderImpl} class that will be used by all
 * resources to respond to negotiations that are performed in instant simulation
 * time.
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 * 
 */
public class ResourceAllocationResponderImpl extends BasicCapability implements
		ResourceAllocationResponder
{
	/** */
	private static final long serialVersionUID = 1L;
	private boolean isClaimed = false;
	private final AgentServiceProxy agentServiceProxy;

	private static final Logger LOG = LogUtil
			.getLogger(ResourceAllocationResponderImpl.class);

	@Inject
	public ResourceAllocationResponderImpl(final Binder binder)
	{
		this(new AgentServiceProxy(binder));
	}

	public ResourceAllocationResponderImpl(final AgentServiceProxy self)
	{
		super(self.getBinder());
		this.agentServiceProxy = self;
	}

	@Override
	public void initialize() throws Exception
	{
		super.initialize();
		
				getBinder().inject(ReceivingCapability.class).getIncoming()
				.ofType(Claim.class).subscribe(new Observer<Claim>()
				{

					@Override
					public void onCompleted()
					{
						LOG.info("All claims where received");
					}

					@Override
					public void onError(final Throwable t)
					{
						t.printStackTrace();
					}

					@Override
					public void onNext(Claim args)
					{
						processMessage(args);
					}
				});

		getBinder().inject(ReceivingCapability.class).getIncoming()
				.ofType(AvailabilityCheck.class)
				.subscribe(new Observer<AvailabilityCheck>()
				{

					@Override
					public void onCompleted()
					{
						LOG.info("All AvailabilityChecks where received");
					}

					@Override
					public void onError(final Throwable t)
					{
						t.printStackTrace();
					}

					@Override
					public void onNext(AvailabilityCheck args)
					{
						processMessage(args);
					}
				});
		
		getBinder().inject(ReceivingCapability.class).getIncoming()
		.ofType(ProposalRequest.class)
		.subscribe(new Observer<ProposalRequest>()
		{

			@Override
			public void onCompleted()
			{
				LOG.info("All ProposalRequests where received");
			}

			@Override
			public void onError(final Throwable t)
			{
				t.printStackTrace();
			}

			@Override
			public void onNext(ProposalRequest args)
			{
				processMessage(args);
			}
		});
	}

	/**
	 * @return the isClaimed
	 */
	private boolean isClaimed()
	{
		return isClaimed;
	}

	/**
	 * @param isClaimed the isClaimed to set
	 */
	private void setClaimed(boolean isClaimed)
	{
		this.isClaimed = isClaimed;
	}

	/** @see eu.a4ee.negotiation.ResourceAllocationResponder#processMessage(io.coala.message.AbstractMessage) */
	@Override
	public void processMessage(
			final Message<?> m)
	{
		AbstractMessage<ASIMOVMessageID> r = null;
		if (m instanceof Claim)
		{
			Claim claim = ((Claim) m);
			if (claim.isDeClaim())
			{
				// ReasonerService<?> reasonerService = agentServiceProxy
				// .getBinder().bind(ReasonerService.class);
				// reasonerService.removeBeliefFromKBase(reasonerService.toBelief(
				// claim.getAssertion(),
				// ResourceAllocation.ALLOCATED_AGENT_AID,
				// agentServiceProxy.getID()));
				reset();
				return;
			} else
			{
				r = processClaim((Claim) m);

			}
		} else if (m instanceof AvailabilityCheck)
		{
			r = (processAvailabilityCheck((AvailabilityCheck) m));
		}
		else if (m instanceof ProposalRequest)
		{
			r = (processProposalRequest((ProposalRequest) m));
		}
		try
		{
			agentServiceProxy.getBinder().inject(SendingCapability.class).send(r);
		} catch (Exception e)
		{
			LOG.error(
					"Could not reach agent with ID "
							+ agentServiceProxy.getID(), e);
		}
	}

	/** @see eu.a4ee.negotiation.ResourceAllocationResponder#processClaim(eu.a4ee.negotiation.messages.Claim) */
	@Override
	public synchronized Claimed processClaim(Claim claim)
	{
		Claimed claimed = new Claimed(claim, claim.getID().getTime(),
				claim.getReceiverID(), claim.getSenderID());
		if (!isClaimed())
		{
			// need te original query and the filled in allocation statement
			// here.
			// first check isAvailable with original query
			claimed.setAvailable(isAvailable(claim.getQuery()));
			// then assert the statement if it is available
			if (claimed.isAvailable())
			{
				LOG.info(claim);
				ReasoningCapability reasonerService = agentServiceProxy
						.getBinder().inject(ReasoningCapability.class);
				reasonerService.addBeliefToKBase(reasonerService.toBelief(
						claim.getAssertion(),
						ResourceAllocation.ALLOCATED_AGENT_AID,
						agentServiceProxy.getID()));
				this.setClaimed(true);
			}
			// needs to declaim afterwards on failure....
		} else
		{
			claimed.available = false;
		}
		return claimed;
	}

	/**
	 * @return the isAvailable
	 */
	private boolean isAvailable(final Serializable requirements)
	{
		ReasoningCapability reasonerService = agentServiceProxy.getBinder()
				.inject(ReasoningCapability.class);
		Query query = reasonerService.toQuery(new SLParsableSerializable(requirements.toString()));
		final CountDownLatch latch = new CountDownLatch(1);
		final CountDownLatch wasTrue = new CountDownLatch(1);
		reasonerService.queryToKBase(query).take(1)
				.subscribe(new Observer<Map<String, Object>>()
				{

					@Override
					public void onCompleted()
					{
						latch.countDown();
					}

					@Override
					public void onError(final Throwable t)
					{
						t.printStackTrace();
					}

					@Override
					public void onNext(Map<String, Object> args)
					{
//						LOG.info(requirements + " returns: " + args);

						if (args != null)
							wasTrue.countDown();
					}
				});
		try
		{
			latch.await();
		} catch (InterruptedException e1)
		{
			LOG.error("Error while waiting for latch.", e1);
			return false;
		}
		return (wasTrue.getCount() < 1);
	}
	

	/**
	 * @return the isAvailable
	 */
	private double getScore(final Serializable requirements)
	{
		ReasoningCapability reasonerService = agentServiceProxy.getBinder()
				.inject(ReasoningCapability.class);
		Query query = reasonerService.toQuery(requirements);
		final Map<Serializable, Double> result = new HashMap<Serializable,Double>();
		final CountDownLatch latch = new CountDownLatch(1);
		final CountDownLatch wasTrue = new CountDownLatch(1);
		reasonerService.queryToKBase(query).take(1)
				.subscribe(new Observer<Map<String, Object>>()
				{

					@Override
					public void onCompleted()
					{
						latch.countDown();
					}

					@Override
					public void onError(final Throwable t)
					{
						t.printStackTrace();
					}

					@Override
					public void onNext(Map<String, Object> args)
					{
//						LOG.info(requirements + " returns: " + args);

						if (args != null) {
							result.put(requirements,Double.valueOf(args.get("score").toString()));
							wasTrue.countDown();
						}
					}
				});
		try
		{
			latch.await();
		} catch (InterruptedException e1)
		{
			LOG.error("Error while waiting for latch.", e1);
			return 0;
		}
		return result.get(requirements);
	}

	/** @see eu.a4ee.negotiation.ResourceAllocationResponder#processAvailabilityCheck(eu.a4ee.negotiation.messages.UnAvailabilityRequest) */
	@Override
	public synchronized AvailabilityReply processAvailabilityCheck(
			AvailabilityCheck availabilityCheck)
	{
		AvailabilityReply reply = new AvailabilityReply(availabilityCheck,
				availabilityCheck.getID().getTime(),
				availabilityCheck.getReceiverID(),
				availabilityCheck.getSenderID());
		reply.setAvailable(isAvailable(availabilityCheck.getRequirements()));
		reply.setRequirements(availabilityCheck.getRequirements());
		return reply;
	}
	
	/** @see eu.a4ee.negotiation.ResourceAllocationResponder#processAvailabilityCheck(eu.a4ee.negotiation.messages.UnAvailabilityRequest) */
	@Override
	public synchronized Proposal processProposalRequest(
			ProposalRequest proposalRequest)
	{
		Proposal reply = new Proposal(proposalRequest,
				proposalRequest.getID().getTime(),
				proposalRequest.getReceiverID(),
				proposalRequest.getSenderID());
		reply.setScore(getScore(proposalRequest.getQuery()));
		return reply;
	}

	/** @see eu.a4ee.negotiation.ResourceAllocationResponder#reset() */
	@Override
	public void reset()
	{
		this.setClaimed(false);
	}

}
