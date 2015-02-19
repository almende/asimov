package io.asimov.test.sim;

import io.arum.run.RunUseCase;
import io.asimov.agent.scenario.ScenarioManagementOrganization;
import io.asimov.db.Datasource;
import io.coala.agent.AgentStatusObserver;
import io.coala.agent.AgentStatusUpdate;
import io.coala.bind.Binder;
import io.coala.bind.BinderFactory;
import io.coala.capability.admin.CreatingCapability;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.capability.replicate.ReplicationConfig;
import io.coala.exception.CoalaException;
import io.coala.log.LogUtil;
import io.coala.time.SimTime;
import io.coala.time.TimeUnit;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import rx.Observer;

/**
 * {@link ScenarioTest}
 * 
 * @date $Date: 2014-09-28 14:42:17 +0200 (zo, 28 sep 2014) $
 * @version $Revision: 1083 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
// @Ignore
public class ScenarioTest
{

	/** */
	private static final Logger LOG = LogUtil.getLogger(ScenarioTest.class);

	/** */
	public static final long nofHours = (24 * 2);

	/** */
	public static final long nofOccupants = 9;

	/** */
	public static final String scenario = "src/test/resources/usecase.xml";


	/** */
	private static String replicationID = //"testRepl"
	//		+ (System.currentTimeMillis() / 1000)
	 "replication_test_0"
	;

	/** */
	private static Binder binder;

	@BeforeClass
	public static void prepareReplication() throws CoalaException,
			JAXBException
	{
		binder = BinderFactory.Builder
				.fromFile("asimov.properties")
				.withProperty(ReplicationConfig.class,
						ReplicationConfig.MODEL_NAME_KEY, replicationID)
				.build().create("_unittest_");
		binder.inject(Datasource.class).removeEvents();
		binder.inject(Datasource.class).removeMaterials();
		binder.inject(Datasource.class).removeAssemblyLines();
		binder.inject(Datasource.class).removePersons();
		binder.inject(Datasource.class).removeReplication();
		binder.inject(Datasource.class).removeProcesses();
//		UseCase.Util.saveDefaultReplication(binder, new File(scenario),
//				new File(training), projectID, Amount.valueOf(30, Unit.ONE),
//				Amount.valueOf(30, NonSI.DAY));
	}

	@Test
	public void scenarioTest() throws Exception
	{

		final String scenarioName = "testScenario";

		LOG.info("Sim repl duration: " + binder.inject(ReplicationConfig.class));

		LOG.info(binder.getID() + ": Test scenario starting...");

		final CreatingCapability booterSvc = binder
				.inject(CreatingCapability.class);

		final ReplicatingCapability simulator = binder
				.inject(ReplicatingCapability.class);
		final SimTime endOfSimulation = simulator.getTime().plus(2,TimeUnit.DAYS);
		final CountDownLatch latch = new CountDownLatch(1);

		simulator.getTimeUpdates().subscribe(new Observer<SimTime>()
				{
					private SimTime	currentSimTime = simulator.getTime();

					@Override
					public void onNext(final SimTime time)
					{

						if (time.isAfter(endOfSimulation)) {
							try {
								new TimeLineWriter().writeTimeLine();
								new MidasWriter().writeToMidas();
								new EventTraceWriter().writeSimulatorOutput();
								latch.countDown();
							} catch (Exception e) {
								LOG.error("Failed to write output of ASIMOV",e);
								System.exit(1);
							}
						} else if (time.isAfter(currentSimTime)) {
							System.out.println("Simulator " + simulator.getClockID()
								+ " time is now " + time);
							currentSimTime = time;
						}
					}

					@Override
					public void onCompleted()
					{
					}

					@Override
					public void onError(final Throwable t)
					{
						t.printStackTrace();
					}
				});


		booterSvc.createAgent(scenarioName,
				ScenarioManagementOrganization.class).subscribe(
				new AgentStatusObserver()
				{

					@Override
					public void onNext(final AgentStatusUpdate update)
					{
						LOG.info(binder.getID() + ": " + scenarioName
								+ " agent status now " + update.getStatus());

						if (update.getStatus().isInitializedStatus())
						{
							LOG.info(binder.getID() + ": Starting sim!");
							simulator.start();
						} 
					}

					@Override
					public void onError(final Throwable e)
					{
						e.printStackTrace();
					}

					@Override
					public void onCompleted()
					{
						LOG.info(binder.getID() + ": " + scenarioName
								+ " status updates COMPLETED");
					}
				});

		// wait for scenario agent to finish
		latch.await();

		LOG.info(binder.getID() + ": Test scenario complete");
	}
}
