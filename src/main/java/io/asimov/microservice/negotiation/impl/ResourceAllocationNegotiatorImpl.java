package io.asimov.microservice.negotiation.impl;

import io.asimov.messaging.ASIMOVMessageID;
import io.asimov.microservice.negotiation.AgentServiceProxy;
import io.asimov.microservice.negotiation.ResourceAllocationNegotiator;
import io.asimov.microservice.negotiation.ResourceAllocationRequestor;
import io.asimov.microservice.negotiation.ResourceAllocationRequestor.AllocationCallback;
import io.asimov.microservice.negotiation.messages.AvailabilityCheck;
import io.asimov.microservice.negotiation.messages.AvailabilityReply;
import io.asimov.microservice.negotiation.messages.Claimed;
import io.asimov.model.events.EventType;
import io.asimov.reasoning.sl.SLParsableSerializable;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.capability.interact.ReceivingCapability;
import io.coala.capability.interact.SendingCapability;
import io.coala.log.LogUtil;
import io.coala.message.AbstractMessage;
import io.coala.message.Message;
import io.coala.time.SimTimeFactory;
import io.coala.time.TimeUnit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observable;
import rx.Observer;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

public class ResourceAllocationNegotiatorImpl extends NegotiatingCapability implements
		ResourceAllocationNegotiator
{

	/** */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LogUtil
			.getLogger(ResourceAllocationNegotiatorImpl.class);

	private ResourceAllocationRequestor.AllocationCallback callback;
	private ConversionCallback conversion;
	private Map<Serializable, Set<AgentID>> candidateMap;
	private Map<Serializable, Set<AgentID>> selectedCandidateMap;
	private ResourceAllocationRequestor requestor;
	private boolean failing = false;
	private HashSet<Serializable> requirementsFound = new HashSet<Serializable>();

	@Inject
	public ResourceAllocationNegotiatorImpl(final Binder binder)
	{
		super(binder);
	}


	private void sendMessage(AbstractMessage<ASIMOVMessageID> m) throws Exception
	{
		agentServiceProxy.getBinder().inject(SendingCapability.class).send(m);
	}

	@Override
	public void initialize() throws Exception
	{
		super.initialize();
		getBinder().inject(ReceivingCapability.class).getIncoming()
				.ofType(Claimed.class).subscribe(new Observer<Claimed>()
				{

					@Override
					public void onCompleted()
					{
						LOG.info("All claimed where received");
					}

					@Override
					public void onError(final Throwable t)
					{
						t.printStackTrace();
					}

					@Override
					public void onNext(Claimed args)
					{
						processMessage(args);
					}
				});

		getBinder().inject(ReceivingCapability.class).getIncoming()
				.ofType(AvailabilityReply.class)
				.subscribe(new Observer<AvailabilityReply>()
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
					public void onNext(AvailabilityReply args)
					{
						processMessage(args);
					}
				});
	}

	/** @see io.asimov.negotiation.ResourceAllocationNegotiator#processMessage(io.coala.message.AbstractMessage) */
	@Override
	public void processMessage(
			final Message<?> m)
	{
		if (m instanceof AvailabilityReply)
		{
			AvailabilityReply availabilityReply = (AvailabilityReply) m;
			Serializable requirementsFormula = availabilityReply
					.getRequirements();
			Set<AgentID> aids = candidateMap.get(requirementsFormula);
			aids.remove(availabilityReply.getSenderID());
			if (aids.size() > 0)
				candidateMap.put(requirementsFormula, aids);
			else
				candidateMap.remove(requirementsFormula);
			if (availabilityReply.isAvailable())
			{
				requirementsFound.add(requirementsFormula);
				Set<AgentID> selectedAids = selectedCandidateMap
						.get(availabilityReply.getRequirements());
				if (selectedAids == null)
				{
					selectedAids = new HashSet<AgentID>();
				}
				selectedAids.add(availabilityReply.getSenderID());
				selectedCandidateMap.put(requirementsFormula, selectedAids);
			} else if (candidateMap.get(requirementsFormula) == null)
			{
				if (!requirementsFound.contains(requirementsFormula))
				{
					this.failing = true;
				}
			}
			if (candidateMap.keySet().size() == 0)
				requestAllocations();
		} else if (m instanceof Claimed)
		{
			requestor.handleIncommingClaimed((Claimed) m);
		} else
		{
			LOG.error("Only AvailibilityReplies or Claimed, not "
					+ m.getClass().getSimpleName()
					+ ", can be processed by the negotiator.");
		}
	}
	
	
	

	private void requestAllocations()
	{
		if (failing)
		{
			callback.failure(new HashSet<AgentID>());
			return;
		}
		// Convert requirements to allocation requests
		for (Serializable requirements : selectedCandidateMap.keySet())
		{
			if (selectedCandidateMap.get(requirements).size() == 0)
			{
				callback.failure(new HashSet<AgentID>());
				return;
			}
		}
		HashMap<Serializable, Serializable> qToA = new HashMap<Serializable, Serializable>();
		for (Serializable requirements : selectedCandidateMap.keySet())
		{
			Serializable assertionFormula = conversion.convert(requirements);
			qToA.put(requirements, assertionFormula);
			candidateMap.put(assertionFormula,
					selectedCandidateMap.get(requirements));
		}
		requestor = new ResourceAllocationRequestorImpl(
				(AgentServiceProxy) getAgentServiceProxy());
		requestor.setQueryToAssertionMap(qToA);
		requestor.setCandidateMap(candidateMap);
		requestor.doAllocation(callback);
	}

	/** @see io.asimov.negotiation.ResourceAllocationNegotiator#negotiate() */
	@Override
	public synchronized Observable<AllocationCallback> negotiate(
			final ResourceAllocationRequestor.AllocationCallback _depr_callback,
			Map<Serializable, Set<AgentID>> candidateMap,
			ConversionCallback conversion)
	{
		this.candidateMap = candidateMap;
		this.selectedCandidateMap = new HashMap<Serializable, Set<AgentID>>();
		final Subject<ResourceAllocationRequestor.AllocationCallback,
			ResourceAllocationRequestor.AllocationCallback> callbackSubject = 
			ReplaySubject.create();
		this.callback = 
		new AllocationCallback() {
			
			private Map<AgentID, Serializable> resources;
			private Set<AgentID> failedIds;
			private boolean wasSuccesBoolean = false;

			@Override
			public void done(Map<AgentID, Serializable> resources)
			{
				this.resources = resources;
				for (AgentID allocatedResourceAgentID : resources.keySet()) {
					try {
						performAllocationChange(allocatedResourceAgentID.getValue(), EventType.ALLOCATED);
					} catch (Exception e) {
						LOG.error(getID().getOwnerID()+" failed to emit allocation event for "+allocatedResourceAgentID);
						failing = true;
					}
				}
				if (failing) {
					deAllocate(getScenarioAgentID().getValue());
					failure(resources.keySet());
				} else {
					wasSuccesBoolean = true;
					callbackSubject.onNext(this);
					_depr_callback.done(resources);
					callbackSubject.onCompleted();
				}
			}

			@Override
			public void failure(Set<AgentID> aids)
			{
				this.failedIds = aids;
				callbackSubject.onNext(this);
				_depr_callback.failure(aids);
				callbackSubject.onCompleted();
			}

			@Override
			public void error(Exception error)
			{
 				_depr_callback.error(error);
				callbackSubject.onError(error);
			}

			@Override
			public Map<AgentID, Serializable> getAllocatedResources()
			{
				if (!wasSucces())
					throw new IllegalStateException("Allocated resources are only available after succesfull allocation");
				return resources;
			}

			@Override
			public Set<AgentID> getUnavailabeResourceIDs()
			{
				if (wasSucces())
					throw new IllegalStateException("Failed resources are only available after failed allocation");
				return failedIds;
			}

			@Override
			public boolean wasSucces()
			{
				return wasSuccesBoolean;
			}

			@Override
			public AgentID getScenarioAgentID()
			{
				return _depr_callback.getScenarioAgentID();
			}
			
		};
		this.conversion = conversion;
		// per formula:
		Set<AbstractMessage<ASIMOVMessageID>> messageQueue = new HashSet<AbstractMessage<ASIMOVMessageID>>();

		for (final Serializable requirements : candidateMap.keySet())
		{
			// request availability to all agents.
			for (AgentID aid : candidateMap.get(requirements))
			{
				AvailabilityCheck check = new AvailabilityCheck(
						agentServiceProxy.getBinder()
								.inject(SimTimeFactory.class)
								.create(0, TimeUnit.TICKS), agentServiceProxy
								.getBinder().getID(), aid);
				check.setRequirements(new SLParsableSerializable(requirements
						.toString()));
				messageQueue.add(check);
			}
		}
		for (AbstractMessage<ASIMOVMessageID> m : messageQueue)
		{
			try
			{
				sendMessage(m);
			} catch (Exception e)
			{
				Set<AgentID> aids = new HashSet<AgentID>();
				aids.add(m.getReceiverID());
				LOG.warn(e.getMessage(), e);
				_depr_callback.failure(aids);
			}
		}
		return callbackSubject.last().asObservable();
	}

	/** @see io.asimov.negotiation.ResourceAllocationNegotiator#deAllocate() */
	@Override
	public void deAllocate(final String scenarioAgentID)
	{
		try
		{
			if (requestor != null)
				requestor.deAllocate(scenarioAgentID);
			else 
				LOG.error("Could not de-allocate", new Exception());
		} catch (Exception e)
		{
			callback.error(e);
		}
	}

}
