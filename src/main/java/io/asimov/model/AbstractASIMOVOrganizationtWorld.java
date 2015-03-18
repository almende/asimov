package io.asimov.model;

import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.capability.BasicCapability;
import io.coala.capability.CapabilityID;
import io.coala.capability.embody.GroundingCapability;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.model.ModelComponent;
import io.coala.time.Instant;

import javax.inject.Inject;

/**
 * {@link AbstractASIMOVOrganizationtWorld}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
public abstract class AbstractASIMOVOrganizationtWorld extends BasicCapability implements
		GroundingCapability, ModelComponent<CapabilityID>
{

	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link AbstractASIMOVOrganizationtWorld} constructor
	 * @param binder
	 */
	@Inject
	protected AbstractASIMOVOrganizationtWorld(final Binder binder)
	{
		super(binder);
	}


	/** @see ModelComponent#getOwnerID() */
	@Override
	public AgentID getOwnerID()
	{
		return getBinder().getID();
	}

	/** @see ModelComponent#getTime() */
	@Override
	public Instant<?> getTime()
	{
		return getBinder().inject(ReplicatingCapability.class).getTime();
	}

}
