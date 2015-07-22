package io.asimov.model;

import io.coala.capability.CapabilityID;
import io.coala.capability.embody.GroundingCapability;
import io.coala.model.ModelComponent;

/**
 * {@link ASIMOVOrganizationWorld}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
public interface ASIMOVOrganizationWorld extends GroundingCapability,
		ModelComponent<CapabilityID>
{

	/** */
	String WORLD_NAME = "world";
	
	
	
}
