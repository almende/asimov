package io.asimov.run;

import io.asimov.agent.scenario.ScenarioManagementOrganization;
import io.asimov.db.Datasource;
import io.asimov.db.mongo.MongoDatasource;
import io.asimov.model.TraceService;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.xml.XmlUtil;
import io.asimov.vis.timeline.VisJSTimelineUtil;
import io.asimov.xml.SimulationFile;
import io.asimov.xml.SimulationFile.Simulations;
import io.asimov.xml.SimulationFile.Simulations.SimulationCase;
import io.asimov.xml.TAgentBasedSimulationModuleOutput;
import io.asimov.xml.TEventTrace;
import io.coala.agent.AgentStatusObserver;
import io.coala.agent.AgentStatusUpdate;
import io.coala.bind.Binder;
import io.coala.bind.BinderFactory;
import io.coala.capability.admin.CreatingCapability;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.capability.replicate.ReplicationConfig;
import io.coala.exception.CoalaException;
import io.coala.log.LogUtil;
import io.coala.time.SimDuration;
import io.coala.time.SimTime;
import io.coala.time.TimeUnit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import rx.Observer;

/**
 * {@link RunUseCase}
 * 
 * @date $Date: 2014-09-28 14:42:17 +0200 (zo, 28 sep 2014) $
 * @version $Revision: 1083 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class RunUseCase
{

	/** */
	private static final Logger LOG = LogUtil.getLogger(RunUseCase.class);

