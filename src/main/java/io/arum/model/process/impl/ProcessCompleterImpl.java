package io.arum.model.process.impl;

import io.arum.model.resource.ARUMResourceType;
import io.arum.model.resource.assemblyline.AssemblyLine;
import io.arum.model.resource.person.Person;
import io.arum.model.resource.supply.Material;
import io.asimov.agent.process.ManageProcessActionService;
import io.asimov.agent.process.ProcessCompletion;
import io.asimov.agent.process.ProcessCompletion.ProcessCompleter;
import io.asimov.agent.process.ProcessManagementWorld;
import io.asimov.db.Datasource;
import io.asimov.messaging.ASIMOVMessage;
import io.asimov.microservice.negotiation.ResourceAllocationNegotiator;
import io.asimov.microservice.negotiation.ResourceAllocationNegotiator.ConversionCallback;
import io.asimov.microservice.negotiation.ResourceAllocationRequestor.AllocationCallback;
import io.asimov.model.AbstractEmbodied;
import io.asimov.model.AbstractNamed;
import io.asimov.model.ActivityParticipation;
import io.asimov.model.ActivityParticipation.Result;
import io.asimov.model.Resource;
import io.asimov.model.ResourceAllocation;
import io.asimov.model.ResourceRequirement;
import io.asimov.model.process.Task;
import io.asimov.model.sl.ASIMOVFormula;
import io.asimov.model.sl.ASIMOVTerm;
import io.asimov.model.sl.LegacySLUtil;
import io.asimov.model.sl.SLConvertible;
import io.asimov.reasoning.sl.SLParsableSerializable;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.capability.admin.DestroyingCapability;
import io.coala.capability.embody.Percept;
import io.coala.capability.know.ReasoningCapability.Belief;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.enterprise.fact.CoordinationFact;
import io.coala.enterprise.role.AbstractExecutor;
import io.coala.enterprise.role.Executor;
import io.coala.event.grant.Paced;
import io.coala.invoke.ProcedureCall;
import io.coala.invoke.Schedulable;
import io.coala.log.InjectLogger;
import io.coala.log.LogUtil;
import io.coala.model.ModelID;
import io.coala.time.SimTime;
import io.coala.time.SimTimeFactory;
import io.coala.time.TimeUnit;
import io.coala.time.Trigger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.almende.util.uuid.UUID;

import rx.Observer;

