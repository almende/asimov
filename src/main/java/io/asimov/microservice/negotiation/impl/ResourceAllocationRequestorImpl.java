package io.asimov.microservice.negotiation.impl;

import io.asimov.agent.process.NonSkeletonActivityCapability;
import io.asimov.agent.process.NonSkeletonActivityCapability.NonSkeletonActivityState;
import io.asimov.messaging.ASIMOVMessage;
import io.asimov.messaging.ASIMOVMessageID;
import io.asimov.microservice.negotiation.AgentServiceProxy;
import io.asimov.microservice.negotiation.ClaimSortByProposal;
import io.asimov.microservice.negotiation.ConversionCallback;
import io.asimov.microservice.negotiation.ResourceAllocationRequestor;
import io.asimov.microservice.negotiation.messages.Claim;
import io.asimov.microservice.negotiation.messages.Claimed;
import io.asimov.microservice.negotiation.messages.Proposal;
import io.asimov.model.ResourceAllocation;
import io.asimov.model.events.EventType;
import io.asimov.model.sl.LegacySLUtil;
import io.asimov.reasoning.sl.SLParsableSerializable;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.capability.interact.SendingCapability;
import io.coala.capability.know.ReasoningCapability;
import io.coala.capability.know.ReasoningCapability.Belief;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.log.LogUtil;
import io.coala.message.AbstractMessage;
import io.coala.time.SimTimeFactory;
import io.coala.time.TimeUnit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observer;

/**
 * 
 * The {@link ResourceAllocationRequestorImpl} helps requesting resource
 * allocations in instant simulation time.
 * 
 * @date $Date: 2014-09-23 16:13:57 +0200 (di, 23 sep 2014) $
 * @version $Revision: 1074 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 * 
 */

