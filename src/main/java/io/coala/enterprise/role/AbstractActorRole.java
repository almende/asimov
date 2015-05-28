/* $Id: 9e2e41927242e225924e6fc5190c3e65aaadb0c6 $
 * $URL: https://dev.almende.com/svn/abms/enterprise-ontology/src/main/java/io/coala/enterprise/role/AbstractActorRole.java $
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
 * Copyright (c) 2010-2014 Almende B.V. 
 */
package io.coala.enterprise.role;

import io.coala.agent.Agent;
import io.coala.agent.AgentID;
import io.coala.agent.AgentStatusUpdate;
import io.coala.bind.Binder;
import io.coala.capability.AbstractCapability;
import io.coala.capability.CapabilityID;
import io.coala.capability.admin.CreatingCapability;
import io.coala.capability.admin.DestroyingCapability;
import io.coala.capability.configure.ConfiguringCapability;
import io.coala.capability.interact.ReceivingCapability;
import io.coala.capability.interact.SendingCapability;
import io.coala.capability.know.ReasoningCapability;
import io.coala.capability.plan.SchedulingCapability;
import io.coala.capability.replicate.RandomizingCapability;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.config.PropertyGetter;
import io.coala.enterprise.fact.CoordinationFact;
import io.coala.enterprise.fact.CoordinationFactType;
import io.coala.enterprise.organization.Organization;
import io.coala.exception.CoalaExceptionFactory;
import io.coala.factory.ClassUtil;
import io.coala.invoke.ProcedureCall;
import io.coala.invoke.Schedulable;
import io.coala.log.InjectLogger;
import io.coala.log.LogUtil;
import io.coala.message.Message;
import io.coala.model.ModelComponentIDFactory;
import io.coala.process.Job;
import io.coala.random.RandomDistribution;
import io.coala.time.SimTime;
import io.coala.time.SimTimeFactory;
import io.coala.time.TimeUnit;
import io.coala.time.Trigger;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;
//import javax.inject.Named;
import javax.inject.Named;

import org.apache.log4j.Logger;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action0;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@link AbstractActorRole}
 * 
 * @version $Revision: 330 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * @param <F> the (super)type of {@link CoordinationFact} being handled
 */
