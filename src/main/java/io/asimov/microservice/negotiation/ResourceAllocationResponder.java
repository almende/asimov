package io.asimov.microservice.negotiation;

import io.asimov.microservice.negotiation.messages.AvailabilityCheck;
import io.asimov.microservice.negotiation.messages.AvailabilityReply;
import io.asimov.microservice.negotiation.messages.Claim;
import io.asimov.microservice.negotiation.messages.Claimed;
import io.asimov.microservice.negotiation.messages.Proposal;
import io.asimov.microservice.negotiation.messages.ProposalRequest;
import io.coala.capability.BasicCapabilityStatus;
import io.coala.capability.Capability;
import io.coala.capability.CapabilityFactory;
import io.coala.message.Message;

/**
 * {@link ResourceAllocationResponder}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 * 
 */
public interface ResourceAllocationResponder extends Capability<BasicCapabilityStatus>
{
	
	interface Factory extends CapabilityFactory<ResourceAllocationResponder>
	{
		// empty
	}

	void processMessage(Message<?> m);

	Claimed processClaim(Claim claim);

	AvailabilityReply processAvailabilityCheck(
			AvailabilityCheck availabilityCheck);

	void reset();

	/**
	 * @param proposalRequest
	 * @return the proposal with the score
	 */
	Proposal processProposalRequest(ProposalRequest proposalRequest);

}