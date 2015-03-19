package io.asimov.model;

import io.asimov.agent.process.ManageProcessActionService;
import io.asimov.agent.process.ProcessCompletion;
import io.asimov.agent.process.ProcessCompletion.ProcessCompleter;
import io.asimov.microservice.negotiation.ResourceReadyNotification;
import io.asimov.model.resource.RouteLookup;
import io.coala.agent.AgentID;
import io.coala.capability.CapabilityFactory;
import io.coala.enterprise.fact.AbstractCoordinationFact;
import io.coala.enterprise.fact.AbstractCoordinationFactBuilder;
import io.coala.enterprise.fact.CoordinationFact;
import io.coala.enterprise.fact.CoordinationFactType;
import io.coala.enterprise.fact.FactID;
import io.coala.enterprise.role.Executor;
import io.coala.enterprise.role.Initiator;
import io.coala.model.ModelComponentID;
import io.coala.time.SimTime;

import java.util.List;

/**
 * {@link ActivityParticipation}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public abstract class ActivityParticipation extends AbstractCoordinationFact
{


	
	
	/**
	 * {@link ActivityParticipationInitiator}
	 * 
	 * @version $Revision: 1048 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public interface ActivityParticipationInitiator extends Initiator<Result>
	{

	}

	/**
	 * {@link ActivityParticipant}
	 * 
	 * @version $Revision: 1048 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public interface ActivityParticipant extends Executor<Request>
	{
		
		
		/**
		 * A Schedulable method ID that must be provided for the arriveAtAssembly event
		 * Denotes the method that causes the agent to arrive at an assemblyLine.
		 */
		public final static String TRANSIT_TO_RESOURCE = "ARRIVE_AT_ASSEMBLY";
		
		public static final String RETRY_REQUEST = "RETRY_REQUEST";
		
		public static final String SCHEDULE_RE_ENTRANCE = "SCHEDULE_RE_ENTRANCE";
		
		/**
		 * {@link Factory}
		 * 
		 * @version $Revision: 1048 $
		 * @author <a href="mailto:Rick@almende.org">Rick</a>
		 */
		interface Factory extends CapabilityFactory<ActivityParticipant>
		{
			// empty
		}

		/**
		 * @param resourceInfo
		 */
		void notifyResourcesReady(ActivityParticipation.Request resourceInfo);

		/**
		 * @param resourceReadyListenerImpl 
		 * @param awaitingResource sends a notification when resource is ready
		 */
