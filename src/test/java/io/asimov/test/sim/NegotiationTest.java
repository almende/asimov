package io.asimov.test.sim;

import io.coala.agent.AgentID;
import io.coala.agent.AgentStatusUpdate;
import io.coala.agent.BasicAgent;
import io.coala.bind.Binder;
import io.coala.bind.BinderFactory;
import io.coala.capability.admin.CreatingCapability;
import io.coala.capability.admin.DestroyingCapability;
import io.coala.capability.know.ReasoningCapability;
import io.coala.exception.CoalaException;
import io.coala.jsa.sl.SLParsableSerializable;
import io.coala.log.LogUtil;
import io.coala.model.ModelID;
import jade.semantics.kbase.filters.FilterKBase;
import jade.util.leap.Iterator;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import rx.Observer;
@Ignore
public class NegotiationTest
{

	private BinderFactory factory = null;

	private ModelID testModel;

	static AgentID unitTestAgentID;

	private CountDownLatch TEST_RUNNING_STATE;

	private final int TEST_RUNNING_STATE_IDLE = 2;

	private final int TEST_RUNNING_STATE_STARTED = 1;

	private final int TEST_RUNNING_STATE_DONE = 0;

	private static long counter = 0;

	private Binder binder;

	ReasoningCapability reasonerService;

	public NegotiationTest() throws CoalaException
	{
		this.platform = Platform.getInstance();
		factory = BinderFactory.Builder.fromFile("asimov.properties").build();
		TEST_RUNNING_STATE = new CountDownLatch(TEST_RUNNING_STATE_IDLE);
	}

	private static final Logger LOG = LogUtil.getLogger(NegotiationTest.class);

	private Platform platform;

	public synchronized void waitForReady()
	{
		if (TEST_RUNNING_STATE.getCount() == TEST_RUNNING_STATE_IDLE)
			TEST_RUNNING_STATE.countDown();
		else if (TEST_RUNNING_STATE.getCount() == TEST_RUNNING_STATE_DONE)
			TEST_RUNNING_STATE = new CountDownLatch(TEST_RUNNING_STATE_STARTED);
		else
		{
			try
			{
				TEST_RUNNING_STATE.await();
			} catch (InterruptedException e)
			{
				// nothing to do here
			}
			waitForReady();
		}
	}

	@Before
	public void prepare() throws CoalaException
	{
		waitForReady();
		platform.agents.clear();
		testModel = new ModelID("test" + (counter++));
		unitTestAgentID = new AgentID(testModel, "unitTest");
		binder = factory.create(unitTestAgentID, BasicAgent.class);
		reasonerService = binder.inject(ReasoningCapability.class);
		platform.agents.put("room1", factory.create(new AgentID(binder.getID()
				.getModelID(), "room1"), NegotiationTestAgent.class));
		platform.agents.put("room2", factory.create(new AgentID(binder.getID()
				.getModelID(), "room2"), NegotiationTestAgent.class));
		platform.agents
				.put("patient1", factory.create(new AgentID(binder.getID()
						.getModelID(), "patient1"), NegotiationTestAgent.class));
		platform.agents
				.put("process1", factory.create(new AgentID(binder.getID()
						.getModelID(), "process1"), NegotiationTestAgent.class));
		platform.agents
				.put("process2", factory.create(new AgentID(binder.getID()
						.getModelID(), "process2"), NegotiationTestAgent.class));
	}

