/* $Id: d7b3d1b7959a9d9bfb9fe64f2605548e98a428da $
 * $URL: https://dev.almende.com/svn/abms/coala-common/src/main/java/com/almende/coala/agent/AbstractAgentManager.java $
 * 
 * Part of the EU project Adapt4EE, see http://www.adapt4ee.eu/
 * 
 * @license
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright (c) 2010-2013 Almende B.V. 
 */
package io.coala.agent;

import io.coala.bind.Binder;
import io.coala.bind.BinderFactory;
import io.coala.bind.BinderFactoryConfig;
import io.coala.exception.CoalaException;
import io.coala.exception.CoalaExceptionFactory;
import io.coala.lifecycle.ActivationType;
import io.coala.lifecycle.LifeCycle;
import io.coala.lifecycle.LifeCycleHooks;
import io.coala.lifecycle.MachineUtil;
import io.coala.log.LogUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * {@link AbstractAgentManager} manages the {@link LifeCycle}s of the deployed
 * {@link Agent}s
 * 
 * @date $Date: 2014-08-08 16:20:51 +0200 (Fri, 08 Aug 2014) $
 * @version $Revision: 353 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public abstract class AbstractAgentManager implements AgentManager
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger(AbstractAgentManager.class);

	/** */
	private final SortedMap<AgentID, Agent> agents = Collections
			.synchronizedSortedMap(new TreeMap<AgentID, Agent>());

	/** */
	private final BinderFactory binderFactory;

	/** */
	private Subject<AgentStatusUpdate, AgentStatusUpdate> agentStatusUpdates = PublishSubject
			.create();

	/** */
	private Subject<AgentStatusUpdate, AgentStatusUpdate> wrapperStatusUpdates = PublishSubject
			.create();

	/**
	 * @param status
	 */
	private void updateAgentStatus(final Agent agent,
			final BasicAgentStatus status)
	{
		// try
		// {
		MachineUtil.setStatus(agent, status, status.isFinishedStatus()
				|| status.isFailedStatus());
		// } catch (final Throwable t)
		// {
		// LOG.error(String.format(
		// "Problem updating status of agent %s from %s -> %s",
		// agent == null ? "?" : agent.getID(), agent == null ? "?"
		// : agent.getStatus(), status), t);
		// }
	}

	private final Map<AgentID, Observable<AgentStatusUpdate>> agentStates = new HashMap<>();

	private final Set<AgentID> activatedOnce = Collections
			.synchronizedSet(new HashSet<AgentID>());

	/**
	 * {@link AbstractAgentManager} constructor
	 * 
	 * @throws CoalaException
	 */
	protected AbstractAgentManager(
			final BinderFactory.Builder binderFactoryBuilder)
	{
		this.binderFactory = binderFactoryBuilder.withAgentStatusSource(
				this.agentStatusUpdates.asObservable()).build();
		this.wrapperStatusUpdates.subscribe(new AgentStarter());
		this.agentStatusUpdates.subscribe(new AgentFinisher());
	}

	/**
	 * {@link AbstractAgentManager} constructor
	 * 
	 * @throws CoalaException
	 */
	protected AbstractAgentManager(final Binder binder)
	{
		this.binderFactory = BinderFactory.Builder
				.fromConfig(binder.inject(BinderFactoryConfig.class))
				.withAgentStatusSource(this.agentStatusUpdates.asObservable())
				.build();
		this.wrapperStatusUpdates.subscribe(new AgentStarter());
		this.agentStatusUpdates.subscribe(new AgentFinisher());
	}

	/**
	 * {@link AgentStarter}
	 * 
	 * @version $Revision: 353 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 *
	 */
	protected class AgentStarter implements Observer<AgentStatusUpdate>
	{
		@Override
		public void onCompleted()
		{
			LOG.trace("COMPLETED AgentStarter");
		}

		@Override
		public void onError(final Throwable t)
		{
			t.printStackTrace();
		}

		@Override
		public synchronized void onNext(final AgentStatusUpdate wrapperUpdate)
		{
			// System.err.println("Got wrapper update: "+wrapperUpdate);
			final AgentID agentID = wrapperUpdate.getAgentID();
			final Agent agent = get(agentID, false);
			final LifeCycleHooks hooks = (LifeCycleHooks) agent;
			if (agent == null)
			{
				// onError(new NullPointerException("Agent not found: " +
				// agentID));
				return;
			}
			if (wrapperUpdate.getStatus().isInitializedStatus())
			{
				try
				{
					hooks.initialize();
					if (agent.getStatus().isCreatedStatus())
						updateAgentStatus(agent, BasicAgentStatus.INITIALIZED);
				} catch (final Throwable e)
				{
					System.err
							.println("Problem initializing agent: " + agentID);
					onError(e);
					updateAgentStatus(agent, BasicAgentStatus.FAILED);
				}
			} else if (wrapperUpdate.getStatus().isPassiveStatus())
			{
				final ActivationType activationType = ((LifeCycleHooks) agent)
						.getActivationType();
				switch (activationType)
				{
				case ACTIVATE_NEVER:
					// do nothing, remain paused until agent notifies
					// complete
					if (agent.getStatus().isInitializedStatus())
					{
						updateAgentStatus(agent, BasicAgentStatus.PASSIVE);
					}
					break;

				case ACTIVATE_ONCE:
					if (activatedOnce.contains(agentID))
						return;

					activatedOnce.add(agentID);

				case ACTIVATE_MANY:
					try
					{
						updateAgentStatus(agent, BasicAgentStatus.ACTIVE);
						hooks.activate();
						if (agent.getStatus().isActiveStatus())
						{
							updateAgentStatus(agent, BasicAgentStatus.PASSIVE);
							hooks.deactivate();
						}
					} catch (final Throwable t)
					{
						System.err.println(String.format("Wrapper could not "
								+ "activate/complete agent %s", agentID));
						onError(t);
						updateAgentStatus(agent, BasicAgentStatus.FAILED);
					}
					break;

				case ACTIVATE_AND_FINISH:
					try
					{
						updateAgentStatus(agent, BasicAgentStatus.ACTIVE);
						hooks.activate();
						if (agent.getStatus().isActiveStatus())
						{
							updateAgentStatus(agent, BasicAgentStatus.PASSIVE);
							hooks.deactivate();
							if (agent.getStatus().isPassiveStatus())
								updateAgentStatus(agent,
										BasicAgentStatus.COMPLETE);
						}
					} catch (final Throwable t)
					{
						System.err.println(String.format("Wrapper could not "
								+ "activate/complete agent %s", agentID));
						onError(t);
						updateAgentStatus(agent, BasicAgentStatus.FAILED);
					}
					break;

				default:
					LOG.warn("What to do? Assuming complete after pause"
							+ " for activation type: " + activationType);
					updateAgentStatus(agent, BasicAgentStatus.COMPLETE);
					break;

				}
			} else if (wrapperUpdate.getStatus().isFinishedStatus()
					|| wrapperUpdate.getStatus().isFailedStatus())
			{
				try
				{
					if (!delete(agentID))
						LOG.warn("Failed to remove agent from cache: "
								+ agentID);
				} catch (final Throwable t)
				{
					System.err.println(String.format(
							"Problem destroying agent: %s", agentID));
					onError(t);
				}
			}
		}
	}

	/**
	 * {@link AgentFinisher}
	 * 
	 * @version $Revision: 353 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 *
	 */
	protected class AgentFinisher implements Observer<AgentStatusUpdate>
	{
		@Override
		public void onCompleted()
		{
			LOG.trace("COMPLETED AgentFinisher");
		}

		@Override
		public void onError(final Throwable t)
		{
			t.printStackTrace();
		}

		@Override
		public void onNext(final AgentStatusUpdate update)
		{
			final AgentID agentID = update.getAgentID();
			final Agent agent = get(agentID, false);
			if (agent == null)
			{
				System.err
						.println("Agent already deleted, ignoring state update: "
								+ update.getStatus());
				return;
			}
			if (update.getStatus().isCompleteStatus())
			{
				try
				{
					((LifeCycleHooks) agent).finish();
					updateAgentStatus(agent, BasicAgentStatus.FINISHED);

					// wait for wrapper to close before deleting wrapped
					// agent from cache
				} catch (final Throwable e)
				{
					System.err.println(String.format(
							"Problem while wrapper was finishing for "
									+ "agent: %s", agentID));
					onError(e);
					updateAgentStatus(agent, BasicAgentStatus.FAILED);
				}
			}
		}
	}

	@Override
	public Observable<AgentID> getChildIDs(final AgentID parentID,
			final boolean currentOnly)
	{
		if (!currentOnly)
			return this.agentStatusUpdates.filter(
					new Func1<AgentStatusUpdate, Boolean>()
					{
						@Override
						public Boolean call(final AgentStatusUpdate update)
						{
							return parentID.equals(update.getAgentID()
									.getParentID())
									&& update.getStatus().isCreatedStatus();
						}
					}).map(new Func1<AgentStatusUpdate, AgentID>()
			{
				@Override
				public AgentID call(final AgentStatusUpdate update)
				{
					return update.getAgentID();
				}
			});

		synchronized (this.agents)
		{
			final SortedSet<AgentID> childIDs = new TreeSet<>();
			for (AgentID candidate : this.agents.keySet())
				if (parentID.equals(candidate.getParentID()))
					childIDs.add(candidate);
			return Observable.from(childIDs);
		}
	}

	/** @return the {@link BinderFactory} */
	protected BinderFactory getBinderFactory()
	{
		return this.binderFactory;
	}

	/**
	 * create the agents specified for launch at startup
	 * 
	 * @throws CoalaException
	 */
	protected void bootAgents()
	{
		for (AgentID agentID : this.binderFactory.getConfig().getBootAgentIDs())
			try
			{
				boot(agentID, null);
			} catch (final Exception e)
			{
				LOG.error("Problem creating agent: " + agentID, e);
			}
	}

	/**
	 * @param agentID the identifier of the agent to check exists
	 * @return {@code true} if specified agent was already created,
	 *         {@code false} otherwise
	 */
	protected boolean isCreated(final AgentID agentID)
	{
		return this.agents.containsKey(agentID);
	}

	/** */
	// private final ExecutorService updateExecutor = Executors
	// .newSingleThreadExecutor();

	/**
	 * @param agentID
	 */
	protected void updateWrapperAgentStatus(final AgentID agentID,
			final AgentStatus<?> status)
	{
		if (agentID == null)
		{
			LOG.warn("Ignoring status update",
					CoalaExceptionFactory.VALUE_NOT_SET.create("agentID"));
			return;
		}

		if (!isCreated(agentID))
			LOG.warn("Unknown agent: " + agentID + " reports status: " + status);

		// this.updateExecutor.execute(new Runnable()
		// {
		// @Override
		// public void run()
		// {
		wrapperStatusUpdates
				.onNext(new BasicAgentStatusUpdate(agentID, status));
		// }
		// });
	}

	/**
	 * @return the {@link Observable} stream of {@link AgentStatusUpdate}s for
	 *         wrapper agents
	 */
	@Override
	public Observable<AgentStatusUpdate> getWrapperAgentStatus()
	{
		return this.wrapperStatusUpdates.asObservable();
	}

	/**
	 * @return the {@link Observable} stream of {@link AgentStatusUpdate}s for
	 *         (wrapped) {@link Agent}s
	 */
	@Override
	public Observable<AgentStatusUpdate> getAgentStatus(final AgentID agentID)
	{
		return this.agentStates.get(agentID);
	}

	protected Agent get(final AgentID agentID, final boolean block)
	{
		Agent result = this.agents.get(agentID);

		while (block && result == null)
		{
			synchronized (this.agents)
			{
				LOG.trace("Waiting for agent " + agentID);
				try
				{
					this.agents.wait(1000);
				} catch (final InterruptedException ignore)
				{
				}
				result = this.agents.get(agentID);
			}
		}
		return result;
	}

	protected boolean put(final Agent agent)
	{
		synchronized (this.agents)
		{
			if (!isCreated(agent.getID())
					&& this.agents.put(agent.getID(), agent) == null)
			{
				this.agents.notify();
				return true;
			}
			return false; // another agent with this id already existed
		}
	}

	protected boolean delete(final AgentID agent)
	{
		this.activatedOnce.remove(agent);
		this.agentStates.remove(agent);
		if (this.agents.remove(agent) == null)
			return false;

		getBinderFactory().remove(agent);

		if (size() == 0)
		{
			LOG.info("Last one out closes the door: Shutting down "
					+ getClass().getSimpleName());
			shutdown();
			this.agentStatusUpdates.onCompleted();
			this.wrapperStatusUpdates.onCompleted();
		} else
		{
			LOG.trace(size() + " agent(s) remaining (within wrappers)..");
		}
		return true;
	}

	protected int size()
	{
		return this.agents.size();
	}

	@Override
	public Observable<AgentStatusUpdate> boot(final String agentName)
	{
		return boot(agentName, null);
	}

	@Override
	public Observable<AgentStatusUpdate> boot(final String agentName,
			final Class<? extends Agent> agentType)
	{
		try
		{
			final AgentID agentID = this.binderFactory.getConfig()
					.getReplicationConfig().newID().createAgentID(agentName);
			return boot(agentID, agentType);
		} catch (final Exception e)
		{
			return Observable.error(e);
		}
	}

	@Override
	public Observable<AgentStatusUpdate> boot(final AgentID agentID)
	{
		return boot(agentID, null);
	}

	// private static final ExecutorService WORKERS = Executors
	// .newCachedThreadPool();

	@Override
	public synchronized Observable<AgentStatusUpdate> boot(
			final AgentID agentID, final Class<? extends Agent> agentType)
	{
		if (agentID == null || agentID.getValue().isEmpty())
			return Observable.error(CoalaExceptionFactory.VALUE_NOT_SET.create(
					"agentID", "Must specify the agentID when booting"));

		if (isCreated(agentID))
		{
			LOG.info("Agent already exists, skipping boot for: " + agentID);
			return this.agentStates.get(agentID);
		}

		final Subject<AgentStatusUpdate, AgentStatusUpdate> statusSubject = PublishSubject
				.create();
		final Observable<AgentStatusUpdate> result = statusSubject
				.asObservable();
		this.agentStates.put(agentID, result);
		// this.agents.put(agentID, null);

		// FIXED!! result.subscribe(this.agentStatusUpdates);
		
        result.subscribe(new Action1<AgentStatusUpdate>()
        {
            @Override
            public void call(final AgentStatusUpdate t1)
            {
                agentStatusUpdates.onNext(t1);
            }
        }, new Action1<Throwable>()
        {
            @Override
            public void call(final Throwable t1)
            {
                agentStatusUpdates.onError(t1);
            }
        });

		// WORKERS.submit(new Runnable()
		// {
		//
		// @Override
		// public void run()
		// {
		final AgentFactory agentFactory;
		final Agent agent;
		try
		{
			final Binder binder = getBinderFactory().create(agentID, agentType);
			agentFactory = binder.inject(AgentFactory.class);
			agent = agentFactory.create();
		} catch (final Throwable t)
		{
			return Observable.error(t);
		}

		try
		{
			put(agent);
			agent.getStatusHistory().subscribe(new Observer<BasicAgentStatus>()
			{
				@Override
				public void onError(final Throwable e)
				{
					e.printStackTrace();
				}

				@Override
				public void onNext(final BasicAgentStatus status)
				{
					// System.err.println("STATUS HISTORY next: "
					// + status);
					statusSubject.onNext(new BasicAgentStatusUpdate(agentID,
							status));
				}

				@Override
				public void onCompleted()
				{
					// System.err.println("STATUS HISTORY COMPLETED");
					statusSubject.onCompleted();
				}
			});

			updateAgentStatus(agent, BasicAgentStatus.CREATED);

			LOG.trace("Used " + agentFactory.getClass().getName()
					+ " to create " + agent.getClass().getName());

			boot(agent);
		} catch (final Throwable e)
		{
			statusSubject.onError(e);

			updateAgentStatus(agent, BasicAgentStatus.FAILED);
		}
		// }
		// });
		return result;
	}

	/**
	 * @param agent the (pre-cached) {@link Agent} to boot
	 * @return the agent's {@link AgentID}
	 * @throws CoalaException if agent wrapping failed
	 */
	protected abstract AgentID boot(Agent agent) throws CoalaException;

	protected abstract void shutdown();

}