//		void notifyResourceReadyStateForActivity(
//				ResourceReadyListener initiator, ResourceReadyNotification.Request awaitingResource);

		/**
		 * @param cause
		 * @return the currently occupied resource agentID
		 */
		AgentID getCurrentlyOccupiedResource(ActivityParticipation.Request cause);
		
		/**
		 * 
		 * @return the identifier of the scenario agent.
		 */
		AgentID getScenarioAgentID();
		
		//Set<AgentID> getHighestPriorityActivityLocations();

		/**
		 * @param request 
		 * @return
		 */
		boolean isReadyForActivity(ResourceReadyNotification.Request request);

		/**
		 * @param cause 
		 */
		void walkRoute(RouteLookup.Result state, ActivityParticipation.Request cause, boolean walkTomorrow);
		

	}

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private final ActivityParticipationResourceInformation resourceInfo;

	/** */
	private final AgentID scenarioReplicatorID;

	/** */
	private final List<ActivityParticipationResourceInformation> otherResourceInfo;

	/**
	 * {@link Request} constructor
	 * 
	 * @param id
	 * @param producerID
	 * @param senderID
	 * @param receiverID
	 * @param expiration
	 */
	protected ActivityParticipation(
			final FactID id,
			final ModelComponentID<?> producerID,
			final AgentID senderID,
			final AgentID receiverID,
			final SimTime expiration,
			final AgentID scenarioReplicatorID,
			final ActivityParticipationResourceInformation resourceInfo,
			final List<ActivityParticipationResourceInformation> otherResourceInfo)
	{
		super(id, producerID, senderID, receiverID, expiration);
		this.resourceInfo = resourceInfo;
		this.otherResourceInfo = otherResourceInfo;
		this.scenarioReplicatorID = scenarioReplicatorID;
	}

	/**
	 * {@link ActivityParticipation} zero-arg constructor
	 */
	protected ActivityParticipation()
	{
		super();
		this.resourceInfo = null;
		this.otherResourceInfo = null;
		this.scenarioReplicatorID = null;
	}

	/**
	 * @return
	 */
	public AgentID getScenarioReplicatorID()
	{
		return this.scenarioReplicatorID;
	}

	/**
	 * @return the resourceInfo
	 */
	public ActivityParticipationResourceInformation getResourceInfo()
	{
		return this.resourceInfo;
	}

	/**
	 * @return the otherResourceInfo
	 */
	public List<ActivityParticipationResourceInformation> getOtherResourceInfo()
	{
		return this.otherResourceInfo;
	}

	/**
	 * {@link Request}
	 * 
	 * @version $Revision: 1048 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public static class Request extends ActivityParticipation
	{

		/** */
		private static final long serialVersionUID = 1L;

		/**
		 * {@link Request} zero-arg constructor
		 */
		protected Request()
		{
			super();
		}

		/**
		 * {@link Request} constructor
		 * 
		 * @param id
		 * @param producerID
		 * @param senderID
		 * @param receiverID
		 * @param expiration
		 */
		protected Request(
				final FactID id,
				final ModelComponentID<?> producerID,
				final AgentID senderID,
				final AgentID receiverID,
				final SimTime expiration,
				final AgentID scenarioReplicatorID,
				final ActivityParticipationResourceInformation resourceInfo,
				final List<ActivityParticipationResourceInformation> otherResourceInfo)
		{
			super(id, producerID, senderID, receiverID, expiration,
					scenarioReplicatorID, resourceInfo, otherResourceInfo);
		}

		/**
		 * {@link Builder}
		 * 
		 * @version $Revision: 1048 $
		 * @author <a href="mailto:Rick@almende.org">Rick</a>
		 * 
		 */
		public static class Builder extends
				AbstractCoordinationFactBuilder<Request, Builder>
		{

			/** */
			private AgentID scenarioReplicatorID = null;

			/** */
			private ActivityParticipationResourceInformation resourceInfo = null;

			/** */
			private List<ActivityParticipationResourceInformation> otherResourceInfo = null;

			/**
			 * @param initiator the {@link ProcessCompleter}
			 * @param cause the {@link ProcessCompletion.Request}
			 */
//			public static Builder forProducer(final ProcessCompleter initiator,
//					final ProcessCompletion.Request cause)
//			{
//				return new Builder().withID(initiator.getTime(), cause)
//						.withProducer(initiator)
//						.withScenarioReplicatorID(cause.getSenderID());
//			}

			/**
			 * @param initiator the {@link ProcessCompleter}
			 * @param cause the {@link ProcessCompletion.Request}
			 */
			public static Builder forProducer(
					final ManageProcessActionService initiator,
					final ProcessCompletion.Request cause)
			{
				return new Builder().withID(initiator.getTime(), cause)
						.withProducer(initiator)
						.withScenarioReplicatorID(cause.getSenderID());
			}

			/**
			 * @return
			 */
			public Builder withScenarioReplicatorID(
					final AgentID scenarioReplicatorID)
			{
				this.scenarioReplicatorID = scenarioReplicatorID;
				return this;
			}

			/**
			 * @return the type of resource entity to manage
			 */
			public Builder withResourceInfo(
					final ActivityParticipationResourceInformation resourceInfo)
			{
				this.resourceInfo = resourceInfo;
				return this;
			}

			/**
			 * @return the type of resource entity to manage
			 */
			public Builder withOtherResourceInfo(
					final List<ActivityParticipationResourceInformation> otherResourceInfo)
			{
				this.otherResourceInfo = otherResourceInfo;
				return this;
			}

			/** @see CoordinationFact.Builder#build() */
			@Override
			public Request build()
			{
				return new Request(getFactID(), getProducerID(), getSenderID(),
						getReceiverID(), getExpiration(),
						this.scenarioReplicatorID, this.resourceInfo,
						this.otherResourceInfo);
			}

		}

	}

	/**
	 * {@link Result}
	 * 
	 * @version $Revision: 1048 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public static class Result extends ActivityParticipation
	{

		/** */
		private static final long serialVersionUID = 1L;

		/**
		 * {@link Result} zero-arg bean constructor
		 */
		protected Result()
		{
			super();
		}

		/**
		 * {@link Result} constructor
		 * 
		 * @param id
		 * @param producerID
		 * @param senderID
		 * @param receiverID
		 * @param expiration
		 */
		protected Result(
				final FactID id,
				final ModelComponentID<?> producerID,
				final AgentID senderID,
				final AgentID receiverID,
				final SimTime expiration,
				final AgentID scenarioReplicatorID,
				final ActivityParticipationResourceInformation resourceInfo,
				final List<ActivityParticipationResourceInformation> otherResourceInfo)
		{
			super(id, producerID, senderID, receiverID, expiration,
					scenarioReplicatorID, resourceInfo, otherResourceInfo);
		}

		/**
		 * {@link Builder}
		 * 
		 * @version $Revision: 1048 $
		 * @author <a href="mailto:Rick@almende.org">Rick</a>
		 * 
		 */
		public static class Builder extends
				AbstractCoordinationFactBuilder<Result, Builder>
		{

			/** */
			private AgentID scenarioReplicatorID = null;

			/** */
			private ActivityParticipationResourceInformation resourceInfo = null;

			/** */
			private List<ActivityParticipationResourceInformation> otherResourceInfo = null;

			/**
			 * @param executor
			 * @param cause
			 */
			public static Builder forProducer(
					final ActivityParticipant executor, final Request cause)
			{
				return new Builder()
						.withID(CoordinationFactType.STATED,
								executor.getTime(), cause)
						.withProducer(executor)
						.withReceiverID(cause.getSenderID())
						.withScenarioReplicatorID(
								cause.getScenarioReplicatorID())
						.withResourceInfo(cause.getResourceInfo())
						.withOtherResourceInfo(cause.getOtherResourceInfo());
			}

			/**
			 * @return
			 */
			public Builder withScenarioReplicatorID(
					final AgentID scenarioReplicatorID)
			{
				this.scenarioReplicatorID = scenarioReplicatorID;
				return this;
			}

			/**
			 * @param otherResourceInfo
			 * @return
			 */
			public Builder withOtherResourceInfo(
					List<ActivityParticipationResourceInformation> otherResourceInfo)
			{
				this.otherResourceInfo = otherResourceInfo;
				return this;
			}

			/**
			 * @param resourceInfo
			 * @return {@link Builder}
			 */
			public Builder withResourceInfo(
					ActivityParticipationResourceInformation resourceInfo)
			{
				this.resourceInfo = resourceInfo;
				return this;
			}

			/** @see CoordinationFact.Builder#build() */
			@Override
			public Result build()
			{
				return new Result(getFactID(), getProducerID(), getSenderID(),
						getReceiverID(), getExpiration(),
						this.scenarioReplicatorID, this.resourceInfo,
						this.otherResourceInfo);
			}

		}

	}
}