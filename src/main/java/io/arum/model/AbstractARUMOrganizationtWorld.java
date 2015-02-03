package io.arum.model;

import io.asimov.agent.resource.AbstractEmbodied;
import io.asimov.agent.resource.AssemblyLine;
import io.asimov.agent.resource.Material;
import io.asimov.agent.resource.Person;
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
 * {@link AbstractARUMOrganizationtWorld}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
public abstract class AbstractARUMOrganizationtWorld extends BasicCapability implements
		GroundingCapability, ModelComponent<CapabilityID>
{

	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link AbstractARUMOrganizationtWorld} constructor
	 * @param binder
	 */
	@Inject
	protected AbstractARUMOrganizationtWorld(final Binder binder)
	{
		super(binder);
	}

	/**
	 * {@link ResourceType}
	 * 
	 * @version $Revision: 1048 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 *
	 */
	public enum ResourceType
	{

		/** */
		PERSON(Person.class),

		/** */
		ASSEMBLY_LINE(AssemblyLine.class),

		/** */
		MATERIAL(Material.class),

		;

		private final Class<? extends AbstractEmbodied<?>> type;

		private ResourceType(final Class<? extends AbstractEmbodied<?>> type)
		{
			this.type = type;
		}

		public Class<? extends AbstractEmbodied<?>> getType()
		{
			return this.type;
		}
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
