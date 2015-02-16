package io.asimov.agent.scenario.impl;

import io.arum.model.process.ProcessManagementOrganization;
import io.arum.model.resource.RouteLookup.RouteProvider;
import io.arum.model.resource.person.PersonResourceManagementWorld;
import io.asimov.agent.process.ProcessCompletion;
import io.asimov.agent.scenario.Replication;
import io.asimov.agent.scenario.ScenarioManagementWorld;
import io.asimov.agent.scenario.ScenarioManagementWorld.ProcessEvent;
import io.asimov.agent.scenario.ScenarioManagementWorld.ProcessEventType;
import io.asimov.agent.scenario.ScenarioManagementWorld.ResourceEvent;
import io.asimov.agent.scenario.ScenarioReplication;
import io.asimov.agent.scenario.ScenarioReplication.ScenarioReplicator;
import io.coala.agent.AgentID;
import io.coala.agent.AgentStatusUpdate;
import io.coala.bind.Binder;
import io.coala.capability.admin.DestroyingCapability;
import io.coala.enterprise.fact.CoordinationFact;
import io.coala.enterprise.fact.CoordinationFactType;
import io.coala.enterprise.role.AbstractSelfInitiator;
import io.coala.enterprise.role.Executor;
import io.coala.invoke.ProcedureCall;
import io.coala.invoke.Schedulable;
import io.coala.log.InjectLogger;
import io.coala.random.RandomDistribution;
import io.coala.time.SimDuration;
import io.coala.time.SimTime;
import io.coala.time.TimeUnit;
import io.coala.time.Trigger;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import rx.Observer;
import rx.functions.Func1;

