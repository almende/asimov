package io.asimov.agent.process;

import io.asimov.agent.scenario.ScenarioReplication;
import io.coala.agent.AgentID;
import io.coala.capability.CapabilityFactory;
import io.coala.enterprise.fact.AbstractCoordinationFact;
import io.coala.enterprise.fact.AbstractCoordinationFactBuilder;
import io.coala.enterprise.fact.CoordinationFact;
import io.coala.enterprise.fact.CoordinationFactType;
import io.coala.enterprise.fact.FactID;
import io.coala.enterprise.role.Executor;
import io.coala.model.ModelComponentID;
import io.coala.time.SimTime;

/**
 * {@link ProcessCompletion}
 * 
 * @version $Revision: 1083 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public abstract class ProcessCompletion extends AbstractCoordinationFact
{

	/**
	 * {@link Initiator}
	 * 
	 * @version $Revision: 1083 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 *
	 */
	public interface Initiator extends
			io.coala.enterprise.role.Initiator<Result>
	{

		/**
		 * {@link Factory}
		 * 
		 * @version $Revision: 1083 $
		 * @author <a href="mailto:Rick@almende.org">Rick</a>
		 */
		interface Factory extends CapabilityFactory<Initiator>
		{
			// empty
		}

		/**
		 * @param cause
		 * @param processTypeID
		 * @return the {@link Request} that has been sent
		 * @throws Exception
		 */
		Request initiate(ScenarioReplication.Request cause,
				String processTypeID, AgentID procMgrID) throws Exception;

	}

	/**
	 * {@link ProcessCompleter}
	 * 
	 * @version $Revision: 1083 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public interface ProcessCompleter extends Executor<Request>
	{

		/**
		 * {@link Factory}
		 * 
		 * @version $Revision: 1083 $
		 * @author <a href="mailto:Rick@almende.org">Rick</a>
		 */
		interface Factory extends CapabilityFactory<ProcessCompleter>
		{
			// empty
		}

	}

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private String processTypeID;

	/**
	 * {@link ProcessCompletion} zero-arg bean constructor
	 */
	protected ProcessCompletion()
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
	private ProcessCompletion(final FactID id,
			final ModelComponentID<?> producerID, final AgentID senderID,
			final AgentID receiverID, final SimTime expiration,
			final String processTypeID)
	{
		super(id, producerID, senderID, receiverID, expiration);
		this.processTypeID = processTypeID;
	}

	/** @return the processTypeID */
	public String getProcessTypeID()
	{
		return this.processTypeID;
	}

	/**
	 * {@link Request}
	 * 
	 * @version $Revision: 1083 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public static class Request extends ProcessCompletion
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
		private Request(final FactID id, final ModelComponentID<?> producerID,
				final AgentID senderID, final AgentID receiverID,
				final SimTime expiration, final String processTypeID)
		{
			super(id, producerID, senderID, receiverID, expiration,
					processTypeID);
		}

		/**
		 * {@link Builder}
		 * 
		 * @version $Revision: 1083 $
		 * @author <a href="mailto:Rick@almende.org">Rick</a>
		 *
		 */
		public static class Builder extends
				AbstractCoordinationFactBuilder<Request, Builder>
		{

			/** */
			private String processTypeID;

			/**
			 * @param initiator the {@link ScenarioReplication.Executor}
			 * @param cause the {@link ScenarioReplication.Request}
			 * @return this {@link Builder}
			 */
			public static Builder forProducer(
					final ScenarioReplication.ScenarioReplicator initiator,
					final ScenarioReplication.Request cause)
			{
				return new Builder().withID(initiator.getTime(), cause)
						.withProducer(initiator);
			}

			/**
			 * @param initiator the {@link ScenarioReplication.Executor}
			 * @param cause the {@link Result}, a failed previous attempt
			 * @return this {@link Builder}
			 */
			public static Builder forProducer(
					final ScenarioReplication.ScenarioReplicator initiator,
					final Result cause)
			{
				return new Builder().withID(initiator.getTime(),
						cause.getCauseID().getCauseID())
						.withProducer(initiator);
			}

			/**
			 * @return the processTypeID
			 */
			public Builder withProcessTypeID(final String processTypeID)
			{
				this.processTypeID = processTypeID;
				return this;
			}

			/** @see CoordinationFact.Builder#build() */
			@Override
			public Request build()
			{
				return new Request(getFactID(), getProducerID(), getSenderID(),
						getReceiverID(), getExpiration(), this.processTypeID);
			}

		}
	}

	/**
	 * {@link Result}
	 * 
	 * @version $Revision: 1083 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public static class Result extends ProcessCompletion
	{

		/** */
		private static final long serialVersionUID = 1L;

		/** */
		private boolean success;

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
		private Result(final FactID id, final ModelComponentID<?> producerID,
				final AgentID senderID, final AgentID receiverID,
				final SimTime expiration, final String processTypeID,
				final boolean success)
		{
			super(id, producerID, senderID, receiverID, expiration,
					processTypeID);
			this.success = success;
		}

		/**
		 * @return
		 */
		public boolean getSuccess()
		{
			return this.success;
		}

		/**
		 * {@link Builder}
		 * 
		 * @version $Revision: 1083 $
		 * @author <a href="mailto:Rick@almende.org">Rick</a>
		 *
		 */
		public static class Builder extends
				AbstractCoordinationFactBuilder<Result, Builder>
		{

			/** */
			private String processTypeID;

			/** */
			private boolean success;

			/**
			 * @param executor
			 * @param cause
			 */
			public static Builder forProducer(final ProcessCompleter executor,
					final Request cause)
			{
				return new Builder()
						.withID(CoordinationFactType.STATED,
								executor.getTime(), cause)
						.withProducer(executor)
						.withReceiverID(cause.getSenderID())
						.withProcessTypeID(cause.getProcessTypeID());
			}

			/**
			 * @param executor
			 * @param cause
			 */
			public static Builder forProducer(
					final ManageProcessActionService executor,
					final Request cause)
			{
				return new Builder()
						.withID(CoordinationFactType.STATED,
								executor.getTime(), cause)
						.withProducer(executor)
						.withReceiverID(cause.getSenderID())
						.withProcessTypeID(cause.getProcessTypeID());
			}

			/**
			 * @return the processTypeID
			 */
			public Builder withProcessTypeID(final String processTypeID)
			{
				this.processTypeID = processTypeID;
				return this;
			}

			/**
			 * @return the processTypeID
			 */
			public Builder withSuccess(final boolean success)
			{
				this.success = success;
				return this;
			}

			/** @see CoordinationFact.Builder#build() */
			@Override
			public Result build()
			{
				return new Result(getFactID(), getProducerID(), getSenderID(),
						getReceiverID(), getExpiration(), this.processTypeID,
						this.success);
			}
		}

	}

}