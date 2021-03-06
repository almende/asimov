package io.asimov.model.resource;

import io.asimov.agent.process.NonSkeletonActivityCapability;
import io.asimov.agent.resource.GenericResourceManagementWorld;
import io.asimov.agent.scenario.ScenarioManagementWorld;
import io.asimov.messaging.ASIMOVMessage;
import io.asimov.model.resource.RouteLookup.RouteInitiator;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.capability.BasicCapability;
import io.coala.capability.CapabilityID;
import io.coala.capability.configure.ConfiguringCapability;
import io.coala.capability.interact.ReceivingCapability;
import io.coala.capability.interact.SendingCapability;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.log.LogUtil;
import io.coala.model.ModelComponentIDFactory;
import io.coala.name.Identifiable;
import io.coala.time.SimTime;
import io.coala.time.TimeUnit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observable;
import rx.Observer;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

/**
 * {@link NonSkeletonActivityCapabilityImpl}
 * 
 * @date $Date$
 * @version $Revision$
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public class NonSkeletonActivityCapabilityImpl extends BasicCapability
		implements NonSkeletonActivityCapability
{
	
	/** */
	private static final long serialVersionUID = 1L;
	
	
	private static final Logger LOG = LogUtil
			.getLogger(NonSkeletonActivityCapabilityImpl.class);

	private static final String EXIT_SITE_AFTER_PROCESS_COMPLETION = "exitSiteAfterProcessCompletion";
	
	private Map<NonSkeletonActivityState,
	Subject<NonSkeletonActivityState,NonSkeletonActivityState>> results = 
	new HashMap<NonSkeletonActivityCapability.NonSkeletonActivityState, 
	Subject<NonSkeletonActivityState,NonSkeletonActivityState>>();
	
	private Set<String> requestedActivityIDs = new HashSet<String>();
	
	private AgentID scenarioAgent = null;
	
	private Map<NonSkeletonActivityState, AgentID> awaitingStatesFromRequestor = new HashMap<NonSkeletonActivityState,AgentID>();
	
	private Observer<ASIMOVMessage> messageObserver = new Observer<ASIMOVMessage>()
	{

		@Override
		public void onCompleted()
		{
			; // nothing special here
		}

		@Override
		public void onError(Throwable e)
		{
			LOG.error("Error executing Non-Skeleton Activity",e);
		}

		@Override
		public void onNext(ASIMOVMessage t)
		{
			if (t.content instanceof NonSkeletonActivityState) {
				NonSkeletonActivityState state = (NonSkeletonActivityState)t.content;
				if (requestedActivityIDs.contains(state.getSkeltetonActivityId())) {
					// = Response
					onLeftSite(state);
				} else {
					// = Request
					GenericResourceManagementWorld world = getBinder().inject(GenericResourceManagementWorld.class);
					if (!world.isMoveable()) {
						LOG.warn("Not a moveable resource so nothing to do, responding with echo.");
						try
						{
							getBinder().inject(SendingCapability.class).send(new ASIMOVMessage(getBinder().inject(ReplicatingCapability.class).getTime(), t.getReceiverID(), t.getSenderID(), t.content));
							return;
						} catch (Exception e)
						{
							LOG.error("Fatal error. could not respond");
							getBinder().inject(ReplicatingCapability.class).pause();
						}
					}
					LOG.info(getOwnerID()+" @ "+world.getCurrentLocation()+" with entity "+world.getEntity());
					if (world.getCurrentLocation().getValue().equals(ScenarioManagementWorld.WORLD_NAME)
							&& !t.getReceiverID().equals(t.getSenderID())
							){
						try
						{
							getBinder().inject(SendingCapability.class).send(new ASIMOVMessage(getBinder().inject(ReplicatingCapability.class).getTime().plus(1,TimeUnit.MILLIS), t.getReceiverID(), t.getSenderID(), t.content));
						} catch (Exception e)
						{
							LOG.error("Fatal error. could not respond");
							getBinder().inject(ReplicatingCapability.class).pause();
						}
					} else {
						getBinder().inject(RouteInitiator.class).initiate((io.asimov.agent.process.NonSkeletonActivityCapability)NonSkeletonActivityCapabilityImpl.this,
								world.getCurrentLocation(),
								state.getScenarioAgentID());
					}
				}
				
				
			}
		}
	};
	
	
	/**
	 * {@link NonSkeletonActivityCapabilityImpl} constructor
	 * @param binder
	 */
	@Inject
	protected NonSkeletonActivityCapabilityImpl(Binder binder)
	{
		super(binder);
	}

	@Override
	public void activate(){
		getBinder().inject(ReceivingCapability.class).getIncoming().
			ofType(ASIMOVMessage.class).subscribe(messageObserver);
	}
	
	/** @see io.asimov.model.process.NonSkeletonActivityCapability#call(java.lang.String) */
	@Override
	public Observable<NonSkeletonActivityState> call(final String type, String... arguments)
	{
		final Subject<NonSkeletonActivityState, NonSkeletonActivityState> result = 
				ReplaySubject.create();
		switch (type) {
			case NonSkeletonActivityCapability.LEAVE_SITE_WHEN_ON_IT:
				if (arguments == null || arguments.length != 2)
					throw new IllegalStateException("Expected the name of the scenario agent and the resource agent as an argument.");
				scenarioAgent = getBinder().inject(ModelComponentIDFactory.class).createAgentID(arguments[0]);
				leaveSite(result, 
						new NonSkeletonActivityState(getBinder().inject(ModelComponentIDFactory.class).createAgentID(arguments[1]), 
								NonSkeletonActivityCapabilityStatus.NonSkeletonActivity_pending,
								NonSkeletonActivityCapability.LEAVE_SITE_WHEN_ON_IT,scenarioAgent
								)
						);
				break;
			default: {
				LOG.warn("Could not execute NonSkeletonActivity because type "+type+" is unknown");	
				result.onCompleted();
			}
		}
		
		return result.asObservable();
	}

	/**
	 * @param result
	 */
	private void leaveSite(
			Subject<NonSkeletonActivityState, NonSkeletonActivityState> result, NonSkeletonActivityState state)
	{
		
		if (!getBinder().inject(ConfiguringCapability.class).getProperty(EXIT_SITE_AFTER_PROCESS_COMPLETION).getBoolean(false).booleanValue()){
			result.onCompleted();
			try {
				getBinder().inject(SendingCapability.class).send(
						new ASIMOVMessage(getTime(), getOwnerID(), scenarioAgent, "AvailableResourcesChange:"
								+ getTime().hashCode()));
			} catch (Exception e) {
				LOG.error("Failed to send available resource change update",e);
			}
			return;
		}
		result.onNext(state);
			NonSkeletonActivityState walkingState = state.transitToStatus(NonSkeletonActivityCapabilityStatus.NonSkeletonActivity_traveling);
			result.onNext(walkingState);
			results.put(walkingState, result);
			try
			{
				requestedActivityIDs.add(walkingState.getSkeltetonActivityId());
				getBinder().inject(SendingCapability.class).send(new ASIMOVMessage(getTime(), getOwnerID(), walkingState.getExecutor(), walkingState));
				LOG.warn("Requested leaveSite action.");
			} catch (Exception e)
			{
				LOG.error("Failed to request leaveSite action.",e);
			}
		result.onCompleted();	
		
	}
	
	@Override
	public synchronized void onLeftSite() {
		for (NonSkeletonActivityState state : awaitingStatesFromRequestor.keySet()) {
			onLeftSite(state);
		}
	}
	
	@Override
	public void onLeftSite(final NonSkeletonActivityState state){
		if (awaitingStatesFromRequestor.containsKey(state)) {
			try
			{
				AgentID sender = awaitingStatesFromRequestor.remove(state);
				getBinder().inject(SendingCapability.class).send(new ASIMOVMessage(getTime(), state.getExecutor(), sender, state));
			} catch (Exception e)
			{
				LOG.error("Fatal error. could not respond");
				getBinder().inject(ReplicatingCapability.class).pause();
			}
			
		} else {
			if (state.getStatus().equals(NonSkeletonActivityCapabilityStatus.NonSkeletonActivity_traveling)) {
				results.get(state).onNext(state.transitToStatus(NonSkeletonActivityCapabilityStatus.NonSkeletonActivity_executing));
				results.get(state).onNext(state.transitToStatus(NonSkeletonActivityCapabilityStatus.NonSkeletonActivity_done));
				results.get(state).onCompleted();
				results.remove(state);
				try {
					getBinder().inject(SendingCapability.class).send(
							new ASIMOVMessage(getTime(), getOwnerID(), scenarioAgent, "AvailableResourcesChange:"
									+ getTime().hashCode()));
				} catch (Exception e) {
					LOG.error("Failed to send available resource change update",e);
				}
			} else {
				throw new IllegalStateException("Invalid state! expected travelling state...");
			}
		}
	}

	/** @see io.coala.model.ModelComponent#getOwnerID() */
	@Override
	public AgentID getOwnerID()
	{
		return getBinder().getID();
	}

	/** @see io.coala.model.ModelComponent#getTime() */
	@Override
	public SimTime getTime()
	{
		return getBinder().inject(ReplicatingCapability.class).getTime();
	}

	/** @see java.lang.Comparable#compareTo(java.lang.Object) */
	@Override
	public int compareTo(Identifiable<CapabilityID> o)
	{
		return this.getID().compareTo(o.getID());
	}

	/** @see io.coala.name.Identifiable#getID() */
	@Override
	public CapabilityID getID()
	{
		return super.getID();
	}

}
