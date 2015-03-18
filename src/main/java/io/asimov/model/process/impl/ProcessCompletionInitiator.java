package io.asimov.model.process.impl;

import io.asimov.agent.process.ProcessCompletion;
import io.asimov.agent.scenario.ScenarioManagementWorld;
import io.asimov.agent.scenario.ScenarioReplication;
import io.asimov.agent.scenario.ScenarioReplication.ScenarioReplicator;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.enterprise.role.AbstractInitiator;
import io.coala.log.InjectLogger;
import io.coala.random.RandomDistribution;
import io.coala.time.SimTime;
import io.coala.time.TimeUnit;

import javax.inject.Inject;

import org.apache.log4j.Logger;

/**
 * {@link ProcessCompletionInitiator}
 * 
 * @version $Revision: 1083 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
public class ProcessCompletionInitiator extends
		AbstractInitiator<ProcessCompletion.Result> implements
		ProcessCompletion.Initiator {

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	@InjectLogger
	private Logger LOG;

	private int resourcesHash = 0;

	/** */
	private RandomDistribution<SimTime> retryDelayDist;

	private ScenarioReplication.Request cause;

	/**
	 * {@link ProcessCompletionInitiator} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected ProcessCompletionInitiator(final Binder binder) {
		super(binder);
	}

	@Override
	public void initialize() throws Exception {
		super.initialize();

		// TODO read from config?
		this.retryDelayDist = newDist().getConstant(newTime(1, TimeUnit.HOURS));
	}

	protected ScenarioReplicator getReplicator() {
		return getBinder().inject(ScenarioReplicator.class);
	}

	@Override
	public ProcessCompletion.Request initiate(
			final ScenarioReplication.Request cause,
			final String processTypeID, final AgentID procMgrID)
			throws Exception {
		this.cause = cause;
		return send(ProcessCompletion.Request.Builder
				.forProducer(getReplicator(), cause)
				.withProcessTypeID(processTypeID).withReceiverID(procMgrID)
				.build());
	}

	@Override
	public void onStated(final ProcessCompletion.Result result) {
		if (!((ProcessCompletion.Result) result).getSuccess())
			try {
				LOG.warn("Process allocation failed");
			} catch (final Exception e) {
				LOG.error("Problem retrying process completion", e);
			}
		else {
//			resourcesHash = ("" + getTime().hashCode() + result.hashCode())
//					.hashCode(); // invalidate hash
			LOG.warn("Process completed successfully!");
//			ProcedureCall<?> pc = ProcedureCall.create(this, getBinder()
//					.inject(ScenarioReplicator.class),
//					ScenarioReplicatorImpl.SCHEDULE_NEXT_BP, this.cause, result
//							.getProcessTypeID(), resourcesHash);
//			getScheduler().schedule(pc, Trigger.createAbsolute(getTime()));
			getBinder().inject(ScenarioManagementWorld.class)
					.updateResourceStatusHash("");
		}
	}
}