package io.asimov.agent.process;

import io.coala.agent.AgentID;
import io.coala.capability.CapabilityFactory;
import io.coala.enterprise.fact.AbstractCoordinationFact;
import io.coala.enterprise.fact.AbstractCoordinationFactBuilder;
import io.coala.enterprise.fact.CoordinationFact;
import io.coala.enterprise.fact.FactID;
import io.coala.enterprise.role.Executor;
import io.coala.enterprise.role.Initiator;
import io.coala.model.ModelComponentID;
import io.coala.time.SimTime;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * {@link ResourceAllocation}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public interface ResourceAllocation extends CoordinationFact
{

	/**
	 * {@link ResourceAllocationInitiator}
	 * 
	 * @version $Revision: 1048 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public interface ResourceAllocationInitiator extends
			Initiator<ResourceAllocation>
	{

	}

	/**
	 * {@link ResourceNegotiator}
	 * 
	 * @version $Revision: 1048 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public interface ResourceNegotiator extends Executor<Request>
	{

		/**
		 * {@link Factory}
		 * 
		 * @version $Revision: 1048 $
		 * @author <a href="mailto:Rick@almende.org">Rick</a>
		 */
		interface Factory extends CapabilityFactory<ResourceNegotiator>
		{
			// empty
		}
	}

	Map<Serializable, Set<AgentID>> getCandidateMap();

	/**
	 * {@link Request}
	 * 
	 * @version $Revision: 1048 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public class Request extends AbstractCoordinationFact implements
			ResourceAllocation
	{

		/** */
		private static final long serialVersionUID = 1L;

		private Map<Serializable, Set<AgentID>> candidateMap;

		/**
		 * {@link Request} deserializable zero-arg constructor
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
				final AgentID receiverID, final SimTime expiration,
				final Map<Serializable, Set<AgentID>> candidateMap)
		{
			super(id, producerID, senderID, receiverID, expiration);
			this.candidateMap = candidateMap;
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

			private Map<Serializable, Set<AgentID>> candidateMap;

			/**
			 * @param initiator
			 * @param cause
			 */
			public static Builder forProducer(
					final ResourceNegotiator initiator,
					final ProcessCompletion.Request cause,
					final Map<Serializable, Set<AgentID>> candidateMap)
			{
				return new Builder().withID(initiator.getTime(), cause)
						.withProducer(initiator).withCandidateMap(candidateMap);
			}

			public Builder withCandidateMap(
					final Map<Serializable, Set<AgentID>> candidateMap)
			{
				this.candidateMap = candidateMap;
				return this;
			}

			private Map<Serializable, Set<AgentID>> getCandidateMap()
			{
				return candidateMap;
			}

			/** @see CoordinationFact.Builder#build() */
			@Override
			public Request build()
			{
				return new Request(getFactID(), getProducerID(), getSenderID(),
						getReceiverID(), getExpiration(), getCandidateMap());
			}

		}

		/** @see eu.a4ee.model.process.ResourceAllocation#getCandidateMap() */
		@Override
		public Map<Serializable, Set<AgentID>> getCandidateMap()
		{
			return candidateMap;
		}
	}
}