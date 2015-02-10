package io.asimov.microservice.negotiation;

import io.arum.model.resource.DirectoryLookup.LookupInitiator;
import io.arum.model.resource.RouteLookup.RouteInitiator;
import io.asimov.model.ActivityParticipation;
import io.asimov.model.ActivityParticipation.ActivityParticipant;
import io.asimov.model.ActivityParticipationResourceInformation;
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
 * {@link ResourceReadyNotification}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public abstract class ResourceReadyNotification extends
		AbstractCoordinationFact
{

	/**
	 * {@link LookupInitiator}
	 * 
	 * @version $Revision: 1048 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public interface ResourceReadyNotificationInitiator extends
			Initiator<ResourceReadyNotification.Result>
	{

	}

	/**
	 * {@link ResourceReadyListener}
	 * 
	 * @version $Revision: 1048 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public interface ResourceReadyListener extends
			Executor<ResourceReadyNotification.Request>
	{

		/**
		 * A Schedulable method ID that must be provided for the doneWalking event
		 * Denotes the method that is called after the agent is done walking
		 */
		public final static String DONE_WALKING = "DONE_WALKING";
		
		/**
		 * {@link Factory}
		 * 
		 * @version $Revision: 1048 $
		 * @author <a href="mailto:Rick@almende.org">Rick</a>
		 */
		interface Factory extends CapabilityFactory<ResourceReadyListener>
		{
			// empty
		}

	}

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private ActivityParticipationResourceInformation participationResourceInformation;
	
	/** */
	private final List<ActivityParticipationResourceInformation> otherResourceInfo;

	/**
	 * {@link ResourceReadyNotification} zero-arg bean constructor
	 */
	protected ResourceReadyNotification()
	{
		super();
		this.otherResourceInfo = null;
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
	protected ResourceReadyNotification(
			final FactID id,
			final ModelComponentID<?> producerID,
			final AgentID senderID,
			final AgentID receiverID,
			final SimTime expiration,
			final ActivityParticipationResourceInformation participationResourceInformation,
			final List<ActivityParticipationResourceInformation> otherResourceInfo)
	{
		super(id, producerID, senderID, receiverID, expiration);
		this.participationResourceInformation = participationResourceInformation;
		this.otherResourceInfo = otherResourceInfo;
	}

	/**
	 * @return the participationResourceInformation
	 */
	public ActivityParticipationResourceInformation getResourceInfo()
	{
		return this.participationResourceInformation;
	}
	
	/**
	 * @return the participationResourceInformation
	 */
	public List<ActivityParticipationResourceInformation> getOtherResourceInfo()
	{
		return this.otherResourceInfo;
	}
	

	/**
	 * {@link Result}
	 * 
	 * @version $Revision: 1048 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public static class Request extends ResourceReadyNotification
	{

		/** */
		private static final long serialVersionUID = 1L;

		/**
		 * {@link Result} zero-arg bean constructor
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
				final ActivityParticipationResourceInformation participationResourceInformation,
				final List<ActivityParticipationResourceInformation> otherResourceInfo)
		{
			super(id, producerID, senderID, receiverID, expiration,
					participationResourceInformation,otherResourceInfo);
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
			private ActivityParticipationResourceInformation participationResourceInformation;
			
			/** */
			private List<ActivityParticipationResourceInformation> otherResourceInfo = null;

			/**
			 * @param initiator the {@link ActivityParticipant}
			 * @param cause the {@link ActivityParticipation.Request}
			 */
			public static Builder forProducer(
					final ActivityParticipant initiator,
					final ActivityParticipation cause)
			{
				return new Builder().withID(initiator.getTime(), cause)
						.withProducer(initiator)
						.withResourceInfo(cause.getResourceInfo())
						.withOtherResourceInfo(cause.getOtherResourceInfo());
			}

			public Builder withResourceInfo(
					final ActivityParticipationResourceInformation participationResourceInformation)
			{
				this.participationResourceInformation = participationResourceInformation;
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
						this.participationResourceInformation, this.otherResourceInfo);
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
	public static class Result extends ResourceReadyNotification
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
				final ActivityParticipationResourceInformation participationResourceInformation,
				final List<ActivityParticipationResourceInformation> otherResourceInfo)
		{
			super(id, producerID, senderID, receiverID, expiration,
					participationResourceInformation,otherResourceInfo);
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
			private ActivityParticipationResourceInformation participationResourceInformation;
			

			/** */
			private List<ActivityParticipationResourceInformation> otherResourceInfo = null;

			/**
			 * @param executor
			 * @param cause
			 */
			public static Builder forProducer(
					final ResourceReadyListener executor, final Request cause)
			{
				return new Builder()
						.withID(CoordinationFactType.STATED,
								executor.getTime(), cause)
						.withProducer(executor)
						.withReceiverID(cause.getSenderID())
						.withResourceInfo(cause.getResourceInfo())
						.withOtherResourceInfo(cause.getOtherResourceInfo());
			}
			
			/**
			 * @param executor
			 * @param cause
			 */
			public static Builder forProducer(
					final RouteInitiator executor, final ActivityParticipation.Request cause)
			{
				return new Builder()
						.withID(CoordinationFactType.STATED,
								executor.getTime(), cause)
						.withProducer(executor)
						.withReceiverID(cause.getSenderID())
						.withResourceInfo(cause.getResourceInfo())
						.withOtherResourceInfo(cause.getOtherResourceInfo());
			}

			public Builder withResourceInfo(
					final ActivityParticipationResourceInformation participationResourceInformation)
			{
				this.participationResourceInformation = participationResourceInformation;
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
			public Result build()
			{
				return new Result(getFactID(), getProducerID(), getSenderID(),
						getReceiverID(), getExpiration(),
						this.participationResourceInformation,this.otherResourceInfo);
			}

		}

	}
}