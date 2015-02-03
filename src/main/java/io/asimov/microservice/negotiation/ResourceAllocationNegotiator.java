package io.asimov.microservice.negotiation;

import io.asimov.microservice.negotiation.ResourceAllocationRequestor.AllocationCallback;
import io.coala.agent.AgentID;
import io.coala.capability.BasicCapabilityStatus;
import io.coala.capability.Capability;
import io.coala.capability.CapabilityFactory;
import io.coala.message.Message;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import rx.Observable;

/**
 * {@link ResourceAllocationNegotiator}
 * 
 * @date $Date: 2014-09-23 16:13:57 +0200 (di, 23 sep 2014) $
 * @version $Revision: 1074 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 * 
 */
public interface ResourceAllocationNegotiator extends Capability<BasicCapabilityStatus>
{
	interface Factory extends CapabilityFactory<ResourceAllocationNegotiator>
	{
		// empty
	}

	public interface ConversionCallback
	{
		/**
		 * Method that converts the formula to determine requirement match and
		 * availability to the formula for allocation confirmation.
		 * 
		 * @param f Formula to determine requirement match and availability.
		 * @return Converted formula for allocation confirmation.
		 */
		public Serializable convert(Serializable f);
	}

	void processMessage(Message<?> m);

	void deAllocate(final String scenarioAgentID);

	/**
	 * @param callback
	 * @param candidateMap
	 * @param conversion
	 */
	Observable<AllocationCallback> negotiate(AllocationCallback callback,
			Map<Serializable, Set<AgentID>> candidateMap,
			ConversionCallback conversion);

}