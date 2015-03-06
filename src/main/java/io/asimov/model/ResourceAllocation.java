package io.asimov.model;

import io.asimov.model.sl.ASIMOVNode;
import io.asimov.model.sl.ASIMOVTerm;
import io.asimov.model.sl.SL;
import io.asimov.model.sl.SLConvertible;

import javax.persistence.Embeddable;

/**
 * 
 * {@link ResourceAllocation}
 * 
 * @date $Date: 2014-03-28 11:22:14 +0100 (Fri, 28 Mar 2014) $
 * @version $Revision: 806 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Embeddable
public class ResourceAllocation extends AbstractNamed<ResourceAllocation>
		implements SLConvertible<ResourceAllocation>,
		XMLConvertible<Object, ResourceAllocation>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private String resourceRequirementID;

	/** */
	private String allocatedAgentID;

	/** */
	public static final String RESOURCE_ALLOCATION_TERM = "RESOURCE_ALLOCATION";

	/** */
	public static final String ALLOCATED_AGENT_AID = "ALLOCATED_AGENT_AID";

	/** */
	public static final String RESOURCE_REQUIREMENT_ID = "RESOURCE_REQUIREMENT_ID";

	/** */
	public static final ASIMOVTerm PATTERN = new ASIMOVTerm()
		.withName(RESOURCE_ALLOCATION_TERM)
		.instantiate(ALLOCATED_AGENT_AID, null)
		.instantiate(RESOURCE_REQUIREMENT_ID, null)
		.instantiate(RESOURCE_ALLOCATION_TERM, SL.string(ResourceAllocation.class.getName()));

	/** */
	private static long counter = 0L;

	/** */
	public ResourceAllocation()
	{
		withName("alloc" + (++counter));
	}

	/** @return the resourceRequirementID */
	public String getResourceRequirementID()
	{
		return this.resourceRequirementID;
	}

	/** @param resourceRequirementID the resourceRequirementID to set */
	protected void setResourceRequirementID(final String resourceRequirementID)
	{
		this.resourceRequirementID = resourceRequirementID;
	}

	/** @param resourceRequirementID the resourceRequirementID to set */
	public ResourceAllocation withResourceRequirementID(
			final String resourceRequirementID)
	{
		setResourceRequirementID(resourceRequirementID);
		return this;
	}

	/** @return the allocatedAgentID */
	public String getAllocatedAgentID()
	{
		return this.allocatedAgentID;
	}

	/** @param allocatedAgentID the allocatedAgentID to set */
	protected void setAllocatedAgentID(final String allocatedAgentID)
	{
		this.allocatedAgentID = allocatedAgentID;
	}

	/** @param allocatedAgentID the allocatedAgentID to set */
	public ResourceAllocation withAllocatedAgentID(final String allocatedAgentID)
	{
		setAllocatedAgentID(allocatedAgentID);
		return this;
	}

	/** @see SLConvertible#toSL() */
	@Override
	@SuppressWarnings("unchecked")
	public ASIMOVTerm toSL()
	{
		return PATTERN.instantiate(ALLOCATED_AGENT_AID,
				new ASIMOVTerm().withName(getAllocatedAgentID())).instantiate(
				RESOURCE_REQUIREMENT_ID, SL.string(getResourceRequirementID()));
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
	public ResourceAllocation fromXML(final Object xmlBean)
	{
		// FIXME implement
		return this;
	}

	/** @see SLConvertible#fromSL(Term) */
	@Override
	public <N extends ASIMOVNode<N>> ResourceAllocation fromSL(final N term)
	{
		// FIXME implement
		return this;
	}

}