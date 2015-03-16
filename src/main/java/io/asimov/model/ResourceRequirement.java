package io.asimov.model;

import io.asimov.model.sl.ASIMOVNode;
import io.asimov.model.sl.ASIMOVTerm;
import io.asimov.model.sl.SL;
import io.asimov.model.sl.SLConvertible;

import javax.persistence.Embeddable;

/**
 * 
 * {@link ResourceRequirement}
 * 
 * @date $Date: 2014-03-28 11:22:14 +0100 (Fri, 28 Mar 2014) $
 * @version $Revision: 806 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Embeddable
public class ResourceRequirement extends AbstractNamed<ResourceRequirement>
		implements SLConvertible<ResourceRequirement>,
		XMLConvertible<Object, ResourceRequirement>
{
	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private Resource resource;

	/** */
	private Number amount;
	
	/** */
	private Time duration;

	/** @return the resource */
	public Resource getResource()
	{
		return this.resource;
	}

	/** @param resource the resource to set */
	public void setResource(final Resource resource)
	{
		this.resource = resource;
	}

	/** @return the amount */
	public Number getAmount()
	{
		return this.amount;
	}

	/** @param amount the amount to set */
	protected void setAmount(final Number amount)
	{
		this.amount = amount;
	}

	public Time getDuration()
	{
		return duration;
	}

	public void setDuration(Time duration)
	{
		this.duration = duration;
	}

	/** @param amount the amount to set */
	public ResourceRequirement withResource(final Resource resource,
			final Number amount, final Time duration)
	{
		setResource(resource);
		setAmount(amount);
		setDuration(duration);
		return this;
	}

	/** */
	public static final String TASK_RESOURCE_TERM_NAME = "TASK_RESOURCE_RESERVATION";

	/** */
	public static final String TASK_RESOURCE = "TASK_RESOURCE";

	/** */
	public static final String TASK_RESOURCE_AMOUNT = "TASK_RESOURCE_AMOUNT";
	
	/** */
	public static final String TASK_RESOURCE_DURATION = "TASK_RESOURCE_DURATION";

	/** */
	public static final ASIMOVTerm TASK_RESOURCE_PATTERN = new ASIMOVTerm()
		.withName(TASK_RESOURCE_TERM_NAME)
		.instantiate(TASK_RESOURCE,null)
		.instantiate(TASK_RESOURCE_AMOUNT, null)
		.instantiate(TASK_RESOURCE_DURATION, null);

	/** @see SLConvertible#toSL() */
	@Override
	public ASIMOVTerm toSL()
	{
		return TASK_RESOURCE_PATTERN
				.instantiate(TASK_RESOURCE, getResource().toSL())
				.instantiate(TASK_RESOURCE_AMOUNT, SL.integer(getAmount().intValue()))
				.instantiate(TASK_RESOURCE_DURATION, getDuration().toSL());
	}
	
	

	/** @see java.lang.Object#hashCode() */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = (getName() == null) ? 11 : getName().hashCode();
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result
				+ ((resource == null) ? 0 : resource.hashCode());
		return result;
	}

	/** @see java.lang.Object#equals(java.lang.Object) */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceRequirement other = (ResourceRequirement) obj;
		if (amount == null)
		{
			if (other.amount != null)
				return false;
		} else if (!amount.equals(other.amount))
			return false;
		if (resource == null)
		{
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		return true;
	}

	/** @see XMLConvertible#toXML() */
	@Override
	public Object toXML()
	{
		// FIXME implement
		throw new IllegalStateException("Not Implemented!");
	}

	/** @see XMLConvertible#fromXML(Object) */
	@Override
	public ResourceRequirement fromXML(final Object xmlBean)
	{
		// FIXME implement
		throw new IllegalStateException("Not Implemented!");
	}

	/** @see SLConvertible#fromSL(Term) */
	@Override
	public <N extends ASIMOVNode<N>> ResourceRequirement fromSL(final N term)
	{
		// FIXME implement
		throw new IllegalStateException("Not Implemented!");
	}

}