public class ResourceAllocationRequestorImpl extends NegotiatingCapability implements
		ResourceAllocationRequestor
{

	/** */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LogUtil
			.getLogger(ResourceAllocationRequestorImpl.class);

	private String scenarioAgentID = "testScenario";
	private final AgentServiceProxy agentServiceProxy;
	private Map<AgentID, Serializable> resourcesAllocationMap = null;
	private Map<Serializable, Set<AgentID>> candidateMap = null;
	private AllocationCallback callback;
	private Map<Serializable, Claim> requestedClaims = null;
	private Set<AgentID> failedClaims = null;
	private boolean failing = false;
	private Map<Serializable, Serializable> queryToAssertionMap;
	private static Object lock = new Object();

	//private Subject<AllocationCallback,AllocationCallback> callbackSubject;

	private boolean allocated = false;

	@Inject
	public ResourceAllocationRequestorImpl(final Binder binder)
	{
		
		this(new AgentServiceProxy(binder));
	}

	public ResourceAllocationRequestorImpl(AgentServiceProxy self)
	{
		super(self.getBinder());
		this.agentServiceProxy = self;
		resetFailedClaims();
	}

	/** @see io.asimov.negotiation.ResourceAllocationRequestor#resetFailedClaims() */
	@Override
	public void resetFailedClaims()
	{
		failedClaims = new HashSet<AgentID>();
	}

	/**
	 * @return the agent
	 */
	private Object getAgent()
	{
		return agentServiceProxy;
	}

	/**
	 * @return the candidateMap
	 */
	private Map<Serializable, Set<AgentID>> getCandidateMap()
	{
		return candidateMap;
	}

	/** @see io.asimov.negotiation.ResourceAllocationRequestor#setCandidateMap(java.util.Map) */
	@Override
	public void setCandidateMap(Map<Serializable, Set<AgentID>> candidateMap)
	{
		this.candidateMap = candidateMap;
	}

	/**
	 * @return the callback
	 */
	private AllocationCallback getCallback()
	{
		return callback;
	}

	/**
	 * @param callback the callback to set
	 */
	private void setCallback(AllocationCallback callback)
	{
		this.callback = callback;
	}

	private synchronized Map<AgentID, Serializable> getResourceAllocationMap()
	{
		if (resourcesAllocationMap == null)
			resourcesAllocationMap = new HashMap<AgentID, Serializable>();
		return resourcesAllocationMap;
	}

	/** @see io.asimov.negotiation.ResourceAllocationRequestor#handleIncommingClaimed(io.asimov.negotiation.messages.Claimed) */
	@Override
	public synchronized void handleIncommingClaimed(Claimed claimed)
	{
		if (!claimed.isAvailable())
		{
			for (Entry<Serializable, Claim> entry : requestedClaims.entrySet())
			{
				if (entry.getValue().getID().equals(claimed.getReplyToId()))
					;
				{
					failedClaims.add(entry.getValue().getReceiverID());
					requestedClaims.remove(entry.getKey());
					try
					{
						// LOG.info("MAP:"+getQueryToAssertionMap());
						// LOG.info("KEY"+entry.getKey());
						Serializable formula = getQueryToAssertionMap().get(
								entry.getKey().toString());
						if (formula != null)
						{
							tryAllocationForCandidates(formula);
						} else
						{
							fail(scenarioAgentID);
						}
					} catch (Exception e)
					{
						LOG.error("Failed to allocate resource", e);
						fail(scenarioAgentID);
					}
					break;
				}
			}
		} else
		{
			for (Entry<Serializable, Claim> entry : requestedClaims.entrySet())
			{
				if (entry.getValue().getID().equals(claimed.getReplyToId()))
				{
					getResourceAllocationMap().put(
							entry.getValue().getReceiverID(), entry.getKey());
					if (failing)
						try
						{
							deAllocate(scenarioAgentID);
						} catch (Exception e)
						{
							LOG.error("Allocation failed, deallocating now", e);
							fail(scenarioAgentID);
						}
					else if (getCandidateMap().keySet().size() == getResourceAllocationMap()
							.keySet().size())
					{
						try
						{
							allocate();
						} catch (Exception e)
						{
							LOG.error("Allocation failed, deallocating now", e);
							fail(scenarioAgentID);
						}

					}
					break;
				}
			}
		}
	}

	private void tryAllocationForCandidates(Serializable q) throws Exception
	{
		synchronized (lock) {
		Serializable f = getQueryToAssertionMap().get(q);
		final HashSet<AgentID> candidates = (HashSet<AgentID>) candidateMap.get(f);
		if (candidates.size() == 0)
		{
			fail(scenarioAgentID);
			return;
		} else
		{
			List<Claim> claimCandidates = new ArrayList<Claim>();
			for (AgentID aid : candidates) {
				Claim claim;
				claim = new Claim(null, ((AgentServiceProxy) getAgent()).getID(),
						aid);
				claim.setQuery(q);
				claim.setAssertion(f);
				claimCandidates.add(claim);
			}
			final CountDownLatch wait = new CountDownLatch(1);
			getBinder().inject(ClaimSortByProposal.class)
			.sort(claimCandidates, new ConversionCallback()
			{
				
				@Override
				public Serializable convert(Serializable f)
				{
					// we return a query that contains a key called score that has as a numeric value
					// FIXME converter and comparator should be external.
					return new SLParsableSerializable(LegacySLUtil.countAllocationScore("score").toString());
				}
			}, new Comparator<Proposal>()
			{
				
				@Override
				public int compare(Proposal o1, Proposal o2)
				{
					return Double.compare(o1.getScore(), o2.getScore());
				}
			}).take(1).subscribe(new Observer<Claim>()
			{

				@Override
				public void onCompleted()
				{
					// Nothing special
					wait.countDown();
				}

				@Override
				public void onError(Throwable e)
				{
					LOG.error("An error occured while determining suitability for resoruce",e);
					wait.countDown();
				}

				@Override
				public void onNext(Claim claim)
				{
					candidates.remove(claim.getReceiverID());
					requestedClaims.put(claim.getAssertion(), claim);
					try
					{
						sendMessage(claim);
					} catch (Exception e)
					{
						LOG.error("Failed to send claim: "+claim);
					}
				}
			});
			while (wait.getCount() > 0) {
				wait.await(1000,java.util.concurrent.TimeUnit.MILLISECONDS);
				if (wait.getCount() > 0)
					LOG.info("Waiting for response of best candidate");
			}
		}
		}
	}
	

	/** @see io.asimov.negotiation.ResourceAllocationRequestor#doAllocation(io.asimov.negotiation.ResourceAllocationRequestorOld.AllocationCallback) */
	@Override
	public void doAllocation(final AllocationCallback callback)
	{
		if (resourcesAllocationMap != null)
			throw new IllegalStateException(
					"Resource allocation already performed, please start a new instance of the ResourceAllocactionRequestor.");
		if (candidateMap == null)
			throw new IllegalStateException(
					"You need to first add candidates per resource type.");
		requestedClaims = new HashMap<Serializable, Claim>();
		
		this.setCallback(callback);
		
		for (Serializable resourceFormula : candidateMap.keySet())
		{
			try
			{
				Serializable qF = null;
				for (Serializable queryFormula : getQueryToAssertionMap()
						.keySet())
				{
					if (getQueryToAssertionMap().get(queryFormula).equals(
							resourceFormula))
					{
						qF = queryFormula;
						break;
					}
				}
				tryAllocationForCandidates(qF);
			} catch (Exception e)
			{
				LOG.error(e.getMessage(),e);
				fail(scenarioAgentID);
			}
		}
	}

	private void fail(final String scenarioAgentID)
	{
		failing = true;
		if (failedClaims.size() == 0 && !allocated)
				callback.error(new Exception("Fail was invoked whil no claims where failed."));
		try
		{
			deAllocate(scenarioAgentID);
		} catch (Exception e)
		{
			LOG.error("De-allocation failed due to an Exception", e);
		}
	}
	private void allocate() throws Exception
	{
		for (Entry<AgentID, Serializable> allocation : resourcesAllocationMap
				.entrySet())
		{
			AgentID aid = allocation.getKey();
			if (getAgent() instanceof AgentServiceProxy)
			{
				ReasoningCapability reasonerService = ((AgentServiceProxy) getAgent())
						.getBinder().inject(ReasoningCapability.class);
				Belief belief = reasonerService.toBelief(allocation.getValue(),
						ResourceAllocation.ALLOCATED_AGENT_AID, aid);
				ASIMOVMessage informMessage = new ASIMOVMessage(
						((AgentServiceProxy) getAgent()).getBinder()
								.inject(SimTimeFactory.class)
								.create(0, TimeUnit.TICKS),
						((AgentServiceProxy) getAgent()).getID(), aid, new SLParsableSerializable(belief.toString()));
				agentServiceProxy.getBinder().inject(SendingCapability.class)
						.send(informMessage);
			}
			Claim claim = requestedClaims.get(allocation.getValue());
			if (claim == null)
				continue;
			claim.setDeClaim(true);
			requestedClaims.remove(allocation.getValue());
			sendMessage(claim);
		}
		if (!resourcesAllocationMap.isEmpty()) {
			allocated  = true;
			getCallback().done(resourcesAllocationMap);
		}
	}

	private void sendMessage(AbstractMessage<ASIMOVMessageID> m) throws Exception
	{
		agentServiceProxy.getBinder().inject(SendingCapability.class).send(m);
	}

	/** @see io.asimov.negotiation.ResourceAllocationRequestor#deAllocate() */
	@Override
	public synchronized void deAllocate(final String scenarioAgentID) throws Exception
	{
		if (!allocated) {
			LOG.error("Allocation failed, de-allocating now: "+this.failedClaims);
			final ReasoningCapability reasonerService = ((AgentServiceProxy) getAgent())
					.getBinder().inject(ReasoningCapability.class);
		
			for (Claim claim : this.requestedClaims.values()) {
				if (claim == null)
					return;
				final AgentID aid = claim.getReceiverID();
				Belief belief = 
						reasonerService.toBelief(
						claim.getAssertion(),
						ResourceAllocation.ALLOCATED_AGENT_AID, aid).negate(); 
				ASIMOVMessage informMessage = new ASIMOVMessage(
						((AgentServiceProxy) getAgent()).getBinder()
								.inject(ReplicatingCapability.class).getTime(),
						((AgentServiceProxy) getAgent()).getID(), aid, new SLParsableSerializable(belief.toString()));
				try
				{
					agentServiceProxy.getBinder().inject(SendingCapability.class)
							.send(informMessage);
				} catch (Exception e)
				{
					LOG.error("Failed to inform de-allocation.",e);
				}
				claim.setDeClaim(true);
				try
				{
					sendMessage(claim);
				} catch (Exception e)
				{
					LOG.error("Failed to send claim.",e);
				}
			}
			this.requestedClaims.clear();
		}
		else
			LOG.info("De-allocation allocated resources");
		
		for (final Entry<AgentID, Serializable> allocation : getResourceAllocationMap()
				.entrySet())
		{
			final AgentID aid = allocation.getKey();
			final ReasoningCapability reasonerService = ((AgentServiceProxy) getAgent())
					.getBinder().inject(ReasoningCapability.class);
			getBinder().inject(NonSkeletonActivityCapability.class)
			.call(NonSkeletonActivityCapability.LEAVE_SITE_WHEN_ON_IT,scenarioAgentID, aid.getValue())
			.subscribe(new Observer<NonSkeletonActivityState>(){

				@Override
				public void onCompleted()
				{
					Belief belief = 
							reasonerService.toBelief(
							allocation.getValue(),
							ResourceAllocation.ALLOCATED_AGENT_AID, aid).negate(); 
					ASIMOVMessage informMessage = new ASIMOVMessage(
							((AgentServiceProxy) getAgent()).getBinder()
									.inject(ReplicatingCapability.class).getTime(),
							((AgentServiceProxy) getAgent()).getID(), aid, new SLParsableSerializable(belief.toString()));
					try
					{
						agentServiceProxy.getBinder().inject(SendingCapability.class)
								.send(informMessage);
					} catch (Exception e)
					{
						LOG.error("Failed to inform de-allocation.",e);
					}
					try {
						performAllocationChange(aid.getValue(), EventType.DEALLOCATED);
					} catch (Exception e) {
						LOG.error("Failed to emit deallocation change event for "+aid,e);
					}
					LOG.info("De-allocated "+allocation.getValue().toString());
					Claim claim = requestedClaims.get(allocation.getValue());
					if (claim == null)
						return;
					claim.setDeClaim(true);
					requestedClaims.remove(allocation.getValue());
					LOG.info("De-claimed "+allocation.getValue().toString());
					try
					{
						sendMessage(claim);
					} catch (Exception e)
					{
						LOG.error("Failed to send claim.",e);
					}
				}

				@Override
				public void onError(Throwable e)
				{
					LOG.error(e.getMessage(),e);
				}

				@Override
				public void onNext(NonSkeletonActivityState t)
				{
					;// nothing special here
				}});
		}
		if (failedClaims.size() > 0 && !allocated)
			callback.failure(failedClaims);
		resetFailedClaims();
	}

	/** @see io.asimov.negotiation.ResourceAllocationRequestor#getQueryToAssertionMap() */
	@Override
	public synchronized Map<Serializable, Serializable> getQueryToAssertionMap()
	{
		return queryToAssertionMap;
	}

	/** @see io.asimov.negotiation.ResourceAllocationRequestor#setQueryToAssertionMap(java.util.Map) */
	@Override
	public synchronized void setQueryToAssertionMap(
			Map<Serializable, Serializable> queryToAssertionMap)
	{
		this.queryToAssertionMap = queryToAssertionMap;
	}

}
