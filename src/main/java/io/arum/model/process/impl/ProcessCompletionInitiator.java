package io.arum.model.process.impl;

import io.arum.model.resource.person.PersonResourceManagementWorld;
import io.asimov.agent.process.ProcessCompletion;
import io.asimov.agent.scenario.ScenarioReplication;
import io.asimov.agent.scenario.ScenarioReplication.ScenarioReplicator;
import io.asimov.agent.scenario.impl.ScenarioReplicatorImpl;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.enterprise.role.AbstractInitiator;
import io.coala.log.InjectLogger;
import io.coala.random.RandomDistribution;
import io.coala.time.SimDuration;
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
		ProcessCompletion.Initiator
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	@InjectLogger
	private Logger LOG;

	/** */
	private RandomDistribution<SimTime> retryDelayDist;

	/**
	 * {@link ProcessCompletionInitiator} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected ProcessCompletionInitiator(final Binder binder)
	{
		super(binder);
	}

	@Override
	public void initialize() throws Exception
	{
		super.initialize();

		// TODO read from config?
		this.retryDelayDist = newDist().getConstant(newTime(1, TimeUnit.HOURS));
	}

	protected ScenarioReplicator getReplicator()
	{
		return getBinder().inject(ScenarioReplicator.class);
	}

	@Override
	public ProcessCompletion.Request initiate(
			final ScenarioReplication.Request cause,
			final String processTypeID, final AgentID procMgrID)
			throws Exception
	{
		return send(
				ProcessCompletion.Request.Builder
						.forProducer(getReplicator(), cause)
						.withProcessTypeID(processTypeID)
						.withReceiverID(procMgrID).build());
	}

	@Override
	public ProcessCompletion.Request reinitiate(final SimTime delay,
			final ProcessCompletion.Result cause) throws Exception
	{
		// FIXME re-use process manager agent/org for another alloc attempt?
		return send(
				delay,
				ProcessCompletion.Request.Builder
						.forProducer(getReplicator(), cause)
						.withProcessTypeID(cause.getProcessTypeID())
						.withReceiverID(cause.getSenderID()).build());
	}

	@Override
	public void onStated(final ProcessCompletion.Result result)
	{
		if (!((ProcessCompletion.Result) result).getSuccess())
			try
			{
				final SimTime delay = this.retryDelayDist.draw();
				// FIXME Assumes al persons have same desiredSiteEnterTime;
				//LOG.warn("FIXME: Assumption that al persons have the same desired onSite time.");
				SimDuration onSiteDelta = getBinder().inject(PersonResourceManagementWorld.class).onSiteDelay(getSimulator().getTime().plus(delay));
				LOG.warn("Process allocation failed, retrying in " + delay.plus(onSiteDelta)
						+ ": " + result);
				//ScenarioReplicatorImpl.processStatus.get(result.getProcessTypeID()).put(result.getSenderID(),ScenarioReplicatorImpl.PROCESS_STATUS_PENDING);
				reinitiate(delay.plus(onSiteDelta), result);
			} catch (final Exception e)
			{
				LOG.error("Problem retrying process completion", e);
			}
		else {
			ScenarioReplicatorImpl.processStatus.get(result.getProcessTypeID()).remove(result.getSenderID());
			LOG.warn("Process completed successfully!");
		}
	}
}