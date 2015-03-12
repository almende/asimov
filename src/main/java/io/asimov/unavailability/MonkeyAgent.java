package io.asimov.unavailability;

import io.arum.model.ARUMOrganization;
import io.asimov.messaging.ASIMOVMessage;
import io.asimov.microservice.negotiation.ResourceAllocationNegotiator;
import io.asimov.microservice.negotiation.ResourceAllocationNegotiator.ConversionCallback;
import io.asimov.microservice.negotiation.ResourceAllocationRequestor.AllocationCallback;
import io.asimov.model.ResourceAllocation;
import io.asimov.model.sl.LegacySLUtil;
import io.asimov.reasoning.sl.SLParsableSerializable;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.capability.admin.DestroyingCapability;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.invoke.ProcedureCall;
import io.coala.invoke.Schedulable;
import io.coala.log.LogUtil;
import io.coala.time.TimeUnit;
import io.coala.time.Trigger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import rx.Observer;
import rx.Subscription;

import com.google.inject.Inject;

/**
 * The {@ref MonkeyAgent} provides unavailability of resources by allocation of
 * resources governed by a unavailability distribution.
 * 
 * @author suki
 *
 */
public class MonkeyAgent extends ARUMOrganization<MonkeyAgentWorld> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6542209399739939980L;

	private static final Logger LOG = LogUtil.getLogger(MonkeyAgent.class);

	private static final String STOP_UNAVAILABLE = "STOP_UNAVAILABLE";
	private static final String RETRY_UNAVAILABLE = "RETRY_UNAVAILABLE";

	private Set<Subscription> recievers = new HashSet<Subscription>();

	private static final String DONE = "DONE";
	
	private int resourcesHash = 0;

	Set<UnAvailabilityRequest> pending = new HashSet<UnAvailabilityRequest>();

	Set<UnAvailabilityRequest> allocated = new HashSet<UnAvailabilityRequest>();

	AgentID scenarioAgentId;

	@Inject
	public MonkeyAgent(Binder binder) {
		super(binder);
	}

	@Override
	public void initialize() throws Exception {
		super.initialize();
		recievers.add(getReceiver().getIncoming()
				.ofType(UnAvailabilityRequest.class)
				.subscribe(new Observer<UnAvailabilityRequest>() {

					@Override
					public void onCompleted() {
						// Nothing special here
					}

					@Override
					public void onError(Throwable e) {
						LOG.error("Failed to receive UnAvailabilityRequest", e);
					}

					@Override
					public void onNext(UnAvailabilityRequest t) {
						LOG.error("Monkey received:" + t.toJSON());
						scenarioAgentId = t.getSenderID();
						pending.add(t);
						negotiateForRequest(t);
					}
				}));
		recievers.add(getReceiver().getIncoming().ofType(ASIMOVMessage.class)
				.subscribe(new Observer<ASIMOVMessage>() {

					@Override
					public void onCompleted() {
						// Nothing special
					}

					@Override
					public void onError(Throwable e) {
						LOG.error(
								"Failed to retry scheduling of unavailability",
								e);
					}

					@Override
					public void onNext(ASIMOVMessage t) {
						if (t.content instanceof String) {
							String content = (String) t.content;
							if (content.startsWith("AvailableResourcesChange:")) {
								int newResourcesHash = Integer.valueOf(content.replace("AvailableResourcesChange:", "")).intValue();
								if (resourcesHash != newResourcesHash) {
									resourcesHash = newResourcesHash;
								} else {
									return;
								}
								for (UnAvailabilityRequest r : pending) {
									getScheduler()
											.schedule(
													ProcedureCall.create(
															MonkeyAgent.this,
															MonkeyAgent.this,
															RETRY_UNAVAILABLE,
															r),
													Trigger.createAbsolute(getBinder()
															.inject(ReplicatingCapability.class)
															.getTime()));
								}
							}
						}
					}
				}));
	}

	@Schedulable(RETRY_UNAVAILABLE)
	protected void negotiateForRequest(UnAvailabilityRequest t) {
		Map<Serializable, Set<AgentID>> candidates = new HashMap<Serializable, Set<AgentID>>();
		Set<AgentID> agents = new HashSet<AgentID>();
		agents.add(t.getUnavailableResource());
		candidates.put(
				new SLParsableSerializable(LegacySLUtil
						.requestResourceAllocationForRequirement(
								t.getResourceType(), t.getResourceSubType())
						.toString()), agents);
		negotiate(candidates);
	}

	public void negotiate(Map<Serializable, Set<AgentID>> candidates) {
		LOG.trace("Monkey negotiates");
		getBinder()
				.inject(ResourceAllocationNegotiator.class)
				.negotiate(getAllocCallback(scenarioAgentId), candidates,
						new ConversionCallback() {
							@Override
							public Serializable convert(Serializable forumla) {
								return LegacySLUtil.convertAgentID(
										getOwnerID(), forumla);
							}
						}).subscribe(new Observer<AllocationCallback>() {

					@Override
					public void onNext(final AllocationCallback allocationResult) {
						if (allocationResult.wasSucces()) {
							final Map<AgentID, Serializable> resources = allocationResult
									.getAllocatedResources();
							for (AgentID aid : resources.keySet()) {
								SLParsableSerializable f = new SLParsableSerializable(
										resources.get(aid).toString());
								getReasoner()
										.addBeliefToKBase(
												getReasoner()
														.toBelief(
																f,
																ResourceAllocation.ALLOCATED_AGENT_AID,
																aid));
								
								try {
									getMessenger()
											.send(new ASIMOVMessage(
													getBinder()
															.inject(ReplicatingCapability.class)
															.getTime(),
													getID(), scenarioAgentId,
													"AvailableResourcesChange:"
															+ getTime()
																	.hashCode()));
								} catch (Exception e1) {
									LOG.error(
											"Failed to send resource change notification",
											e1);
								}
								startUnavailabilityForAgentWithId(aid);
							}
						}
					}

					@Override
					public void onCompleted() {
						// nothing special
					}

					@Override
					public void onError(final Throwable e) {
						e.printStackTrace();
					}
				});
	}

	protected void startUnavailabilityForAgentWithId(AgentID aid) {
		LOG.info(aid + " is unavailable.");
		for (UnAvailabilityRequest r : pending) {
			if (r.getUnavailableResource().equals(aid)) {
				pending.remove(r);
				allocated.add(r);
				getScheduler()
						.schedule(
								ProcedureCall.create(this, this,
										STOP_UNAVAILABLE, aid),
								Trigger.createAbsolute(getBinder()
										.inject(ReplicatingCapability.class)
										.getTime()
										.plus(r.getUnavailablePeriod())));
				break;
			}
		}

	}

	@Schedulable(STOP_UNAVAILABLE)
	protected void finishUnavailabilityForAgentWithId(AgentID aid) {
		LOG.info(aid + " is available again.");
		for (UnAvailabilityRequest r : allocated) {
			if (r.getUnavailableResource().equals(aid)) {
				allocated.remove(r);
				getBinder().inject(ResourceAllocationNegotiator.class)
						.deAllocate(scenarioAgentId.toString());
			}
		}
		try {
			getMessenger().send(
					new ASIMOVMessage(getBinder().inject(
							ReplicatingCapability.class).getTime(), getID(),
							scenarioAgentId, "AvailableResourcesChange:"
									+ getTime().hashCode()));
		} catch (Exception e1) {
			LOG.error("Failed to send resource change notification", e1);
		}
		getScheduler().schedule(
				ProcedureCall.create(this, this, DONE),
				Trigger.createAbsolute(getBinder().inject(
						ReplicatingCapability.class).getTime().plus(1,TimeUnit.MILLIS)));
	}

	@Schedulable(DONE)
	public void done() {
		for (Subscription s : recievers) {
			s.unsubscribe();
		}
		recievers.clear();
		 try {
		 getBinder().inject(DestroyingCapability.class).destroy();
		 } catch (Exception e) {
		 LOG.error("Failed to kill monkey", e);
		 }
	}

	private final AllocationCallback getAllocCallback(final AgentID scenario) {
		return new AllocationCallback() {

			private boolean wasSucces = false;

			private Set<AgentID> failedAids;

			private Map<AgentID, Serializable> resources;

			@Override
			public void failure(Set<AgentID> aids) {
				this.failedAids = aids;
			}

			@Override
			public void error(final Exception error) {
				LOG.error(error.getMessage(), error);
			}

			@Override
			public void done(Map<AgentID, Serializable> resources) {
				wasSucces = true;
				this.resources = resources;
			}

			@Override
			public Map<AgentID, Serializable> getAllocatedResources() {
				return this.resources;
			}

			@Override
			public Set<AgentID> getUnavailabeResourceIDs() {
				return failedAids;
			}

			@Override
			public boolean wasSucces() {
				return wasSucces;
			}

			@Override
			public AgentID getScenarioAgentID() {
				return scenario;
			}
		};
	}

}
