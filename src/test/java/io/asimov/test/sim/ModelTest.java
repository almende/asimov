package io.asimov.test.sim;

import io.coala.agent.AgentStatusObserver;
import io.coala.agent.AgentStatusUpdate;
import io.coala.bind.Binder;
import io.coala.bind.BinderFactory;
import io.coala.capability.admin.CreatingCapability;
import io.coala.capability.replicate.ReplicationConfig;
import io.coala.log.LogUtil;

import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

/**
 * {@link ModelTest}
 * 
 * @date $Date: 2014-09-19 09:38:53 +0200 (vr, 19 sep 2014) $
 * @version $Revision: 1072 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Ignore
public class ModelTest
{

	/** */
	private static final Logger LOG = LogUtil.getLogger(ModelTest.class);

	private static CountDownLatch latch = null;

	@Test
	public void run() throws Exception
	{
		latch = new CountDownLatch(1);
		final Binder binder = BinderFactory.Builder
				.fromFile()
				.withProperty(ReplicationConfig.class,
						ReplicationConfig.MODEL_NAME_KEY,
						"testModel" + System.currentTimeMillis()).build()
				.create("testAgent", ASIMOVMessageSenderAgent.class);
		// binder2 = factory.create("testAgent2");

		// boot the agent to wrap
		binder.inject(CreatingCapability.class)
				.createAgent("senderAgent", ASIMOVMessageSenderAgent.class)
				.subscribe(new AgentStatusObserver()
				{
					@Override
					public void onNext(final AgentStatusUpdate update)
					{
						// FIXME why does LOG not work?
						LOG.trace("Observed status " + update.getStatus()
								+ " of agent: " + update.getAgentID() + ", "
								+ latch.getCount() + "  agent(s) remaining...");

						// fall-back, preferably use onCompleted()
						if (update.getStatus().isFinishedStatus()
								|| update.getStatus().isFailedStatus())
							latch.countDown();
					}

					@Override
					public void onCompleted()
					{
						// FIXME why is this never called?
						System.err.println("COMPLETED StatusObserver");
					}

					@Override
					public void onError(final Throwable e)
					{
						e.printStackTrace();
					}
				});

		LOG.trace("Awaiting completion of agent " + binder.getID());
		latch.await();
	}
}
