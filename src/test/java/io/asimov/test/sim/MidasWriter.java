package io.asimov.test.sim;

import io.arum.model.events.PersonEvent;
import io.asimov.db.Datasource;
import io.asimov.db.mongo.MongoDatasource;
import io.asimov.model.TraceService;
import io.asimov.vis.timeline.VisJSTimelineUtil;
import io.coala.log.LogUtil;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * {@link MidasWriter}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public class MidasWriter
{

	private static final Logger LOG = LogUtil.getLogger(MidasWriter.class);

	final static String replicationId = "replication_test_0";

	private static final boolean INCLUDE_MATERIALS = true;

	@Test
	public void writeToMidas() throws Exception
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
		Datasource ds = MongoDatasource.getInstance(replicationId);
		List<PersonEvent<?>> events = TraceService.getInstance(replicationId)
				.getEvents(ds);
		LOG.info("Loaded events");
		VisJSTimelineUtil.writeMidasData(events,ds,INCLUDE_MATERIALS);
		LOG.info("Wrote midas data");
		LOG.info("done");
	}
}
