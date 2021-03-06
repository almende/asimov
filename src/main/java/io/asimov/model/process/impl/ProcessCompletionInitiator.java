package io.asimov.model.process.impl;

import io.asimov.agent.process.ProcessCompletion;
import io.asimov.agent.scenario.ScenarioManagementWorld;
import io.asimov.agent.scenario.ScenarioReplication;
import io.asimov.agent.scenario.ScenarioReplication.ScenarioReplicator;
import io.asimov.messaging.ASIMOVMessage;
import io.asimov.model.ASIMOVOrganization;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.enterprise.role.AbstractInitiator;
import io.coala.invoke.ProcedureCall;
import io.coala.invoke.Schedulable;
import io.coala.log.InjectLogger;
import io.coala.time.TimeUnit;
import io.coala.time.Trigger;

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
	}

	protected ScenarioReplicator getReplicator() {
		return getBinder().inject(ScenarioReplicator.class);
	}

	@Override
	public ProcessCompletion.Request initiate(
			final ScenarioReplication.Request cause,
			final String processTypeID, final AgentID procMgrID)
			throws Exception {
		return send(ProcessCompletion.Request.Builder
				.forProducer(getReplicator(), cause)
				.withProcessTypeID(processTypeID).withReceiverID(procMgrID)
				.build());
	}

	@Override
	public void onStated(final ProcessCompletion.Result result) {
		if (!((ProcessCompletion.Result) result).getSuccess()) {
			try {
				LOG.warn("Process allocation failed");
			} catch (final Exception e) {
				LOG.error("Problem retrying process completion", e);
			}
		}
		else {
//			resourcesHash = ("" + getTime().hashCode() + result.hashCode())
//					.hashCode(); // invalidate hash
			LOG.warn("Process completed successfully!");
			ProcedureCall<?> pc = ProcedureCall.create(this, this,
					UPDATE_HASH, result.getProcessTypeID());
			getScheduler().schedule(pc, Trigger.createAbsolute(getTime().plus(1,TimeUnit.MILLIS)));
			try {
				getMessenger().send(new ASIMOVMessage(getTime(), getOwnerID(), getOwnerID(), ASIMOVOrganization.REQUEST_DESTROY));
			} catch (Exception e) {
				LOG.error("Failed to request destroy",e);
			}
		}
	}
	
	public static final String UPDATE_HASH = "UPDATE_HASH";
	
	@Schedulable(UPDATE_HASH)
	public void updateHash(final String processTypeId) {
		getBinder().inject(ScenarioManagementWorld.class)
		.updateResourceStatusHash("");
		getBinder().inject(ScenarioManagementWorld.class).onProcessObserver(processTypeId);
	}
}