	@Test
	public void allocationSequential() throws Exception
	{
		final CountDownLatch successLatch = new CountDownLatch(1);
		final CountDownLatch failureLatch = new CountDownLatch(1);

		final Observer<AgentStatusUpdate> latchObserver = new Observer<AgentStatusUpdate>()
		{
			
			@Override
			public void onCompleted()
			{
				System.err.println("COMPLETED");
				System.err.flush();; // nothing
			}

			@Override
			public void onError(Throwable e)
			{
				org.junit.Assert.fail(e.getMessage());

			}

			@Override
			public void onNext(AgentStatusUpdate args)
			{
				System.err.flush();
				if (args.getStatus().isCompleteStatus())
				{
					final ReasoningCapability r = platform.agents.get(
							args.getAgentID().getValue()).inject(
							ReasoningCapability.class);
					r.queryToKBase(
							r.toQuery(
									new SLParsableSerializable(
											"(and (room ??x ??agent) (patient ??y ??agent))"),
									"agent", args.getAgentID())).subscribe(
							new Observer<Map<String, Object>>()
							{

								@Override
								public void onCompleted()
								{
									// nothing to do here
								}

								@Override
								public void onError(Throwable e)
								{
									e.printStackTrace();
									org.junit.Assert.fail(e.getMessage());
								}

								@Override
								public void onNext(Map<String, Object> args)
								{
									if (args == null)
										failureLatch.countDown();
									else
										successLatch.countDown();
									Iterator it =  ((FilterKBase)r.getKBase()).toStrings().iterator();
									
									while (it.hasNext())
										LOG.info((String)it.next());
								}
							});
				}
			}

		};
//		platform.agents
//				.get("room1")
//				.inject(CreatingCapability.class)
//				.createAgent(platform.agents.get("room1").getID(),
//						NegotiationTestAgent.class);
//		platform.agents
//				.get("room2")
//				.inject(CreatingCapability.class)
//				.createAgent(platform.agents.get("room2").getID(),
//						NegotiationTestAgent.class);
//		platform.agents
//				.get("patient1")
//				.inject(CreatingCapability.class)
//				.createAgent(platform.agents.get("patient1").getID(),
//						NegotiationTestAgent.class);
		platform.agents
				.get("process2")
				.inject(CreatingCapability.class)
				.createAgent(platform.agents.get("process2").getID(),
						NegotiationTestAgent.class).subscribe(latchObserver);
		
		platform.agents
				.get("process1")
				.inject(CreatingCapability.class)
				.createAgent(platform.agents.get("process1").getID(),
						NegotiationTestAgent.class).subscribe(latchObserver);
		successLatch.await(10, TimeUnit.SECONDS);
		failureLatch.await(2, TimeUnit.SECONDS);
		platform.agents.get("room1").inject(DestroyingCapability.class).destroy();
		platform.agents.get("room2").inject(DestroyingCapability.class).destroy();
		platform.agents.get("patient1").inject(DestroyingCapability.class).destroy();
		TEST_RUNNING_STATE.countDown();
		if (failureLatch.getCount() > 0 || successLatch.getCount() > 0)
			org.junit.Assert.fail();
		LOG.trace("done");
	}

	@Test
	public void allocationSerial() throws Exception
	{
		final CountDownLatch successLatch = new CountDownLatch(1);
		final CountDownLatch failureLatch = new CountDownLatch(1);

		final Observer<AgentStatusUpdate> latchObserver = new Observer<AgentStatusUpdate>()
		{

			@Override
			public void onCompleted()
			{
				; // nothing
			}

			@Override
			public void onError(Throwable e)
			{
				org.junit.Assert.fail(e.getMessage());

			}

			@Override
			public void onNext(AgentStatusUpdate args)
			{
				if (args.getStatus().isCompleteStatus())
				{
					ReasoningCapability r = platform.agents.get(
							args.getAgentID().getValue()).inject(
							ReasoningCapability.class);
					r.queryToKBase(
							r.toQuery(
									new SLParsableSerializable(
											"(and (room ??x ??agent) (patient ??y ??agent))"),
									"agent", args.getAgentID())).subscribe(
							new Observer<Map<String, Object>>()
							{

								@Override
								public void onCompleted()
								{
									// nothing to do here
								}

								@Override
								public void onError(Throwable e)
								{
									e.printStackTrace();
									org.junit.Assert.fail(e.getMessage());
								}

								@Override
								public void onNext(Map<String, Object> args)
								{
									LOG.info("GOT:" + args);
									if (args == null)
										failureLatch.countDown();
									else
										successLatch.countDown();
								}
							});
				}
			}

		};
//		platform.agents
//				.get("room1")
//				.inject(CreatingCapability.class)
//				.createAgent(platform.agents.get("room1").getID(),
//						NegotiationTestAgent.class);
//		platform.agents
//				.get("room2")
//				.inject(CreatingCapability.class)
//				.createAgent(platform.agents.get("room2").getID(),
//						NegotiationTestAgent.class);
//		platform.agents
//				.get("patient1")
//				.inject(CreatingCapability.class)
//				.createAgent(platform.agents.get("patient1").getID(),
//						NegotiationTestAgent.class);
		final Thread t2 = new Thread()
		{
			public void run()
			{
				LOG.trace("adding process2");
				try
				{
					platform.agents
							.get("process2")
							.inject(CreatingCapability.class)
							.createAgent(platform.agents.get("process2").getID(),
									NegotiationTestAgent.class)
							.subscribe(latchObserver);
				} catch (Exception e)
				{
					org.junit.Assert.fail(e.getMessage());
				}
			}
		};
		final Thread t1 = new Thread()
		{
			public void run()
			{
				LOG.trace("adding process1");
				try
				{
					platform.agents
							.get("process1")
							.inject(CreatingCapability.class)
							.createAgent(platform.agents.get("process1").getID(),
									NegotiationTestAgent.class)
							.subscribe(latchObserver);
				} catch (Exception e)
				{
					org.junit.Assert.fail(e.getMessage());
				}
				t2.start();
			}
		};
		t1.start();

		successLatch.await(10, TimeUnit.SECONDS);
		failureLatch.await(2, TimeUnit.SECONDS);
		platform.agents.get("room1").inject(DestroyingCapability.class).destroy();
		platform.agents.get("room2").inject(DestroyingCapability.class).destroy();
		platform.agents.get("patient1").inject(DestroyingCapability.class).destroy();

		TEST_RUNNING_STATE.countDown();
		if (failureLatch.getCount() > 0 || successLatch.getCount() > 0)
			org.junit.Assert.fail();
		LOG.trace("done");
	}

