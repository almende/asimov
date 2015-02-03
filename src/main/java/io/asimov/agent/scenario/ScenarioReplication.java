package io.asimov.agent.scenario;

import io.coala.agent.AgentID;
import io.coala.capability.CapabilityFactory;
import io.coala.enterprise.fact.AbstractCoordinationFact;
import io.coala.enterprise.fact.AbstractCoordinationFactBuilder;
import io.coala.enterprise.fact.CoordinationFact;
import io.coala.enterprise.fact.CoordinationFactType;
import io.coala.enterprise.fact.FactID;
import io.coala.model.ModelComponentID;
import io.coala.time.SimTime;

/**
 * {@link ScenarioReplication}
 * 
 * @version $Revision: 1068 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public interface ScenarioReplication extends CoordinationFact
{

	/**
	 * {@link ScenarioReplicator}
	 * 
	 * @version $Revision: 1068 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 *
	 */
	public interface ScenarioReplicator extends
	// io.coala.enterprise.role.Initiator<ScenarioReplication>,
			io.coala.enterprise.role.Executor<ScenarioReplication.Request>
	{

		/**
		 * {@link Factory}
		 * 
		 * @version $Revision: 1068 $
		 * @author <a href="mailto:Rick@almende.org">Rick</a>
		 */
		interface Factory extends CapabilityFactory<ScenarioReplicator>
		{
			// empty
		}

		/**
		 * @return
		 * @throws Exception
		 */
		Request initiate() throws Exception;

		/**
		 * @param string
		 */
		SimTime getAbsProcessRepeatTime(String string);
	}

	/**
	 * {@link Request}
	 * 
	 * @version $Revision: 1068 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 *
	 */
	public class Request extends AbstractCoordinationFact implements
			ScenarioReplication
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
		protected Request(final FactID id,
				final ModelComponentID<?> producerID, final AgentID senderID,
				final AgentID receiverID, final SimTime expiration)
		{
			super(id, producerID, senderID, receiverID, expiration);
		}

		/**
		 * {@link Builder}
		 * 
		 * @version $Revision: 1068 $
		 * @author <a href="mailto:Rick@almende.org">Rick</a>
		 *
		 */
		public static class Builder extends
				AbstractCoordinationFactBuilder<Request, Builder>
		{

			/**
			 * @param initiator
			 */
			public static Builder forProducer(final ScenarioReplicator initiator)
			{
				return new Builder()
						.withID(initiator.getID().getModelID(),
								initiator.getTime()).withProducer(initiator)
						.withReceiverID(initiator.getOwnerID());
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
	 * @version $Revision: 1068 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public class Result extends AbstractCoordinationFact implements
			ScenarioReplication
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
		protected Result(final FactID id, final ModelComponentID<?> producerID,
				final AgentID senderID, final AgentID receiverID,
				final SimTime expiration)
		{
			super(id, producerID, senderID, receiverID, expiration);
		}

		/**
		 * {@link Builder}
		 * 
		 * @version $Revision: 1068 $
		 * @author <a href="mailto:Rick@almende.org">Rick</a>
		 *
		 */
		public static class Builder extends
				AbstractCoordinationFactBuilder<Result, Builder>
		{

			/**
			 * @param executor
			 * @param cause
			 */
			public static Builder forProducer(
					final ScenarioReplicator executor, final Request cause)
			{
				return new Builder()
						.withID(CoordinationFactType.STATED,
								executor.getTime(), cause)
						.withProducer(executor)
						.withReceiverID(cause.getSenderID());
			}

			/** @see CoordinationFact.Builder#build() */
			@Override
			public Result build()
			{
				return new Result(getFactID(), getProducerID(), getSenderID(),
						getReceiverID(), getExpiration());
			}

		}

	}

}