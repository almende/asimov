package io.asimov.agent.scenario;

import io.arum.model.ARUMOrganization;
import io.asimov.agent.gui.ScenarioAgentGui;
import io.asimov.agent.scenario.ScenarioReplication.ScenarioReplicator;
import io.coala.bind.Binder;
import io.coala.capability.admin.DestroyingCapability;
import io.coala.capability.plan.ClockStatusUpdate;
import io.coala.log.InjectLogger;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observer;

/**
 * {@link ScenarioManagementOrganization}
 * 
 * @version $Revision: 1083 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class ScenarioManagementOrganization extends
		ARUMOrganization<ScenarioManagementWorld>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	@InjectLogger
	private Logger LOG;

	/** */
	private ScenarioAgentGui myGui;

	/**
	 * {@link ScenarioManagementOrganization} constructor
	 * 
	 * @param binder
	 * @throws Exception
	 */
	@Inject
	private ScenarioManagementOrganization(final Binder binder)
			throws Exception
	{
		super(binder);
	}

	@Override
	public void initialize() throws Exception
	{
		super.initialize();

		if (getProperty("gui").getBoolean(false)
				|| getProperty("scenario.gui").getBoolean(false)
				|| getProperty(getID().getValue() + ".gui").getBoolean(false))
			try
			{
				if (!GraphicsEnvironment.isHeadless())
					this.myGui = new ScenarioAgentGui(getBinder(), null, true,
							true);
			} catch (final HeadlessException e)
			{
				LOG.warn("No screen/keyboard/mouse available", e);
			}

		getSimulator().getStatusUpdates().subscribe(
				new Observer<ClockStatusUpdate>()
				{
					@Override
					public void onNext(final ClockStatusUpdate update)
					{
						LOG.trace("Simulator " + update.getClockID()
								+ " status is now " + update.getStatus());
						if (update.getStatus().isFinished())
							try
							{
								getBinder().inject(DestroyingCapability.class)
										.destroy();
							} catch (final Exception e)
							{
								LOG.error("Problem destroying self", e);
							}
					}

					@Override
					public void onCompleted()
					{
						//
					}

					@Override
					public void onError(final Throwable t)
					{
						LOG.warn("Problem with simulator status", t);
					}
				});

		// initialize all roles
		// getDirectoryProvider();

		LOG.info("Requesting replication...");
		final ScenarioReplication fact = getScenarioReplicator().initiate();
		LOG.info("Scheduled request: " + fact);

		// getSimulator().getStatusUpdates().doOnNext(
		// new Action1<ClockStatusUpdate>()
		// {
		// @Override
		// public void call(final ClockStatusUpdate update)
		// {
		// if (update.getStatus().isFinished())
		// try
		// {
		// LOG.info("Simulator done");
		// getFinalizer().kill(); // FIXME kill?
		// } catch (final Exception e)
		// {
		// LOG.error("Problem killing self", e);
		// }
		// }
		// });
	}

	@Override
	public void finish()
	{
		if (this.myGui != null && this.myGui.isVisible())
			this.myGui.dispose();
	}

	
	/**
	 * @return the {@link ScenarioReplicator}
	 */
	protected ScenarioReplicator getScenarioReplicator()
	{
		return getBinder().inject(ScenarioReplicator.class);
	}

}