//	/** */
//	public static final long nofHours = (24 * 7);
//
//	/** */
//	public static final long nofOccupants = 9;

	/** */
	private static String replicationID = "arum";
	
	public static File sourceFile = null;
	
	private static File targetDirectory = null;
	
	private static long durationInMillis = 0;

	/** */
	private static Binder binder;
	
	
	 /* 
     * Copies an entire folder out of a jar to a physical location.  
     */
       private static void copyJarFolder(String jarName, String folderName) {
    	 System.out.println("Copying output template from "+jarName+" folder "+folderName);
         try {
              ZipFile z = new ZipFile(jarName);
              Enumeration<? extends ZipEntry> entries = z.entries();
              while (entries.hasMoreElements()) {
                   ZipEntry entry = (ZipEntry)entries.nextElement();
                   if (entry.getName().startsWith(folderName)) {
                        File f = new File(entry.getName());
                        if (entry.isDirectory()) {
                             f.mkdir();
                        }
                        else if (!f.exists()) {
                             if (copyFromJar(entry.getName(), new File(targetDirectory.getAbsolutePath()+"/"+entry.getName()))) {
                                  System.out.println("Copied: " + entry.getName());
                             }
                        }
                   }
              }
              z.close();
         }
         catch (IOException e) {
              e.printStackTrace();
         }
    }
	
    /* 
     * Copies a file out of the jar to a physical location.  
     *    Doesn't need to be private, uses a resource stream, so may have
     *    security errors if ran from webstart application 
     */
    public static boolean copyFromJar(String sResource, File fDest) {
         if (sResource == null || fDest == null) return false;
         InputStream sIn = null;
         OutputStream sOut = null;
         @SuppressWarnings("unused")
		File sFile = null;
         try {
              fDest.getParentFile().mkdirs();
              sFile = new File(sResource);
         }
         catch(Exception e) {}
         try {
              int nLen = 0;
              sIn = RunUseCase.class.getResourceAsStream("/"+sResource);
              if (sIn == null)
                   throw new IOException("Error copying from jar"  + 
                        "(" + sResource + " to " + fDest.getPath() + ")");
              sOut = new FileOutputStream(fDest);
              byte[] bBuffer = new byte[1024];
              while ((nLen = sIn.read(bBuffer)) > 0)
                   sOut.write(bBuffer, 0, nLen);
              sOut.flush();
         }
         catch(IOException ex) {
              ex.printStackTrace();
         }
         finally {
              try {
                   if (sIn != null)
                        sIn.close();
                   if (sOut != null)
                        sOut.close();
              }
              catch (IOException eError) {
                   eError.printStackTrace();
              }
         }
         return fDest.exists();
    }
	
	public static void main(String[] args) {
		if (args == null || args.length != 3) {
			LOG.error("USAGE: java -jar asimov.jar RunUseCase <inputxmlfilename> <durationInDays> <outputdirectory>");
			System.exit(1);
		} else {
			int mb = 1024 * 1024;

			// Getting the runtime reference from system
			Runtime runtime = Runtime.getRuntime();

			System.out.println("##### Heap utilization statistics [MB] #####");

			// Print used memory
			System.out.println("Used Memory:"
					+ (runtime.totalMemory() - runtime.freeMemory()) / mb + "MB");

			// Print free memory
			System.out.println("Free Memory:" + runtime.freeMemory() / mb + "MB");

			// Print total available memory
			System.out.println("Total Memory:" + runtime.totalMemory() / mb + "MB");

			// Print Maximum available memory
			System.out.println("Max Memory:" + runtime.maxMemory() / mb + "MB");
			if ((runtime.maxMemory()/mb) < 1500) {
				LOG.error("A max memory of 2GB is adviced please add java options -XX:PermSize=512m -XX:MaxPermSize=2048m -Xms256m -Xmx2048m");
			}
			sourceFile = new File(args[0]);
			durationInMillis = SimDuration.ZERO.plus(Double.valueOf(args[1]),TimeUnit.DAYS).getMillis();
			targetDirectory = new File(args[2]);
			if (targetDirectory.isDirectory())
				LOG.warn("Overwriting results in "+targetDirectory.getAbsolutePath());
			else {
				targetDirectory.mkdir();
			}
			try {
				copyJarFolder(getJarFileName(RunUseCase.class), "gui");
			} catch (Exception e1) {
				LOG.error("Failed to create output template", e1);
				System.exit(1);

			}
			try {
				System.out.println("Preparing..");
				prepareUseCase();
			} catch (CoalaException e) {
				LOG.error("Initialization error:",e);
				System.exit(1);
			} catch (JAXBException e) {
				LOG.error("Initialization error while parsing xml:",e);
				System.exit(1);
			}
			try {
				System.out.println("Initializing..");
				runUseCaseForDurationInMs(durationInMillis);
			} catch (Exception e) {
				LOG.error("An error occured during simulation bootstrap",e);
				System.exit(1);
			}
		}
		
	}
	
	public static String getJarFileName(@SuppressWarnings("rawtypes") Class aclass) throws Exception {
		  CodeSource codeSource = aclass.getProtectionDomain().getCodeSource();

		  File jarFile;

		  if (codeSource.getLocation() != null) {
		    jarFile = new File(codeSource.getLocation().toURI());
		  }
		  else {
		    String path = aclass.getResource(aclass.getSimpleName() + ".class").getPath();
		    String jarFilePath = path.substring(path.indexOf(":") + 1, path.indexOf("!"));
		    jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
		    jarFile = new File(jarFilePath);
		  }
		  return jarFile.getAbsolutePath();
		}

	public static void prepareUseCase() throws CoalaException,
			JAXBException
	{
		try {
			binder = BinderFactory.Builder
					.fromFile("asimov.properties")
					.withProperty(ReplicationConfig.class,
							ReplicationConfig.MODEL_NAME_KEY, replicationID)
					.build().create("_asimov_");
		} catch (Exception e) {
			LOG.error("Failed to open usecase input xml file.",e);
			System.exit(1);
		}
		binder.inject(Datasource.class).removeEvents();
		binder.inject(Datasource.class).removeResourceDescriptors();
		binder.inject(Datasource.class).removeReplication();
		binder.inject(Datasource.class).removeProcesses();
	}

	public static void runUseCaseForDurationInMs(long durationInMillis) throws Exception
	{

		final String scenarioName = "testScenario";
		binder.inject(ReplicationConfig.class).setProperty("scenarioFile", sourceFile.getAbsolutePath());
		System.out.println("Asimov settings info: " + binder.inject(ReplicationConfig.class));

		LOG.info(binder.getID() + ": Test scenario starting...");

		final CreatingCapability booterSvc = binder
				.inject(CreatingCapability.class);

		final ReplicatingCapability simulator = binder
				.inject(ReplicatingCapability.class);
		final SimTime endOfSimulation = simulator.getTime().plus(durationInMillis,TimeUnit.MILLIS);
		simulator.getTimeUpdates().subscribe(new Observer<SimTime>()
		{
			private SimTime	currentSimTime = simulator.getTime();

			@Override
			public void onNext(final SimTime time)
			{

				if (time.isAfter(endOfSimulation)) {
					try {
						RunUseCase.writeTimeLine(targetDirectory);
						RunUseCase.writeSimulatorOutput(new File(targetDirectory.getCanonicalPath()+"/"+replicationID+"_output.xml"));
					} catch (Exception e) {
						LOG.error("Failed to write output of ASIMOV",e);
						System.exit(1);
					}
					System.out.println("Done wrote results to directory: "+targetDirectory.getAbsolutePath());
					System.exit(0);
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

		final CountDownLatch latch = new CountDownLatch(1);
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
						} else if (update.getStatus().isFailedStatus())
						{
							latch.countDown();
							LOG.info(binder.getID()
									+ ": Agent failed, remaining: "
									+ latch.getCount());
						} else if (update.getStatus().isFinishedStatus())
						{
							latch.countDown();
							LOG.info(binder.getID()
									+ ": Agent finished, remaining: "
									+ latch.getCount());
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
		
		LOG.error("Did not reach end of simulation");
		System.exit(1);
	}
	
	public static void writeTimeLine(File targetDirectory) throws Exception
	{
		int mb = 1024 * 1024;

		// Getting the runtime reference from system
		Runtime runtime = Runtime.getRuntime();

		System.out.println("##### Heap utilization statistics [MB] #####");

		// Print used memory
		System.out.println("Used Memory:"
				+ (runtime.totalMemory() - runtime.freeMemory()) / mb);

		// Print free memory
		System.out.println("Free Memory:" + runtime.freeMemory() / mb);

		// Print total available memory
		System.out.println("Total Memory:" + runtime.totalMemory() / mb);

		// Print Maximum available memory
		System.out.println("Max Memory:" + runtime.maxMemory() / mb);

		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		for (ActivityEvent e : TraceService.getInstance(replicationID)
				.getActivityEvents(binder.inject(Datasource.class)))
				events.add(e);
		LOG.info("Loaded events");
		VisJSTimelineUtil.writeTimelineData(events,targetDirectory);
		LOG.info("Wrote timeline");
		LOG.info("done");
	}
	
	public static void writeSimulatorOutput(final File output) throws Exception
	{
		int mb = 1024 * 1024;

		// Getting the runtime reference from system
		Runtime runtime = Runtime.getRuntime();

		System.out.println("##### Heap utilization statistics [MB] #####");

		// Print used memory
		System.out.println("Used Memory:"
				+ (runtime.totalMemory() - runtime.freeMemory()) / mb);

		// Print free memory
		System.out.println("Free Memory:" + runtime.freeMemory() / mb);

		// Print total available memory
		System.out.println("Total Memory:" + runtime.totalMemory() / mb);

		// Print Maximum available memory
		System.out.println("Max Memory:" + runtime.maxMemory() / mb);

		TraceService trace = TraceService.getInstance(replicationID);
		final TEventTrace xmlOutput = trace.toXML(MongoDatasource.getInstance(replicationID));
		SimulationFile simFile = new SimulationFile();
		simFile.setId(replicationID);
		simFile.setSimulations(new Simulations());
		simFile.getSimulations().getSimulationCase().add(new SimulationCase());
		simFile.getSimulations().getSimulationCase().get(0).setSimulationResult(new TAgentBasedSimulationModuleOutput());
		simFile.getSimulations().getSimulationCase().get(0).getSimulationResult().setEventTrace(xmlOutput);
		XmlUtil.getCIMMarshaller().marshal(simFile, output);
		LOG.info("Worte the following event trace to "+output.getAbsolutePath()+output.getName());
		XmlUtil.getCIMMarshaller().marshal(xmlOutput, System.out);
	}
}
