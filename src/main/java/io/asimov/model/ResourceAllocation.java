package io.asimov.model;

import io.coala.jsa.sl.SLConvertible;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.tools.SL;

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
	public static final Term PATTERN = SL.term(
			String.format("(%s :%s ??%s :%s ??%s)", RESOURCE_ALLOCATION_TERM,
					ALLOCATED_AGENT_AID, ALLOCATED_AGENT_AID,
					RESOURCE_REQUIREMENT_ID, RESOURCE_REQUIREMENT_ID))
			.instantiate(RESOURCE_ALLOCATION_TERM,
					SL.string(ResourceAllocation.class.getName()));

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
	public Term toSL()
	{
		return PATTERN.instantiate(ALLOCATED_AGENT_AID,
				SL.term(getAllocatedAgentID())).instantiate(
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
	public ResourceAllocation fromSL(final Node term)
	{
		// FIXME implement
		return this;
	}

}