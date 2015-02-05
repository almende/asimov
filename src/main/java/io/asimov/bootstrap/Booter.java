package io.asimov.bootstrap;

import io.asimov.agent.scenario.Replication;
import io.asimov.agent.scenario.ScenarioManagementOrganization;
import io.asimov.agent.scenario.SimStatus;
import io.asimov.db.Datasource;
import io.coala.agent.AgentStatusObserver;
import io.coala.agent.AgentStatusUpdate;
import io.coala.bind.Binder;
import io.coala.bind.BinderFactory;
import io.coala.capability.admin.CreatingCapability;
import io.coala.capability.plan.ClockStatusUpdate;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.capability.replicate.ReplicationConfig;
import io.coala.log.LogUtil;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import org.aeonbits.owner.ConfigFactory;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import rx.Observer;

/**
 * {@link Booter}
 * 
 * @date $Date: 2014-09-25 10:31:31 +0200 (do, 25 sep 2014) $
 * @version $Revision: 1076 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public class Booter
{

	/** */
	private static Logger LOG = LogUtil.getLogger(Booter.class);

	public static final String DEFAULT_CONFIG_FILE = "asimov.properties";

	private static void setupDefaultReplication(final Binder binder)
			throws Exception
	{
		// LOG.error("Missing argument, expected a replication id as argument but got none.");
		// System.exit(1);
		final String replicationID = binder.getID().getModelID().getValue();

		final Datasource ds = binder.inject(Datasource.class);
		ds.removeReplication();

		final String basePath = System.getProperty("user.dir") + "/default/"; // src/test/resources/
		final String usecaseUri = new File(basePath + "arum_scenario.xml").toURI()
				.toASCIIString();
		final String contextUri = new File(basePath + "arum_context.xml")
				.toURI().toASCIIString();
		final Number startDate = DateTime.now().withTimeAtStartOfDay()
				.getMillis();
		final Number durationMS = Duration.standardDays(28).getMillis();

		

		final Replication replication = new Replication()
				.withName(replicationID);
		replication.setId(replicationID);
		replication.setUseCaseUri(usecaseUri);
		replication.setContextUri(contextUri);
		replication.setStartDate(startDate);
		replication.setDurationMS(durationMS);

		ds.save(replication);

		LOG.warn("No replicationID specified, assuming defaults: "
				+ ds.findReplication());
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception
	{
		final String configFileName = args.length > 1 ? args[1]
				: DEFAULT_CONFIG_FILE;

		if (args.length > 0)
			ConfigFactory.setProperty("modelName", args[0]);
		
			final Binder binder = (args.length > 0) ? 
						BinderFactory.Builder.fromFile(configFileName)
						.withProperty(ReplicationConfig.class,
						ReplicationConfig.MODEL_NAME_KEY, args[0]).build()
						.create("_booter_")
					:
						BinderFactory.Builder.fromFile(configFileName).build()
						.create("_booter_");
						
		if (args.length == 0)
		{
			LOG.warn("Did not receive replication ID seting up default.");
			setupDefaultReplication(binder);
		} else {
			LOG.warn("Using replication with ID: "+args[0]+" form DataSource.");
		}

		final CountDownLatch latch = new CountDownLatch(1);
		binder.inject(ReplicatingCapability.class).getStatusUpdates()
				.subscribe(new Observer<ClockStatusUpdate>()
				{
					@Override
					public void onNext(final ClockStatusUpdate update)
					{
						LOG.info("Simulator " + update.getClockID() + " is "
								+ update.getStatus());
						if (update.getStatus().isFinished())
							latch.countDown();
					}

					@Override
					public void onCompleted()
					{
						//
					}

					@Override
					public void onError(final Throwable t)
					{
						t.printStackTrace();
						final Datasource ds = binder.inject(Datasource.class);
						final Replication replication = ds.findReplication();
						replication.setStatus(SimStatus.CANCELLED);
						ds.update(replication);
						System.exit(2);
					}
				});

		binder.inject(CreatingCapability.class)
				.createAgent("scenMgmt", ScenarioManagementOrganization.class)
				.subscribe(new AgentStatusObserver()
				{

					@Override
					public void onNext(final AgentStatusUpdate update)
					{
						LOG.info(update.getAgentID() + " is "
								+ update.getStatus());

						if (update.getStatus().isInitializedStatus())
						{
							LOG.info(binder.getID() + ": Starting sim!");
							// replication.setStatus(SimStatus.PREPARING);
							// ds.update(replication);
							binder.inject(ReplicatingCapability.class).start();
						} else if (update.getStatus().isFailedStatus())
						{
							LOG.info(binder.getID()
									+ ": Agent failed, remaining: "
									+ latch.getCount());
							latch.countDown();
						} else if (update.getStatus().isFinishedStatus())
						{
							LOG.info(binder.getID()
									+ ": Agent finished, remaining: "
									+ latch.getCount());
							latch.countDown();
						}
					}

					@Override
					public void onError(final Throwable e)
					{
						LOG.error("Problem with scenario replication mgr", e);
						final Datasource ds = binder.inject(Datasource.class);
						final Replication replication = ds.findReplication();
						replication.setStatus(SimStatus.CANCELLED);
						ds.update(replication);
						System.exit(2);
					}

					@Override
					public void onCompleted()
					{
						LOG.warn(binder.getID()
								+ ": Agent updates completed, remaining: "
								+ latch.getCount());
						latch.countDown();
					}
				});

		// wait for scenario agent to finish
		latch.await();
	}

	
}
