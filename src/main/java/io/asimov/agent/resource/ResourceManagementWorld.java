package io.asimov.agent.resource;

import io.asimov.model.ASIMOVOrganizationWorld;
import io.asimov.model.AbstractEmbodied;
import io.coala.agent.AgentID;
import io.coala.capability.CapabilityFactory;
import io.coala.capability.embody.GroundingCapability;
import io.coala.capability.embody.PerceivingCapability;

import java.util.List;

/**
 * {@link ResourceManagementWorld}
 * 
 * TODO Move to even more generic worldView in sim-common
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public interface ResourceManagementWorld<E extends AbstractEmbodied<?>> extends
		ASIMOVOrganizationWorld, GroundingCapability, PerceivingCapability
{

	/**
	 * {@link Factory}
	 * 
	 * @version $Revision: 1048 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 *
	 */
	@SuppressWarnings("rawtypes")
	interface Factory extends CapabilityFactory<ResourceManagementWorld>
	{
		// empty
	}

	E getEntity();
	
	/**
	 * @return
	 */
	String getResourceType();

	/**
	 * Gets the current location for the service owner.
	 * @return the agentID of the agent representing the current location of the service owner
	 */
	AgentID getCurrentLocation();
	
	/**
	 * Gets the current location (as coordinates) for the service owner.
	 * @return the coordinates of the current location of the service owner.
	 */
	List<Number> getCurrentCoordinates();

	
	boolean isAvailable();

	void setAvailable();
	
	void setUnavailable();
	
	/**
	 * Print debug statement for resource that have debugging enabled in config with stacktrace
	 * @param line
	 */
	void debug(final String... line);
	
	/**
	 * Print debug statement for resource that have debugging enabled in config
	 * @param includeStackTrace
	 * @param line
	 */
	void debug(final boolean includeStackTrace, final String... line);
	
	/**
	 * Sets the current location for the service owner.
	 * @param locationAgentID the agentID of the agent representing the current location of the service owner
	 */
	//void setCurrentLocation(final AgentID locationAgentID);
	
	/**
	 * Sets the current location for the service owner.
	 * @param locationAgentID the agentID of the agent representing the current location of the service owner
	 * @param coordinates of the current location of the service owner.
	 */
	//void setCurrentLocation(final AgentID locationAgentID, final List<Number> coordinates);

}
