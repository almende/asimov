package io.asimov.model.resource;

import io.asimov.agent.process.ProcessCompletion;
import io.asimov.agent.process.ProcessCompletion.ProcessCompleter;
import io.asimov.agent.scenario.ScenarioReplication.ScenarioReplicator;
import io.asimov.model.ActivityParticipation;
import io.asimov.model.ActivityParticipation.ActivityParticipant;
import io.coala.agent.AgentID;
import io.coala.capability.CapabilityFactory;
import io.coala.enterprise.fact.AbstractCoordinationFact;
import io.coala.enterprise.fact.AbstractCoordinationFactBuilder;
import io.coala.enterprise.fact.CoordinationFact;
import io.coala.enterprise.fact.CoordinationFactType;
import io.coala.enterprise.fact.FactID;
import io.coala.enterprise.role.Initiator;
import io.coala.model.ModelComponentID;
import io.coala.time.SimTime;

import java.util.Set;

/**
 * {@link DirectoryLookup}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public interface DirectoryLookup extends CoordinationFact
{

	/**
	 * {@link LookupInitiator}
	 * 
	 * @version $Revision: 1048 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public interface LookupInitiator extends Initiator<Result>
	{

	}

	/**
	 * {@link DirectoryLookupInitiator}
	 * 
	 * @version $Revision: 1048 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public interface DirectoryLookupInitiator extends Initiator<Result>
	{

		void initiate(final ProcessCompletion.Request cause);

	}

	/**
	 * {@link ScenarioReplicator}
	 * 
	 * @version $Revision: 1048 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 *
	 */
	public interface DirectoryProvider extends
			io.coala.enterprise.role.Executor<Request>
	{

		/**
		 * {@link Factory}
		 * 
		 * @version $Revision: 1048 $
		 * @author <a href="mailto:Rick@almende.org">Rick</a>
		 */
		interface Factory extends CapabilityFactory<DirectoryProvider>
		{
			// empty
		}
	}

	/**
	 * {@link Request}
	 * 
	 * @version $Revision: 1048 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public class Request extends AbstractCoordinationFact implements
			DirectoryLookup
	{

		/** */
		private static final long serialVersionUID = 1L;

		/**
		 * {@link Request} zero-arg bean constructor
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
		protected Request(final FactID id,
				final ModelComponentID<?> producerID, final AgentID senderID,
				final AgentID receiverID, final SimTime expiration)
		{
			super(id, producerID, senderID, receiverID, expiration);
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

			/**
			 * @param initiator the {@link ProcessCompleter}
			 * @param cause the {@link ProcessCompletion.Request}
			 */
			public static Builder forProducer(
					final DirectoryLookupInitiator initiator,
					final ProcessCompletion.Request cause)
			{
				return new Builder().withID(initiator.getTime(), cause)
						.withProducer(initiator)
						.withReceiverID(cause.getSenderID());
			}

			/**
			 * @param initiator the {@link ActivityParticipant}
			 * @param cause the {@link ActivityParticipation.Request}
			 */
			public static Builder forProducer(
					final ActivityParticipant initiator,
					final AgentID directoryProviderID,
					final ActivityParticipation.Request cause)
			{
				return new Builder().withID(initiator.getTime(), cause)
						.withProducer(initiator)
						.withReceiverID(directoryProviderID);
			}

			/** @see CoordinationFact.Builder#build() */
			@Override
			public Request build()
			{
				return new Request(getFactID(), getProducerID(), getSenderID(),
						getReceiverID(), getExpiration());
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
	public class Result extends AbstractCoordinationFact implements
			DirectoryLookup
	{

		/** */
		private static final long serialVersionUID = 1L;

		private Set<AgentID> rooms;

		private Set<AgentID> occupants;

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
		protected Result(final FactID id, final ModelComponentID<?> producerID,
				final AgentID senderID, final AgentID receiverID,
				final SimTime expiration, final Set<AgentID> rooms,
				final Set<AgentID> occupants)
		{
			super(id, producerID, senderID, receiverID, expiration);
		}

		public Set<AgentID> getRooms()
		{
			return this.rooms;
		}

		public Set<AgentID> getOccupants()
		{
			return this.occupants;
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

			private Set<AgentID> rooms;

			private Set<AgentID> occupants;

			/**
			 * @param executor
			 * @param cause
			 */
			public static Builder forProducer(final DirectoryProvider executor,
					final Request cause, final Set<AgentID> rooms,
					final Set<AgentID> occupants)
			{
				return new Builder()
						.withID(CoordinationFactType.STATED,
								executor.getTime(), cause)
						.withProducer(executor)
						.withReceiverID(cause.getSenderID());
			}

			public Builder withRooms(final Set<AgentID> rooms)
			{
				this.rooms = rooms;
				return this;
			}

			public Builder withOccupants(final Set<AgentID> occupants)
			{
				this.occupants = occupants;
				return this;
			}

			/** @see CoordinationFact.Builder#build() */
			@Override
			public Result build()
			{
				return new Result(getFactID(), getProducerID(), getSenderID(),
						getReceiverID(), getExpiration(), this.rooms,
						this.occupants);
			}

		}

	}
}
