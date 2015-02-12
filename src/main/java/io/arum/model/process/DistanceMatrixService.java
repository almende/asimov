package io.arum.model.process;

import io.arum.model.resource.RouteLookup;
import io.asimov.agent.process.NotYetInitializedException;
import io.coala.agent.AgentID;
import io.coala.capability.CapabilityFactory;
import io.coala.enterprise.role.Initiator;
import io.coala.time.SimDuration;

/**
 * {@link DistanceMatrixService}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public interface DistanceMatrixService extends Initiator<RouteLookup>
{
	interface Factory extends CapabilityFactory<DistanceMatrixService>
	{
		// empty
	}
	
	public void generateDistanceMatrix(final AgentID scenarioAgentID);

	public SimDuration findWalkingTimeDistanceBetween(final AgentID sourceAgentID, final AgentID targetAgentID) throws NotYetInitializedException;
	
}
