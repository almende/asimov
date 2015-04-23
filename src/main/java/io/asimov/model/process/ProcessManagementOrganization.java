package io.asimov.model.process;

import io.asimov.agent.gui.SemanticAgentGui;
import io.asimov.agent.process.ProcessCompletion.ProcessCompleter;
import io.asimov.agent.process.ProcessManagementWorld;
import io.asimov.agent.scenario.ScenarioReplication.ScenarioReplicator;
import io.asimov.model.ASIMOVOrganization;
import io.coala.bind.Binder;
import io.coala.log.InjectLogger;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;

import javax.inject.Inject;

import org.apache.log4j.Logger;

/**
 * {@link ProcessManagementOrganization}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class ProcessManagementOrganization extends
		ASIMOVOrganization<ProcessManagementWorld>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	@InjectLogger
	private Logger LOG;

	/** */
	private SemanticAgentGui myGui;

	/**
	 * {@link ProcessManagementOrganization} constructor
	 * 
	 * @param binder
	 * @throws Exception
	 */
	@Inject
	public ProcessManagementOrganization(final Binder binder)
	{
		super(binder);

	}

	@Override
	public ProcessManagementWorld getWorld()
	{
		return getBinder().inject(ProcessManagementWorld.class);
	}

	@Override
	public void initialize() throws Exception
	{
		super.initialize();

		if (getProperty("gui").getBoolean(false)
				|| getProperty("process.gui").getBoolean(false)
				|| getProperty(getID().getValue() + ".gui").getBoolean(false))
			try
			{
				if (!GraphicsEnvironment.isHeadless())
					this.myGui = new SemanticAgentGui(getBinder(), null, true,
							true);
			} catch (final HeadlessException e)
			{
				LOG.warn("No screen/keyboard/mouse available", e);
			}

		// initialize executor observers here
		getProcessCompleter();
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
	protected ProcessCompleter getProcessCompleter()
	{
		return getBinder().inject(ProcessCompleter.class);
	}
}
