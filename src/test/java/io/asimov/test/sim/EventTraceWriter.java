package io.asimov.test.sim;

import io.asimov.db.mongo.MongoDatasource;
import io.asimov.model.TraceService;
import io.asimov.model.xml.XmlUtil;
import io.asimov.xml.SimulationFile;
import io.asimov.xml.SimulationFile.Simulations;
import io.asimov.xml.SimulationFile.Simulations.SimulationCase;
import io.asimov.xml.TAgentBasedSimulationModuleOutput;
import io.asimov.xml.TEventTrace;
import io.coala.log.LogUtil;

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
public class EventTraceWriter
{
	private static final Logger LOG = LogUtil.getLogger(EventTraceWriter.class);

	final static String replicationId = "replication_test_0";
	
	final static File output = new File(replicationId+"_output.xml");

	@Test
	public void writeSimulatorOutput() throws Exception
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

		TraceService trace = TraceService.getInstance(replicationId);
		final TEventTrace xmlOutput = trace.toXML(MongoDatasource.getInstance(replicationId));
		SimulationFile simFile = new SimulationFile();
		simFile.setId(replicationId);
		simFile.setSimulations(new Simulations());
		simFile.getSimulations().getSimulationCase().add(new SimulationCase());
		simFile.getSimulations().getSimulationCase().get(0).setSimulationResult(new TAgentBasedSimulationModuleOutput());
		simFile.getSimulations().getSimulationCase().get(0).getSimulationResult().setEventTrace(xmlOutput);
		XmlUtil.getCIMMarshaller().marshal(simFile, output);
		LOG.info("Worte the following event trace to "+output.getAbsolutePath()+output.getName());
		XmlUtil.getCIMMarshaller().marshal(xmlOutput, System.out);
	}
}