public abstract class AbstractActorRole<F extends CoordinationFact> extends
		AbstractCapability<CapabilityID> implements ActorRole<F>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	@InjectLogger
	private Logger LOG;// = LogUtil.getLogger(AbstractActorRole.class);;

	/** */
	@SuppressWarnings("rawtypes")
	@Inject
	@Named(Binder.AGENT_TYPE)
	private Class ownerType;

	/** the type of {@link CoordinationFact} */
	@JsonIgnore
	private Class<F> factType;

	/** */
	@JsonIgnore
	private final Observable<F> facts;

	/**
	 * {@link AbstractActorRole} constructor
	 * 
	 * @param id
	 * @param owner
	 */
	@SuppressWarnings("unchecked")
	@Inject
	protected AbstractActorRole(final Binder binder)
	{
		super(null, binder);
		setID(new ActorRoleID(binder.getID(), getClass()));

		final List<Class<?>> typeArgs = ClassUtil.getTypeArguments(
				AbstractActorRole.class, getClass());
		this.factType = (Class<F>) typeArgs.get(0);
		// LOG.trace("Listening for messages of type: " +
		// this.factType.getName());
		this.facts = getReceiver().getIncoming().ofType(this.factType);
		this.facts.subscribe(new Observer<F>()
		{
			@Override
			public void onNext(final F fact)
			{
				final SimTime now = getTime();
				// LOG.trace("SCHEDULING FACT: " + fact + " AT " + now);
				getSimulator().schedule(
						ProcedureCall.create(AbstractActorRole.this,
								AbstractActorRole.this, FACT_HANDLER, fact),
						Trigger.createAbsolute(now));
				// LOG.trace("SCHEDULED FACT: " + fact + " AT " + now);
			}

			@Override
			public void onCompleted()
			{
				//
			}

			@Override
			public void onError(final Throwable e)
			{
				e.printStackTrace();
			}
		});
	}

	/** @param fact the ignored {@link CoordinationFact} to log */
	protected void logIgnore(final F fact, final boolean expired)
	{
		final CoordinationFactType factType = fact.getID().getType();
		final ActorRoleType roleType = expired ? factType.originatorRoleType()
				: factType.responderRoleType();
		final CoordinationFactType proceedType = factType.getDefaultResponse(
				roleType, true).outcome();
		final CoordinationFactType receedType = factType.getDefaultResponse(
				roleType, false).outcome();
		LOG.trace(String.format("%s ignoring %s (%s), default response type: "
				+ "%s to proceed or %s otherwise", roleType,
				(expired ? "expiration of " : "") + factType, fact.getClass()
						.getSimpleName(), proceedType, receedType));
	}

	@Override
	public ActorRoleID getID()
	{
		return (ActorRoleID) super.getID();
	}

	/** @see ModelComponent#getOwnerID() */
	@Override
	public AgentID getOwnerID()
	{
		return getID().getOwnerID();
	}

	/** @return the type of this {@link ActorRole}'s owner {@link Organization} */
	@SuppressWarnings("unchecked")
	public Class<? extends Organization> getOwnerType()
	{
		return (Class<? extends Organization>) this.ownerType;
	}

	/*	private class FactHandler implements Observer<F>
		{
			*//** @see Observer#onError(Throwable) */
	/*
	@Override
	public void onError(final Throwable t)
	{
	t.printStackTrace();
	}

	*//** @see Observer#onNext(Object) */
	/*
	@Override
	public void onNext(final F fact)
	{
	// don't handle immediately, may still be constructing the role!
	final SimTime now = getTime();
	LOG.info("SCHEDULING FACT:" + fact + " AT " + now);
	getSimulator().schedule(
	ProcedureCall.create(AbstractActorRole.this,
	AbstractActorRole.this, FACT_HANDLER, fact),
	Trigger.createAbsolute(now));
	};

	*//** @see Observer#onCompleted() */
	/*
	@Override
	public void onCompleted()
	{
	// empty
	}
	}
	*/
	private static final String FACT_HANDLER = "factHandler";

	@Schedulable(FACT_HANDLER)
	public void handleFact(F fact)
	{
		// System.err.println("HANDLING FACT:" + fact);
		try
		{
			switch (fact.getID().getType())
			{
			case ACCEPTED:
				asExecutor().onAccepted(fact);
				break;
			case QUIT:
				asExecutor().onQuit(fact);
				break;
			case REJECTED:
				asExecutor().onRejected(fact);
				break;
			case REQUESTED:
				asExecutor().onRequested(fact);
				break;
			case STOPPED:
				asExecutor().onStopped(fact);
				break;
			case _ALLOWED_PROMISE_CANCELLATION:
				asExecutor().onAllowedPromiseCancellation(fact);
				break;
			case _ALLOWED_STATE_CANCELLATION:
				asExecutor().onAllowedStateCancellation(fact);
				break;
			case _CANCELLED_ACCEPT:
				asExecutor().onCancelledAccept(fact);
				break;
			case _CANCELLED_REQUEST:
				asExecutor().onCancelledRequest(fact);
				break;
			case _REFUSED_PROMISE_CANCELLATION:
				asExecutor().onRefusedPromiseCancellation(fact);
				break;
			case _REFUSED_STATE_CANCELLATION:
				asExecutor().onRefusedStateCancellation(fact);
				break;

			case DECLINED:
				asInitiator().onDeclined(fact);
				break;
			case PROMISED:
				asInitiator().onPromised(fact);
				break;
			case STATED:
				asInitiator().onStated(fact);
				break;
			case _ALLOWED_ACCEPT_CANCELLATION:
				asInitiator().onAllowedAcceptCancellation(fact);
				break;
			case _ALLOWED_REQUEST_CANCELLATION:
				asInitiator().onAllowedRequestCancellation(fact);
				break;
			case _CANCELLED_PROMISE:
				asInitiator().onCancelledPromise(fact);
				break;
			case _CANCELLED_STATE:
				asInitiator().onCancelledState(fact);
				break;
			case _REFUSED_ACCEPT_CANCELLATION:
				asInitiator().onRefusedAcceptCancellation(fact);
				break;
			case _REFUSED_REQUEST_CANCELLATION:
				asInitiator().onRefusedRequestCancellation(fact);
				break;

			default:
				throw CoalaExceptionFactory.VALUE_NOT_ALLOWED.createRuntime(
						"factType", fact.getID().getType());
			}
		} catch (final Throwable t)
		{
			onError(t);
		}
	}

	/**
	 * @param value
	 * @return
	 */
	protected AgentID newAgentID(final String value)
	{
		return getBinder().inject(ModelComponentIDFactory.class).createAgentID(
				value);
	}

	/**
	 * @return
	 */
	protected RandomDistribution.Factory newDist()
	{
		return getBinder().inject(RandomDistribution.Factory.class);
	}

	/**
	 * @param value
	 * @param unit
	 * @return
	 */
	protected SimTime newTime(final Number value, final TimeUnit unit)
	{
		return getBinder().inject(SimTimeFactory.class).create(value, unit);
	}

	/** @see ActorRole#getTime() */
	@Override
	public SimTime getTime()
	{
		return getSimulator().getTime();
	}

	/** @see ActorRole#replayFacts() */
	@Override
	public Observable<F> replayFacts()
	{
		return this.facts.asObservable();
	}

	private Logger LOG()
	{

		// @InjectLogger doesn't work on injected (abstract) super types
		if (LOG == null)
		{
			LOG = LogUtil.getLogger(AbstractActorRole.class, this);
			LOG.info("Logger NOT INJECTED");
		}
		return LOG;
	}

	/** @see ActorRole#onStopped(CoordinationFact) */
	protected void onStopped(final F fact)
	{
		LOG().warn("Ignoring " + fact.getID().getType() + ": " + fact);
	}

	/** @see ActorRole#onQuit(CoordinationFact) */
	protected void onQuit(final F fact)
	{
		LOG().warn("Ignoring " + fact.getID().getType() + ": " + fact);
	}

	private AbstractInitiator<F> asInitiator()
	{
		return (AbstractInitiator<F>) this;
	}

	private AbstractExecutor<F> asExecutor()
	{
		return (AbstractExecutor<F>) this;
	}

	private static final String ADD_PROCESS_MANAGER_AGENT = "addProcessManagerAgent";

	@Schedulable(ADD_PROCESS_MANAGER_AGENT)
	protected synchronized Observable<AgentStatusUpdate> bootAgent(
			final AgentID agentID, final Class<? extends Agent> agentType,
			// final BasicAgentStatus blockSimUntilState,
			final Job<?> next) throws Exception
	{
		if (next == null) // no need to sleep sim, nothing to schedule next
			return getBooter().createAgent(agentID, agentType);

		final CountDownLatch latch = new CountDownLatch(1);
		final Subject<AgentStatusUpdate, AgentStatusUpdate> status = ReplaySubject
				.create();
		status.subscribe(new Observer<AgentStatusUpdate>()
		{
			/** */
			private boolean success = false;

			@Override
			public void onNext(final AgentStatusUpdate update)
			{
				LOG().trace("Got child agent update: " + update);
				if (update.getStatus().isFailedStatus())
				{
					LOG().warn("Child agent failed: " + update.getAgentID());
					latch.countDown();
				} else if (update.getStatus().isInitializedStatus()// .equals(blockSimUntilState)
				)
				{
					LOG().info(
							"Child agent " + agentID
									+ " reached unblock status: "
									+ update.getStatus());
					success = true;
					latch.countDown(); // yield
				}
			}

			@Override
			public void onCompleted()
			{
				if (success)
					return;
				LOG().warn(
						"Child agent died but never reached blockable status"
								+ ", scheduling next job now");
				getSimulator()
						.schedule(next, Trigger.createAbsolute(getTime()));
				latch.countDown();
			}

			@Override
			public void onError(final Throwable e)
			{
				e.printStackTrace();
			}
		});
		getBooter().createAgent(agentID, agentType).subscribe(status);

		latch.await();
		getSimulator().schedule(next,
				Trigger.createAbsolute(getTime()));

		return status.asObservable();
	}

	/**
	 * @return the (super)type of {@link CoordinationFact}
	 */
	protected Class<F> getFactType()
	{
		return this.factType;
	}

	/** @see ActorRole#send(CoordinationFact) */
	protected <M extends Message<?>> M send(final M fact) throws Exception
	{
		return send(0, fact);
	}

	/** @see ActorRole#send(CoordinationFact) */
	protected <M extends Message<?>> M send(final Number delay, final M fact)
			throws Exception
	{
		return send(newTime(delay, getTime().getUnit()), fact);
	}

	/** @see ActorRole#send(CoordinationFact) */
	protected <M extends Message<?>> M send(final SimTime delay, final M fact)
			throws Exception
	{
		// LOG.trace("Sending fact: " + fact);
		getSimulator().schedule(
				ProcedureCall.create(this, this, SEND_METHOD_ID, fact),
				Trigger.createAbsolute(getTime().plus(delay)));
		return fact;
	}

	private static final String SEND_METHOD_ID = "actorRoleSend";

	/** @see ActorRole#send(CoordinationFact) */
	@Schedulable(SEND_METHOD_ID)
	private <M extends Message<?>> M doSend(final M fact) throws Exception
	{
		getMessenger().send(fact);
		return fact;
	}

	/** @return the agent's local {@link BooterService} */
	@JsonIgnore
	protected CreatingCapability getBooter()
	{
		return getBinder().inject(CreatingCapability.class);
	}

	/** @return the agent's local {@link SimulatorService} */
	@JsonIgnore
	protected SchedulingCapability<SimTime> getScheduler()
	{
		return getSimulator();
	}

	/** @return the agent's local {@link SimulatorService} */
	@JsonIgnore
	protected ReplicatingCapability getSimulator()
	{
		return getBinder().inject(ReplicatingCapability.class);
	}

	/** @return the agent's local {@link MessengerService} */
	@JsonIgnore
	protected SendingCapability getMessenger()
	{
		return getBinder().inject(SendingCapability.class);
	}

	/** @return the agent's local {@link ReceiverService} */
	@JsonIgnore
	protected ReceivingCapability getReceiver()
	{
		return getBinder().inject(ReceivingCapability.class);
	}

	/**
	 * @param key the configuration value to get
	 * @return the {@link PropertyGetter} from agent's local
	 *         {@link ConfigurerService}
	 */
	protected PropertyGetter getProperty(final String key)
	{
		return getBinder().inject(ConfiguringCapability.class).getProperty(key);
	}

	/** @return the agent's local {@link ReasonerService} */
	@JsonIgnore
	protected ReasoningCapability getReasoner()
	{
		return getBinder().inject(ReasoningCapability.class);
	}

	@JsonIgnore
	protected DestroyingCapability getFinalizer()
	{
		return getBinder().inject(DestroyingCapability.class);
	}

	/** @return the agent's local {@link RandomizerService} */
	@JsonIgnore
	protected RandomizingCapability getRandomizer()
	{
		return getSimulator();// getBinder().bind(RandomizerService.class);
	}

	// /** @return the agent's local {@link EmbodierService} */
	// @JsonIgnore
	// protected EmbodierService getWorld()
	// {
	// return getBinder().inject(EmbodierService.class);
	// }

}
