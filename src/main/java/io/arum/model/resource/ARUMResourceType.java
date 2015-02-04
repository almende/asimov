package io.arum.model.resource;

import io.arum.model.resource.assemblyline.AssemblyLine;
import io.arum.model.resource.person.Person;
import io.arum.model.resource.supply.Material;
import io.asimov.model.AbstractEmbodied;



/**
 * {@link ARUMResourceType}
 * 
 * @version $Revision: 977 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
public enum ARUMResourceType
{

	/** */
	PERSON(Person.class),

	/** */
	ASSEMBLY_LINE(AssemblyLine.class),

	/** */
	MATERIAL(Material.class),

	;

	private final Class<? extends AbstractEmbodied<?>> type;

	private ARUMResourceType(final Class<? extends AbstractEmbodied<?>> type)
	{
		this.type = type;
	}

	public Class<? extends AbstractEmbodied<?>> getType()
	{
		return this.type;
	}
}