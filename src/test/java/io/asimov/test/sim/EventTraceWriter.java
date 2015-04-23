package io.asimov.test.sim;

import io.asimov.db.Datasource;
import io.asimov.db.mongo.MongoDatasource;
import io.asimov.model.ASIMOVResourceDescriptor;
import io.asimov.model.TraceService;
import io.asimov.model.xml.XmlUtil;
import io.asimov.xml.SimulationFile;
import io.asimov.xml.SimulationFile.Simulations;
import io.asimov.xml.SimulationFile.Simulations.SimulationCase;
import io.asimov.xml.TAgentBasedSimulationModuleOutput;
import io.asimov.xml.TEventTrace;
import io.asimov.xml.TEventTrace.EventRecord;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.bind.BinderFactory;
import io.coala.capability.configure.ConfiguringCapability;
import io.coala.log.LogUtil;
import io.coala.model.ModelID;

import java.io.File;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * {@link EventTraceWriter}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public class EventTraceWriter {
	private static final Logger LOG = LogUtil.getLogger(EventTraceWriter.class);

	final static String replicationId = "replication_test_0";

	final static File output = new File(replicationId + "_output.xml");
	
	static Binder binder;
	

	@Test
	public void writeSimulatorOutput()
			throws Exception {
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
		
		binder = BinderFactory.Builder.fromFile("asimov.properties").build().create(new AgentID(new ModelID(replicationId), "eventTraceWriter"));


		final boolean includeResouces = binder.inject(ConfiguringCapability.class).getProperty("includeResourcesInEventTrace").getBoolean().booleanValue();
		final boolean includeActivities = binder.inject(ConfiguringCapability.class).getProperty("includeActivitiesInEventTrace").getBoolean().booleanValue();

		
		TraceService trace = TraceService.getInstance(replicationId);
		
		Datasource ds = binder.inject(Datasource.class);
			
		final TEventTrace xmlOutput = trace.toXML(ds,includeResouces,includeActivities);
		
		
		SimulationCase simCase = new SimulationCase();
		SimulationFile simFile = new SimulationFile();
		simFile.withId(replicationId).setSimulations(new Simulations());
		simFile.getSimulations().withSimulationCase(simCase);
		simFile.getSimulations().getSimulationCase().get(0)
				.setSimulationResult(new TAgentBasedSimulationModuleOutput());
		simFile.getSimulations().getSimulationCase().get(0)
				.getSimulationResult().setEventTrace(xmlOutput);
		XmlUtil.getCIMMarshaller().marshal(simFile, output);
		LOG.info("Worte the following event trace to "
				+ output.getAbsolutePath() + output.getName());
		XmlUtil.getCIMMarshaller().marshal(xmlOutput, System.out);
	}
}
