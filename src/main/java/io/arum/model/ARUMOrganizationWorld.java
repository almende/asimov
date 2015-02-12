package io.arum.model;

import io.coala.capability.CapabilityID;
import io.coala.capability.embody.GroundingCapability;
import io.coala.model.ModelComponent;

/**
 * {@link ARUMOrganizationWorld}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
public interface ARUMOrganizationWorld extends GroundingCapability,
		ModelComponent<CapabilityID>
{

	/** */
	String WORLD_NAME = "world";
	
}
