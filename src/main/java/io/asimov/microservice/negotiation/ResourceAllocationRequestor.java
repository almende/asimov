package io.asimov.microservice.negotiation;

import io.asimov.microservice.negotiation.messages.Claimed;
import io.coala.agent.AgentID;
import io.coala.capability.BasicCapabilityStatus;
import io.coala.capability.Capability;
import io.coala.capability.CapabilityFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * {@link ResourceAllocationRequestor}
 * 
 * @date $Date: 2014-09-23 16:13:57 +0200 (di, 23 sep 2014) $
 * @version $Revision: 1074 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 * 
 */
public interface ResourceAllocationRequestor extends Capability<BasicCapabilityStatus>
{

	interface Factory extends CapabilityFactory<ResourceAllocationRequestor>
	{
		// empty
	}
	
	public interface AllocationCallback
	{
		/**  Gets the map of AIDs allocated for the required resources */
		public Map<AgentID, Serializable> getAllocatedResources();
		
		/**  Gets the AIDs of the unavailable resources. */
		public Set<AgentID> getUnavailabeResourceIDs();
		
		/** Check if allocation succeeded */
		public boolean wasSucces();
		
		/**
		 * The callback method that will be called if all resource are
		 * allocated. This method must be implemented to finalize the allocation
		 * from the callers perspective.
		 * 
		 * @param resources A map of AIDs allocated for the required resources.
		 */
		public void done(Map<AgentID, Serializable> resources);

		/**
		 * The callback method that will be called if a resource is unavailable.
		 * 
		 * @param aids The AIDs of the unavailable resources.
		 */
		public void failure(Set<AgentID> aids);

		/**
		 * The scenario agent ID
		 * @return
		 */
		public AgentID getScenarioAgentID();
		
		/**
		 * This method is called if an error occurred during the execution of
		 * the allocation protocol that was not caused by unavailability of the
		 * resource.
		 * @param error The cause of the error
		 */
		public void error(Exception error);
	}

	void resetFailedClaims();

	/**
	 * @param candidateMap the candidateMap to set
	 */
	void setCandidateMap(Map<Serializable, Set<AgentID>> candidateMap);

	void handleIncommingClaimed(Claimed claimed);

	void doAllocation(AllocationCallback callback);

	void deAllocate(final String scenarioAgentID) throws Exception;

	/**
	 * @return the queryToAssertionMap
	 */
	Map<Serializable, Serializable> getQueryToAssertionMap();

	/**
	 * @param queryToAssertionMap the queryToAssertionMap to set
	 */
	void setQueryToAssertionMap(
			Map<Serializable, Serializable> queryToAssertionMap);

}