/**
 * {@link ProcessCompleterImpl}
 * 
 * @version $Revision: 1074 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class ProcessCompleterImpl extends
		AbstractExecutor<ProcessCompletion.Request> implements
		ProcessCompleter, Paced {

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	@InjectLogger
	private Logger LOG;

	// private DistanceMatrixService distanceMatrix;

	/**
	 * {@link ProcessCompleterImpl} constructor
	 * 
	 * @param binder
	 */
	@Inject
	private ProcessCompleterImpl(final Binder binder) {
		super(binder);
		// this.lookup = new DirectoryLookupInitiatorImpl(binder);
	}

	/** @return the agent's local {@link ProcessManagementWorld} */
	// @Override
	protected ProcessManagementWorld getWorld() {
		return getBinder().inject(ProcessManagementWorld.class);
	};

	@Override
	public void initialize() throws Exception {
		super.initialize();
		if (LOG == null)
			LOG = LogUtil.getLogger(getClass());

		// TODO notify scenario replicator
		// getBinder().bind(ProcessGeneratorService.class)
		// .notifyProcessInitializing(getOwnerID().getValue());
	}

	// private FactID factId;

	// private Request cause;

	private static final String ADD_PERCEPT_TO_KB_METHOD_ID = "doAddPerceptToKB";

	@Schedulable(ADD_PERCEPT_TO_KB_METHOD_ID)
	protected void doAddPerceptToKB(final Percept percept) {
		LOG.info("ADDING percept: " + percept);
		getReasoner().addBeliefToKBase(getReasoner().toBelief(percept));
	}

	/** @see Executor#onRequested(CoordinationFact) */
	@Override
	public void onRequested(final ProcessCompletion.Request request) {
		// disabled
		// if (distanceMatrix == null) {
		// distanceMatrix = getBinder().inject(DistanceMatrixService.class);
		// distanceMatrix.generateDistanceMatrix(request.getSenderID());
		// }
		// -- end disabled

		// this.processTypeId = request.getProcessTypeID();
		// setFactId(request.getID());
		// setCause((Request) request);

		final SimTime now = getTime();

		getWorld().makeObservation(request.getProcessTypeID()).subscribe(
				new Observer<Percept>() {

					@Override
					public void onError(final Throwable e) {
						e.printStackTrace();
						LOG.error("Problem while observing percepts", e);
					}

					@Override
					public void onNext(final Percept percept) {
						final ProcedureCall<?> pc = ProcedureCall.create(
								ProcessCompleterImpl.this,
								ProcessCompleterImpl.this,
								ADD_PERCEPT_TO_KB_METHOD_ID, percept);
						getSimulator()
								.schedule(pc, Trigger.createAbsolute(now));
					}

					@Override
					public void onCompleted() {
						final ProcedureCall<?> pc = ProcedureCall.create(
								ProcessCompleterImpl.this,
								ProcessCompleterImpl.this,
								REQUEST_RESOURCE_ALLLOCATIONS, request);
						getSimulator()
								.schedule(pc, Trigger.createAbsolute(now));
					}
				});
		/*
		 * new ObservingSynchronousExecutor<Percept<?>>( new
		 * Evaluator<Percept<?>>() {
		 * 
		 * @Override public boolean hasError() {
		 * 
		 * return false; }
		 * 
		 * @Override public boolean hasNext(Percept<?> args) {
		 * LOG.info("ADDING: " + args); getReasoner().addBeliefToKBase(
		 * getReasoner().toBelief(args)); return false; }
		 * 
		 * @Override public boolean isComplete() { LOG.info("IS_COMPLETE");
		 * return true; } }).subscribe(getWorld().makeObservation(
		 * request.getProcessTypeID()));
		 * 
		 * final ProcedureCall<?> pc = ProcedureCall.create(this, this,
		 * REQUEST_RESOURCE_ALLLOCATIONS, getProcessTypeId());
		 * getSimulator().schedule(pc, Trigger.createAbsolute(getTime()));
		 */
		// Allocate Resources
		// FIXME : Allocate resources per activity, not per
		// process.
		// getBinder().bind(ProcessGeneratorService.class)
		// .notifyProcessAllocating(request.getReceiverID().getValue());

		// this.lookup.initiate((ProcessCompletion.Request) request);
	}

	public static final String MANAGE_PROCESSES_METHOD = "manageProcesses";

	@Schedulable(value = MANAGE_PROCESSES_METHOD)
	public void manageProcesses(final ProcessCompletion.Request cause) {
		getBinder().inject(ManageProcessActionService.class)
				.manageProcessInstanceForType(cause);
		// getBinder().bind(ProcessGeneratorService.class).manageProcesses();
	}

	private static final String REQUEST_RESOURCE_ALLLOCATIONS = "requestResourceAllocations";

	public SimTime getAvailableTime(Object subject) {
		SimTime result = SimTime.ZERO;
		Long delta = null;
		if (subject instanceof Material)
			delta = ((Material) subject).getAvailableFromTime();
		else if (subject instanceof Person)
			delta = ((Person) subject).getAvailableFromTime();
		else if (subject instanceof AssemblyLine)
			delta = ((AssemblyLine) subject).getAvailableFromTime();
		return result.plus((delta == null) ? 0 : delta.longValue(),
				TimeUnit.MILLIS);
	}

	public AgentID[] getListOfResourceAddress(ModelID modelID,
			Iterable<?> resources) {
		boolean wasUnavailable = false;
		Set<AgentID> recipientSet = new HashSet<AgentID>();
		for (Object subject : resources) {
			SimTime ta = getBinder().inject(SimTimeFactory.class).create(
					getAvailableTime(subject).getMillis(), TimeUnit.MILLIS);
			SimTime tn = getBinder().inject(ReplicatingCapability.class)
					.getTime();
			if (ta.isAfter(tn)) {
				wasUnavailable = true;
				LOG.warn(subject + " not yet available for "
						+ ta.minus(tn).getMillis() + "ms");
				continue;
			}
			if (((AbstractEmbodied<?>)subject).isUnAvailable()) {
				wasUnavailable = true;
				continue;
			}
			final String name = ((AbstractNamed<?>) subject).getName();
			recipientSet.add(newAgentID(name));
		}
		AgentID[] recipients = new AgentID[recipientSet.size()];
		if (recipients.length == 0 && !wasUnavailable)
			return null;
		return recipientSet.toArray(recipients);
	}

	@Schedulable(REQUEST_RESOURCE_ALLLOCATIONS)
	private void requestResourceAllocations(
			final ProcessCompletion.Request cause) {
		final String processTypeID = cause.getProcessTypeID();
		// getSimulator().pause();
		// FIXME: Need to be able to to allocate multiple instances of a role
		// now it is just limited to one instance per role.
		LOG.trace("Allocating resources for process.");
		LOG.warn("Resources are allocated per role (not per instance) and "
				+ "per process (not per activity).");
		Iterable<Person> persons = getBinder().inject(Datasource.class)
				.findPersons();
		Iterable<AssemblyLine> assemblyLines = getBinder().inject(
				Datasource.class).findAssemblyLines();
		Iterable<Material> materials = getBinder().inject(Datasource.class)
				.findMaterials();
		AgentID personAIDs[] = getListOfResourceAddress(getBinder().getID()
				.getModelID(), persons);
		AgentID assemblyLineAIDs[] = getListOfResourceAddress(getBinder()
				.getID().getModelID(), assemblyLines);
		AgentID materialAIDs[] = getListOfResourceAddress(getBinder().getID()
				.getModelID(), materials);

		Map<Serializable, Set<AgentID>> candidates = new HashMap<Serializable, Set<AgentID>>();

		for (ResourceRequirement requirement : getWorld()
				.getProcess(processTypeID).getRequiredResources().values()) {
			if (personAIDs != null
					&& requirement.getResource().getTypeID()
							.equals(Person.class)) {
				Set<AgentID> aidSet = new HashSet<AgentID>();
				for (int i = 0; i < personAIDs.length; i++) {
					aidSet.add(personAIDs[i]);
				}
				candidates.put(LegacySLUtil
						.requestResourceAllocationForRequirement(requirement)
						.toString(), aidSet);
			} else if (materialAIDs != null
					&& requirement.getResource().getTypeID()
							.equals(Material.class)) {
				Set<AgentID> aidSet = new HashSet<AgentID>();
				for (int i = 0; i < materialAIDs.length; i++) {
					aidSet.add(materialAIDs[i]);
				}
				candidates.put(LegacySLUtil
						.requestResourceAllocationForRequirement(requirement)
						.toString(), aidSet);
			} else if (assemblyLineAIDs != null
					&& requirement.getResource().getTypeID()
							.equals(AssemblyLine.class)) {

				Set<AgentID> aidSet = new HashSet<AgentID>();
				for (int i = 0; i < assemblyLineAIDs.length; i++) {
					aidSet.add(assemblyLineAIDs[i]);
				}
				candidates.put(LegacySLUtil
						.requestResourceAllocationForRequirement(requirement)
						.toString(), aidSet);
			}
		}

		// FIXME Add callback and converter

		// TODO notify scenario replicator
		// getBinder().bind(ProcessGeneratorService.class)
		// .notifyProcessAllocating(getOwnerID().getValue());

		final CountDownLatch latch = new CountDownLatch(1);
		getBinder()
				.inject(ResourceAllocationNegotiator.class)
				.negotiate(getAllocCallback(cause.getSenderID()), candidates,
						new ConversionCallback() {
							@Override
							public Serializable convert(Serializable forumla) {
								return LegacySLUtil.convertAgentID(
										getOwnerID(), forumla);
							}
						}).subscribe(new Observer<AllocationCallback>() {

					@Override
					public void onNext(final AllocationCallback allocationResult) {
						if (!allocationResult.wasSucces()) {
							LOG.info("Failed to allocate: "
									+ allocationResult
											.getUnavailabeResourceIDs());
						} else {
							final Map<AgentID, Serializable> resources = allocationResult
									.getAllocatedResources();
							for (AgentID aid : resources.keySet()) {
								SLParsableSerializable f = new SLParsableSerializable(
										resources.get(aid).toString());
								Belief b = getReasoner()
								.toBelief(
										f,
										ResourceAllocation.ALLOCATED_AGENT_AID,
										aid);
								getReasoner()
										.addBeliefToKBase(b);
								LOG.info(aid + " is allocated as: "
										+ b.toString());
							}
							LOG.info("All resources are allocated for process instance: "
									+ getID());
						}
					}

					@Override
					public void onCompleted() {
						latch.countDown();
					}

					@Override
					public void onError(final Throwable e) {
						e.printStackTrace();
					}
				});

		try {
			LOG.info("Waiting for allocation of candidates: " + candidates);
			latch.await(1, java.util.concurrent.TimeUnit.SECONDS);
		} catch (final InterruptedException ignore) {
		}

		getReceiver().getIncoming().ofType(ActivityParticipation.Result.class)
				.subscribe(new Observer<ActivityParticipation.Result>() {

					@Override
					public void onCompleted() {
						// nothing special here.
					}

					@Override
					public void onError(Throwable error) {
						LOG.error(
								"Failed to receive ActivityPartication Result",
								error);
					}

					@Override
					public void onNext(Result result) {
						getBinder().inject(ManageProcessActionService.class)
								.notifyActivityParticipationResult(result);
					}
				});

		if (getAllocCallback(cause.getSenderID()).wasSucces())
			try {
				getSimulator().schedule(
						ProcedureCall.create(ProcessCompleterImpl.this,
								ProcessCompleterImpl.this,
								MANAGE_PROCESSES_METHOD, cause),
						Trigger.createAbsolute(getTime()));
				send(
						new ASIMOVMessage(getTime(), getOwnerID(), cause
								.getSenderID(), "AvailableResourcesChange:"
								+ getTime().hashCode()));
			} catch (Exception e) {
				LOG.error("FAILED TO PERFORM PROCESS", e);
			}
		else
			try {
				
				send(ProcessCompletion.Result.Builder
						.forProducer(this, cause)
						.withSuccess(
								getAllocCallback(cause.getSenderID())
										.wasSucces()).build());
				getScheduler().schedule(
						ProcedureCall.create(this, this, DESTROY),
						Trigger.createAbsolute(getTime().plus(1,TimeUnit.MILLIS)));
			} catch (Exception e1) {
				LOG.error(
						"An exception occured while trying to send process completion outcome",
						e1);
			}

	}

	// private String getProcessTypeId()
	// {
	// return processTypeId;
	// }

	// /**
	// * @return the factId
	// */
	// private FactID getFactId()
	// {
	// return this.factId;
	// }
	//
	// /**
	// * @param factId the factId to set
	// */
	// private void setFactId(FactID factId)
	// {
	// this.factId = factId;
	// }
	//
	// /**
	// * @return the cause
	// */
	// public Request getCause()
	// {
	// return this.cause;
	// }
	//
	// /**
	// * @param cause the cause to set
	// */
	// public void setCause(Request cause)
	// {
	// this.cause = cause;
	// }

	AllocationCallback theCallback;

	public static final String DESTROY = "DESTROY";
	
	@Schedulable(DESTROY)
	public void destroy(){
		try {
			getFinalizer().destroy();
		} catch (Exception e) {
			LOG.error("Failed to destroy process agent",e);
		}
	}
	
	private final AllocationCallback getAllocCallback(final AgentID scenario) {
		if (theCallback == null)
			theCallback = new AllocationCallback() {

				private boolean wasSucces = false;

				@Override
				public void failure(Set<AgentID> aids) {
					;// nothing special here
				}

				@Override
				public void error(final Exception error) {
					;// nothing special here
				}

				@Override
				public void done(Map<AgentID, Serializable> resources) {
					wasSucces = true;
				}

				@Override
				public Map<AgentID, Serializable> getAllocatedResources() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Set<AgentID> getUnavailabeResourceIDs() {
					// TODO Auto-generated method stub
					return null;
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
		return theCallback;
	}

}