/**
 * {@link ScenarioReplicatorImpl}
 * 
 * @version $Revision: 1083 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
public class ScenarioReplicatorImpl extends
		AbstractSelfInitiator<ScenarioReplication.Request> implements
		ScenarioReplication.ScenarioReplicator
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	@InjectLogger
	private Logger LOG;

	/** */
	private final Set<AgentID> allResources = new HashSet<>();

	/** */
	private final Set<AgentID> readyResources = new HashSet<>();

	/** */
	// private RandomDistribution<SimTime> repeatIntervalDist;

	/** */
	// FIXME Do not make static
	// private final Map<String, ProcessState> processStates = new
	// ConcurrentSkipListMap<String, ProcessState>();

	/** */
	// private final Set<String> processTypeIDs = new HashSet<String>();

	/** */
	private final Map<String, Set<String>> processInstancesByType = new ConcurrentSkipListMap<String, Set<String>>();

	/** */
	private static boolean busy = false;

	/** FIXME handle pending replications! */
	private Queue<ScenarioReplication.Request> pendingReplications = new LinkedBlockingQueue<ScenarioReplication.Request>();

	public static final String PROCESS_STATUS_RUNNING = "PROCESS_RUNNING";

	public static final String PROCESS_STATUS_PENDING = "PROCESS_PENDING";
	
	public static final String PROCESS_STATUS_DISABLED_TEST = "PROCESS_STATUS_DISABLED_TEST";

	public static final Map<String, Map<AgentID, String>> processStatus = new TreeMap<String, Map<AgentID, String>>();

	/**
	 * {@link ScenarioReplicatorImpl} constructor
	 * 
	 * @param binder
	 */
	@Inject
	private ScenarioReplicatorImpl(final Binder binder)
	{
		super(binder);
	}

	/** @return the agent's local {@link ScenarioManagementWorld} */
	// @Override
	protected ScenarioManagementWorld getWorld()
	{
		return getBinder().inject(ScenarioManagementWorld.class);
	}

	@Override
	public void initialize()
	{
		// REQUIRED TO INITIALIZE ROLE
		getBinder().inject(RouteProvider.class);

		// TODO from config?
		// this.repeatIntervalDist = newDist().getConstant(
		// newTime(1, TimeUnit.MINUTES));

		// subscribe to responses to nested ProcessCompletion requests
		getReceiver().getIncoming().ofType(ProcessCompletion.Result.class)
				.filter(new Func1<ProcessCompletion, Boolean>()
				{
					@Override
					public Boolean call(final ProcessCompletion fact)
					{
						return fact.getID().getType() == CoordinationFactType.STATED;
					}
				}).subscribe(new Observer<ProcessCompletion>()
				{
					@Override
					public void onNext(final ProcessCompletion fact)
					{
						onNested((ProcessCompletion.Result) fact);
					}

					@Override
					public void onCompleted()
					{
						//
					}

					@Override
					public void onError(final Throwable e)
					{
						e.printStackTrace();
					}
				});
	}

	protected void onNested(final ProcessCompletion.Result result)
	{
		// LOG.info("Got nested: " + result, new IllegalStateException(
		// "NOT IMPLEMENTED"));
	}

	/** @see ScenarioReplicator#initiate() */
	@Override
	public ScenarioReplication.Request initiate() throws Exception
	{
		return send(ScenarioReplication.Request.Builder.forProducer(this)
				.build());
	}

	/** @see Executor#onRequested(CoordinationFact) */
	@Override
	public void onRequested(final ScenarioReplication.Request request)
	{
		final CountDownLatch latch = new CountDownLatch(1);
		getWorld().onResources().subscribe(new Observer<ResourceEvent>()
		{
			@Override
			public void onNext(final ResourceEvent event)
			{
				switch (event.getEventType())
				{
				case ADDED:
					if (!allResources.add(event.getResourceID()))
					{
						LOG.trace("Already created resource, " + "skipping: "
								+ event.getResourceID());
					} else
					{
						latch.countDown();
						try
						{
							getBooter().createAgent(event.getResourceID(),
									event.getResourceType()).subscribe(
									new Observer<AgentStatusUpdate>()
									{
										@Override
										public void onNext(
												final AgentStatusUpdate update)
										{
											if (update.getStatus()
													.isInitializedStatus())
											{
												LOG.trace("Child agent has initialized: "
														+ update.getAgentID());
												synchronized (readyResources)
												{
													readyResources.add(update
															.getAgentID());
													readyResources.notifyAll();
												}
											}
										}

										@Override
										public void onCompleted()
										{
											//
										}

										@Override
										public void onError(final Throwable e)
										{
											e.printStackTrace();
										}
									});
						} catch (final Exception e)
						{
							LOG.error("Problem booting resource mgr agent", e);
						}
					}
					break;

				case REMOVED:
					if (!allResources.remove(event.getResourceID()))
					{
						LOG.warn("Already removed resource: "
								+ event.getResourceID());
					} else
						try
						{
							getBinder().inject(DestroyingCapability.class)
									.destroy(event.getResourceID());
						} catch (final Exception e)
						{
							LOG.error("Problem killing resource mgr agent", e);
						}
					break;
				}
			}

			@Override
			public void onCompleted()
			{
			}

			@Override
			public void onError(final Throwable e)
			{
				e.printStackTrace();
				latch.countDown();
			}
		});
		try
		{
			latch.await();
		} catch (InterruptedException e1)
		{
			;
		}

		while (!readyResources.containsAll(allResources))
		{
			final Set<AgentID> diff = new HashSet<AgentID>(allResources);
			diff.removeAll(readyResources);
			LOG.warn("Waiting for resource agents: " + diff);
			synchronized (readyResources)
			{
				try
				{
					readyResources.wait(1000);
				} catch (InterruptedException e1)
				{
					;
				}
			}
		}

		if (busy)
		{
			LOG.warn("Problem handling request, added as pending: " + request,
					new IllegalStateException("Replication already running"));
			this.pendingReplications.add(request);
		} else
		{
			busy = true;

			final Replication replication = getWorld().getReplication();

			LOG.trace("Handling request: " + request + " for replication: "
					+ replication);

			getWorld().onProcessEvent().subscribe(new Observer<ProcessEvent>()
			{
				@Override
				public void onNext(final ProcessEvent event)
				{
					if (event.getEventType().equals(ProcessEventType.REQUESTED))
						scheduleNextBP(request, event.getProcessTypeID());
				}

				@Override
				public void onCompleted()
				{
					//
				}

				@Override
				public void onError(final Throwable e)
				{
					LOG.error("Error while retrieving processes ", e);
				}
			});
		}
	}

	@Override
	protected void onStated(final ScenarioReplication.Request state)
	{
		LOG.info("Replication complete: " + state);
	}

	private static long PROCESS_INSTANCE_COUNT = 0;

	// private synchronized void scheduleProcessInstantiation(
	// final ScenarioReplication.Request cause,
	// final String processTypeID, final String processInstanceID,
	// final SimTime offset) throws Exception
	// {
	// final SimTime t = getTime().plus(offset);
	// LOG.info("Requesting process instantiation of: " + processInstanceID
	// + " at " + t);
	// synchronized (processesByType)
	// {
	// final Set<String> processes = getProcesses(processTypeID);
	// if (!processes.add(processInstanceID))
	// {
	// LOG.error("Already added: " + processInstanceID,
	// new IllegalStateException());
	// return;
	// }
	// }
	// LOG.info("exit before callback:" + processInstanceID);
	//
	// final ProcedureCall<?> pc = ProcedureCall.create(this, this,
	// ADD_PROCESS_MANAGER_AGENT, cause, processTypeID,
	// processInstanceID);
	//
	// getSimulator().schedule(pc, Trigger.createAbsolute(t));
	// LOG.info("exit:" + processInstanceID);
	// }

	private static final String SCHEDULE_NEXT_BP = "scheduleNextBP";

	private SimDuration drawProcessStartTimeOfDay(final String processTypeID)
	{
		final RandomDistribution<SimDuration> dist = getBinder().inject(
				ScenarioManagementWorld.class).getProcessStartTimeOfDayDist(
				processTypeID);
		if (dist != null)
			return dist.draw();
		LOG.warn("No start time distribution available for process type: "
				+ processTypeID);
		return new SimDuration(0, TimeUnit.HOURS);
	}

	public SimTime getAbsProcessRepeatTime(final String processTypeID)
	{
		final SimTime now = getTime();
		if (getBinder().inject(
				PersonResourceManagementWorld.class).onSiteDelay(now.plus(1, TimeUnit.HOURS)).doubleValue() == 0)
			return now.plus(1, TimeUnit.HOURS);
		final SimDuration millisOfDay = new SimDuration(new DateTime(
				now.getIsoTime()).getMillisOfDay(), TimeUnit.MILLIS);
		final SimTime startOfDay = now.minus(millisOfDay);
		SimDuration procStartTimeOfDay = drawProcessStartTimeOfDay(processTypeID);
		if (procStartTimeOfDay.isOnOrBefore(millisOfDay))
			procStartTimeOfDay = procStartTimeOfDay.plus(1, TimeUnit.DAYS);
		final SimTime absStart = startOfDay.plus(procStartTimeOfDay);
		final SimTime result = absStart.plus(getBinder().inject(
				PersonResourceManagementWorld.class).onSiteDelay(absStart));
		final DateTime offset = new DateTime(getBinder().inject(Date.class));
		LOG.info("process repeat from now (" + now.toDateTime(offset)
				+ ") = startofday " + startOfDay.toDateTime(offset)
				+ " + procStartOfDay " + procStartTimeOfDay.toHours()
				+ " = (skip non-working hrs, abs) " + result.toDateTime(offset)
				+ ", processType: " + processTypeID);
		return result;
	}

	@Schedulable(SCHEDULE_NEXT_BP)
	protected void scheduleNextBP(final ScenarioReplication.Request cause,
			final String processTypeID)
	{
		// TODO determine appropriate delay, based on:
		// - current distribution delta (compared to goal)
		// - current process instance agent activity/failures

		// for each BP type,
		// draw next interval T for this BP type
		// schedule scheduleNextBP method after interval T with this type

		final SimTime onSiteStart = getAbsProcessRepeatTime(processTypeID);
		getSimulator().schedule(
				ProcedureCall.create(this, this, ADD_PROCESS_MANAGER_AGENT,
						cause, processTypeID),
				Trigger.createAbsolute(onSiteStart));
		getSimulator().schedule(
				ProcedureCall.create(this, this, SCHEDULE_NEXT_BP, cause,
						processTypeID), Trigger.createAbsolute(onSiteStart));
	}

	private static final String ADD_PROCESS_MANAGER_AGENT = "addProcessManagerAgent";

	@Schedulable(ADD_PROCESS_MANAGER_AGENT)
	protected synchronized void addProcessManagerAgent(
			final ScenarioReplication.Request cause, final String processTypeID)
			throws Exception
	{
		// FIXME hold if too many agents are competing for resources !!!???
		final String processInstanceID = "ProcMgr" + PROCESS_INSTANCE_COUNT++;

		LOG.info("Adding process agent for process instance: "
				+ processInstanceID + " of type " + processTypeID);
		synchronized (processInstancesByType)
		{
			getProcesses(processTypeID).add(processInstanceID);
		}
		final AgentID completerAgentID = newAgentID(processInstanceID);
		if (processStatus.containsKey(processTypeID)
				&& processStatus.get(processTypeID).values()
						.contains(PROCESS_STATUS_RUNNING))
			processStatus.get(processTypeID).put(completerAgentID,
					PROCESS_STATUS_PENDING);
		else
		{
			if (!processStatus.containsKey(processTypeID))
				processStatus
						.put(processTypeID, new HashMap<AgentID, String>());
			processStatus.get(processTypeID).put(completerAgentID,
					PROCESS_STATUS_RUNNING);
			bootAgent(completerAgentID, ProcessManagementOrganization.class,
					ProcedureCall.create(this, this,
							INITIATE_PROCESS_COMPLETION, cause, processTypeID,
							completerAgentID));
		}
	}

	private static final String INITIATE_PROCESS_COMPLETION = "initiateProcessCompletion";

	@Schedulable(INITIATE_PROCESS_COMPLETION)
	protected void initiateProcessCompletion(
			final ScenarioReplication.Request cause,
			final String processTypeID, final AgentID completerID)
			throws Exception
	{
		getBinder().inject(ProcessCompletion.Initiator.class).initiate(cause,
				processTypeID, completerID);
	}

	// public void scheduleProcessManagement(final ScenarioReplication.Request
	// cause, long delta)
	// {
	// final SimTime time = getTime();
	// ProcedureCall<?> job = ProcedureCall.create(this, this,
	// MANAGE_PROCESSES, cause);
	// getBinder().bind(SimulatorService.class).schedule(job,
	// Trigger.createAbsolute(time.plus(delta, TimeUnit.MILLIS)));
	// LOG.info("SCHEDULED MANAGEMENT AT " + getTime());
	//
	// }

	/*	public void notifyProcessInitializing(final String processID)
		{
			final ProcessState oldState = processStates.put(processID,
					ProcessState.INITIALIZING);
			if (oldState != null)
				LOG.warn(this.logPrefix + "(" + processID + ")"
						+ "Unexpected state transition from " + oldState
						+ ", expected: " + ProcessState.INITIALIZING
						+ " as the start state.");
			LOG.info(this.logPrefix + "Notified process initializing: " + processID);
		}

		public void notifyProcessAllocating(final String processID)
		{
			final ProcessState oldState = processStates.put(processID,
					ProcessState.ALLOCATING);
			if (oldState != ProcessState.INITIALIZING)
				LOG.warn(this.logPrefix + "(" + processID + ")"
						+ "Unexpected state " + oldState
						+ ", expected transition: " + ProcessState.INITIALIZING
						+ " -> " + ProcessState.ALLOCATING);
			LOG.info(this.logPrefix + "Notified process allocating: " + processID);
		}

		public void notifyProcessFailed(final String processID)
		{
			final ProcessState oldState = processStates.put(processID,
					ProcessState.FAILED);
			LOG.info(this.logPrefix + "Notified process failed: " + processID);
			if (oldState != ProcessState.ALLOCATING)
				LOG.warn(this.logPrefix + "(" + processID + ")"
						+ "Unexpected state " + oldState
						+ ", expected transition: " + ProcessState.ALLOCATING
						+ " -> " + ProcessState.FAILED);
			else
				scheduleProcessManagement(repeatIntervalDist.draw().getMillis() * 100000);

		}

		public void notifyProcessStarted(final String processID)
		{
			final ProcessState oldState = processStates.put(processID,
					ProcessState.RUNNING);
			if (oldState != ProcessState.ALLOCATING)
				LOG.warn(this.logPrefix + "(" + processID + ")"
						+ "Unexpected state " + oldState
						+ ", expected transition: " + ProcessState.ALLOCATING
						+ " -> " + ProcessState.RUNNING);
			LOG.info(this.logPrefix + "Notified process started: " + processID);
		}

		public void notifyProcessComplete(final String processID)
		{
			final ProcessState oldState = processStates.put(processID,
					ProcessState.COMPLETE);
			LOG.info(this.logPrefix + "Notified process complete: " + processID);
			if (oldState != ProcessState.RUNNING)
				LOG.warn(this.logPrefix + "(" + processID + ")"
						+ "Unexpected state " + oldState
						+ ", expected transition: " + ProcessState.RUNNING + " -> "
						+ ProcessState.COMPLETE);
			else
				scheduleProcessManagement(repeatIntervalDist.draw().getMillis());
		}

		public void notifyProcessRestarted(final String processID)
		{
			final ProcessState oldState = processStates.put(processID,
					ProcessState.RESTARTED);
			if (oldState != ProcessState.COMPLETE
					&& oldState != ProcessState.FAILED)
				LOG.warn(this.logPrefix + "(" + processID + ")"
						+ "Unexpected state " + oldState
						+ ", expected transition: " + ProcessState.COMPLETE
						+ " or " + ProcessState.FAILED + " -> "
						+ ProcessState.RESTARTED);
			LOG.info(this.logPrefix + "Notified process restarted: " + processID);
		}*/

	private Set<String> getProcesses(final String processTypeID)
	{
		synchronized (processInstancesByType)
		{
			if (!processInstancesByType.containsKey(processTypeID))
				processInstancesByType.put(processTypeID,
						Collections.synchronizedSet(new HashSet<String>()));
			return processInstancesByType.get(processTypeID);
		}
	}

	// private static final String MANAGE_PROCESSES = "manageProcesses";
	//
	// @Schedulable(MANAGE_PROCESSES)
	// protected synchronized void manageProcesses(
	// final ScenarioReplication.Request cause)
	// {
	// LOG.info("Managing processes at t=" + getTime());
	//
	// Set<String> restartedProcesses = new HashSet<String>();
	// for (String processTypeID : processTypeIDs)
	// {
	// synchronized (processesByType)
	// {
	// final Set<String> processes = getProcesses(processTypeID);
	// final Set<String> copy = new HashSet<String>();
	// copy.addAll(processes);
	// processLoop: for (String processID : copy)
	// {
	// ProcessState currentState = processStates.get(processID);
	// if (currentState != null)
	// LOG.info("Found state: " + currentState.name());
	// else
	// {
	// LOG.error("No state for " + processID + " in:"
	// + processStates);
	// }
	// if (currentState == null)
	// continue processLoop;
	//
	// final long repeatDelayMS;
	// if (currentState == ProcessState.FAILED)
	// {
	// repeatDelayMS = repeatIntervalDist.draw().getMillis();
	// } else if (currentState == ProcessState.COMPLETE)
	// {
	// repeatDelayMS = 1000; // one second from now
	// } else
	// {
	// continue processLoop;
	// }
	//
	// final SimTime requestTimeOffset = newTime(repeatDelayMS,
	// TimeUnit.MILLIS);
	// // if (!hasRequestedGrantForTime(processInstanceID,time)) {
	// if (repeatDelayMS != 0)
	// {
	// try
	// {
	// scheduleProcessInstantiation(cause, processTypeID,
	// getNewProcessAgentUUID(processTypeID),
	// requestTimeOffset);
	// restartedProcesses.add(processID);
	// } catch (Exception e)
	// {
	// LOG.error(
	// "An error occured while trying to instantiatie the process instance manager",
	// e);
	// }
	// }
	// }
	// }
	// }
	// // TODO schedule process restart
	// // for (String processID : restartedProcesses)
	// // notifyProcessRestarted(processID);
	// }
}