	@Test
	public void allocationParallel() throws Exception
	{
		final CountDownLatch successLatch = new CountDownLatch(1);
		final CountDownLatch failureLatch = new CountDownLatch(1);
		
		final Observer<AgentStatusUpdate> latchObserver = new Observer<AgentStatusUpdate>()
		{

			@Override
			public void onCompleted()
			{
				; // nothing
			}

			@Override
			public void onError(Throwable e)
			{
				org.junit.Assert.fail(e.getMessage());

			}

			@Override
			public void onNext(AgentStatusUpdate args)
			{
				if (args.getStatus().isCompleteStatus())
				{
					ReasoningCapability r = platform.agents.get(
							args.getAgentID().getValue()).inject(
							ReasoningCapability.class);
					r.queryToKBase(
							r.toQuery(
									new SLParsableSerializable(
											"(and (room ??x ??agent) (patient ??y ??agent))"),
									"agent", args.getAgentID())).subscribe(
							new Observer<Map<String, Object>>()
							{

								@Override
								public void onCompleted()
								{
									// nothing to do here
								}

								@Override
								public void onError(Throwable e)
								{
									e.printStackTrace();
									org.junit.Assert.fail(e.getMessage());
								}

								@Override
								public void onNext(Map<String, Object> args)
								{
									LOG.info("GOT:" + args);
									if (args == null)
										failureLatch.countDown();
									else
										successLatch.countDown();
								}
							});
				}
			}

		};
//		platform.agents
//				.get("room1")
//				.inject(CreatingCapability.class)
//				.createAgent(platform.agents.get("room1").getID(),
//						NegotiationTestAgent.class);
//		platform.agents
//				.get("room2")
//				.inject(CreatingCapability.class)
//				.createAgent(platform.agents.get("room2").getID(),
//						NegotiationTestAgent.class);
//		platform.agents
//				.get("patient1")
//				.inject(CreatingCapability.class)
//				.createAgent(platform.agents.get("patient1").getID(),
//						NegotiationTestAgent.class);
		final PausableThreadPoolExecutor es = new PausableThreadPoolExecutor(2);
		es.pause();
		es.execute(new Runnable()
		{
			public void run()
			{
				LOG.trace("adding process2");
				try
				{
					platform.agents
							.get("process2")
							.inject(CreatingCapability.class)
							.createAgent(platform.agents.get("process2").getID(),
									NegotiationTestAgent.class)
							.subscribe(latchObserver);
				} catch (Exception e)
				{
					org.junit.Assert.fail(e.getMessage());
				}
			}
		});
		es.execute(new Runnable()
		{
			public void run()
			{
				LOG.trace("adding process1");
				try
				{
					platform.agents
							.get("process1")
							.inject(CreatingCapability.class)
							.createAgent(platform.agents.get("process1").getID(),
									NegotiationTestAgent.class)
							.subscribe(latchObserver);
				} catch (Exception e)
				{
					org.junit.Assert.fail(e.getMessage());
				}
			}
		});
		es.resume();

		successLatch.await(10, TimeUnit.SECONDS);
		failureLatch.await(2, TimeUnit.SECONDS);
		platform.agents.get("room1").inject(DestroyingCapability.class).destroy();
		platform.agents.get("room2").inject(DestroyingCapability.class).destroy();
		platform.agents.get("patient1").inject(DestroyingCapability.class).destroy();

		TEST_RUNNING_STATE.countDown();
		if (failureLatch.getCount() > 0 || successLatch.getCount() > 0)
			org.junit.Assert.fail();
		LOG.trace("done");
	}

	class PausableThreadPoolExecutor extends ThreadPoolExecutor
	{
		private boolean isPaused;
		private ReentrantLock pauseLock = new ReentrantLock();
		private Condition unpaused = pauseLock.newCondition();

		public PausableThreadPoolExecutor(int poolSize)
		{
			super(poolSize, poolSize, 0L,
					java.util.concurrent.TimeUnit.NANOSECONDS,
					new LinkedBlockingQueue<Runnable>());
		}

		protected void beforeExecute(Thread t, Runnable r)
		{
			super.beforeExecute(t, r);
			pauseLock.lock();
			try
			{
				while (isPaused)
					unpaused.await();
			} catch (InterruptedException ie)
			{
				t.interrupt();
			} finally
			{
				pauseLock.unlock();
			}
		}

		public void pause()
		{
			pauseLock.lock();
			try
			{
				isPaused = true;
			} finally
			{
				pauseLock.unlock();
			}
		}

		public void resume()
		{
			pauseLock.lock();
			try
			{
				isPaused = false;
				unpaused.signalAll();
			} finally
			{
				pauseLock.unlock();
			}
		}
	}

}
