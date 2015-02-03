package io.asimov.agent.resource;

import io.coala.jsa.sl.SLConvertible;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.tools.SL;

import javax.persistence.Embeddable;

/**
 * 
 * {@link Resource}
 * 
 * @date $Date: 2014-07-10 15:06:56 +0200 (do, 10 jul 2014) $
 * @version $Revision: 980 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Embeddable
public class Resource extends AbstractNamed<Resource> implements
		SLConvertible<Resource>
{
	/** */
	private static final long serialVersionUID = 1L;

	// use name for resource role reference within process type

	/** */
	private Class<? extends ResourceType> typeID;

	/** */
	private ResourceSubtype subTypeID;

	/** @return the typeID */
	public Class<? extends ResourceType> getTypeID()
	{
		return this.typeID;
	}

	/** @param typeID the typeID to set */
	protected void setTypeID(final Class<? extends ResourceType> typeID)
	{
		this.typeID = typeID;
	}

	/** @param typeID the typeID to set */
	public Resource withTypeID(final Class<? extends ResourceType> typeID)
	{
		setTypeID(typeID);
		return this;
	}

	/** @return the subTypeID */
	public ResourceSubtype getSubTypeID()
	{
		return this.subTypeID;
	}

	/** @param subTypeID the subTypeID to set */
	protected void setSubTypeID(final ResourceSubtype subTypeID)
	{
		this.subTypeID = subTypeID;
	}

	/** @param subTypeID the subTypeID to set */
	public Resource withSubTypeID(final ResourceSubtype subTypeID)
	{
		setSubTypeID(subTypeID);
		return this;
	}

	/** */
	public static final String TERM_NAME = "RESOURCE";

	/**
	 * Optional field, when a name is entered the same instance of a resource
	 * for this type is used along the whole process.
	 */
	public static final String RESOURCE_NAME = "RESOURCE_NAME";

	/** */
	public static final String RESOURCE_TYPE = "RESOURCE_TYPE";

	/** */
	public static final String RESOURCE_SUB_TYPE = "RESOURCE_SUB_TYPE";

	/** 
	 *  Where the resourceName indicates if it is a unique instance of the resource over the process.
	 *  If the resourceName is equal to the resourceSubType it can be any resource for that type, otherwise it is a unique instance.
	 * */
	public static final Term RESOURCE_PATTERN = SL.term(String.format(
			"(%s :%s ??%s :%s ??%s :%s ??%s)", TERM_NAME, RESOURCE_NAME,
			RESOURCE_NAME, RESOURCE_TYPE, RESOURCE_TYPE, RESOURCE_SUB_TYPE,
			RESOURCE_SUB_TYPE));

	/** @see SLConvertible#toSL() */
	@Override
	@SuppressWarnings("unchecked")
	public Term toSL()
	{
		Term result = ((Term) SL.instantiate(RESOURCE_PATTERN))
				.instantiate(RESOURCE_TYPE, SL.string(getTypeID().getName()))
				.instantiate(RESOURCE_SUB_TYPE,
						SL.string(getSubTypeID().getName()));
		if (getName() != null) 
			return result.instantiate(RESOURCE_NAME, SL.string(getName()));
		return result.instantiate(RESOURCE_NAME, SL.string(getSubTypeID().getName()));
	}

	/** @see SLConvertible#fromSL(Term) */
	@Override
	public Resource fromSL(final Node term)
	{
		// FIXME implement
		throw new IllegalStateException("Not Implemented!");
	}
	
	@Override
	public String getName(){
		if (super.getName() == null)
			return getSubTypeID().getName();
		else
			return super.getName();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((subTypeID == null) ? 0 : subTypeID.hashCode());
		result = prime * result + ((typeID == null) ? 0 : typeID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Resource other = (Resource) obj;
		if (subTypeID == null)
		{
			if (other.subTypeID != null)
				return false;
		} else if (!subTypeID.equals(other.subTypeID))
			return false;
		if (getName() == null)
		{
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		if (typeID == null)
		{
			if (other.typeID != null)
				return false;
		} else if (!typeID.equals(other.typeID))
			return false;
		return true;
	}
	
	

}