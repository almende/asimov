package io.asimov.model.process;

import io.arum.model.resource.ResourceSubtype;
import io.asimov.model.AbstractNamed;
import io.asimov.model.Resource;
import io.asimov.model.ResourceRequirement;
import io.asimov.model.Time;
import io.asimov.model.XMLConvertible;
import io.asimov.model.sl.ASIMOVNode;
import io.asimov.model.sl.ASIMOVTerm;
import io.asimov.model.sl.ASIMOVTermSequenceNode;
import io.asimov.model.sl.SL;
import io.asimov.reasoning.sl.SLConvertible;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Embeddable;


@Embeddable
public class Task extends AbstractNamed<Task> implements SLConvertible<Task>,
		XMLConvertible<Object, Task>
{
	/** */
	private static final long serialVersionUID = 1L;

	/** */
	public static final Task IDLE = new Task().withName("idle");

	/** */
	public static final Task START_OF_PROCESS = new Task()
			.withName("START_OF_PROCESS");

	/** */
	public static final Task END_OF_PROCESS = new Task()
			.withName("END_OF_PROCESS");

	private String description = null;
	
	/**
	 * precondition, identifiers of {@link Task}s that must complete before
	 * starting this {@link Task}
	 */
	private List<String> precedingTaskIDs;

	/**
	 * map this {@link Task}'s required resource type/amount tuples (
	 * {@link ResourceRequirement}) by type
	 */
	private Set<ResourceRequirement> resources = new HashSet<ResourceRequirement>();

	/** identifiers of cases in which this {@link Task} occurred */
	private Set<String> caseIDs = new HashSet<String>();

	/** @return the caseIDs */
	public Set<String> getCaseIDs()
	{
		return this.caseIDs;
	}
	
	

	public String getDescription()
	{
		return (description == null) ? getName() : description;
	}



	public void setDescription(final String description)
	{
		this.description = description;
	}

	/** @param description The description to set */
	public Task withDescription(final String description)
	{
		setDescription(description);
		return this;
	}

	/** @param caseIDs the caseIDs to set */
	protected void setCaseIDs(final Set<String> caseIDs)
	{
		this.caseIDs = caseIDs;
	}

	/** @param caseIDs the caseIDs to set */
	public Task withCaseIDs(final Collection<String> caseIDs)
	{
		for (String caseID : caseIDs)
			getCaseIDs().add(caseID);
		return this;
	}

	/** @param caseIDs the caseIDs to set */
	public Task withCaseIDs(final String... caseIDs)
	{
		for (String caseID : caseIDs)
			getCaseIDs().add(caseID);
		return this;
	}

	/** @return the resources */
	public Map<ResourceSubtype, ResourceRequirement> getResources()
	{
		Map<ResourceSubtype, ResourceRequirement>  result = new HashMap<ResourceSubtype, ResourceRequirement>();
		for (ResourceRequirement requirement : this.resources) {
			result.put(requirement.getResource().getSubTypeID(),requirement);
		}
		return result;
	}

	/** @param resources the resources to set */
	protected void setResourceRequirements(
			final Map<ResourceSubtype, ResourceRequirement> resources)
	{
		Set<ResourceRequirement> result = new HashSet<ResourceRequirement>();
		result.addAll(resources.values());
		this.resources = result;
	}

	/** @param resources the resources to set */
	public Task withResourceRequirements(final ResourceRequirement... resources)
	{
		if (resources != null && resources.length != 0) {
			Map<ResourceSubtype, ResourceRequirement> result = getResources();
			for (ResourceRequirement resource : resources)
				result.put(resource.getResource().getSubTypeID(),
						resource);
			setResourceRequirements(result);
		}
		return this;
	}

	/** @param resources the resources to set */
	public Task withResourceRequirement(final Resource resource, Time duration)
	{
		return withResourceRequirement(resource, 1, duration);
	}

	/** @param resource the resource to set as requirement for this {@link Task} */
	public Task withResourceRequirement(final Resource resource,
			final Number amount, final Time duration)
	{
		Map<ResourceSubtype, ResourceRequirement> result = getResources();
		result.put(resource.getSubTypeID(),
				new ResourceRequirement().withResource(resource, amount, duration));
		setResourceRequirements(result);
		return this;
	}

	/** @return the precedingTasks */
	public List<String> getPrecedingTaskIDs()
	{
		return this.precedingTaskIDs;
	}

	/** @param precedingTaskIDs the precedingTasks to set */
	protected void setPrecedingTaskIDs(final List<String> precedingTaskIDs)
	{
		this.precedingTaskIDs = precedingTaskIDs;
	}

	/**
	 * @param precedingTaskIDs
	 * @return
	 */
	public Task withPrecedingTaskIDs(final List<String> precedingTaskIDs)
	{
		setPrecedingTaskIDs(precedingTaskIDs);
		return this;
	}

	/** {@link Term} name for a {@link Task} */
	public static final String TERM_NAME = "TASK";

	/** {@link Term} slot for a {@link Task}'s name */
	public static final String TASK_NAME = "TASK_NAME";
	
	/** {@link Term} slot for a {@link Task}'s description */
	public static final String TASK_DESCRIPTION = "TASK_DESCRIPTION";

	/** {@link Term} slot for a {@link Task}'s {@link ResourceRequirement}s */
	public static final String TASK_RESOURCE_RESERVATION_SET = "TASK_RESOURCE_RESERVATION_SET";

	/** {@link Term} slot for a {@link Task}'s case identifiers */
	public static final String CASE_IDS = "CASE_IDS";

	/** Pattern for a {@link Task}'s {@link Term} representation */
	public static final ASIMOVTerm PATTERN = new ASIMOVTerm().withName(TERM_NAME)
			.instantiate(TASK_NAME,null)
			.instantiate(TASK_DESCRIPTION,null)
			.instantiate(TASK_RESOURCE_RESERVATION_SET,null)
			.instantiate(CASE_IDS, null);

	/** @see SLConvertible#toSL() */
	@SuppressWarnings("unchecked")
	@Override
	public ASIMOVTerm toSL()
	{
		final List<ASIMOVTerm> resourceTerms = new ArrayList<ASIMOVTerm>();
		final List<ASIMOVTerm> caseIdTerms = new ArrayList<ASIMOVTerm>();

		if (getResources() != null)
			for (ResourceRequirement resource : getResources().values())
			{
				resourceTerms.add(SL.string(resource.getResource().getName()));
			}
		if (getCaseIDs() != null)
			for (String caseID : getCaseIDs())
				caseIdTerms.add(SL.string(caseID));

		final ASIMOVTerm result = ((ASIMOVTerm) PATTERN)
				.instantiate(TASK_NAME, SL.string(getName()))
				.instantiate(TASK_DESCRIPTION, SL.string(getDescription()))
				.instantiate(TASK_RESOURCE_RESERVATION_SET,
						new ASIMOVTermSequenceNode(resourceTerms))
				.instantiate(CASE_IDS, new ASIMOVTermSequenceNode(caseIdTerms));

		return result;
	}
	
	

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((caseIDs == null) ? 0 : caseIDs.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime
				* result
				+ ((precedingTaskIDs == null) ? 0 : precedingTaskIDs.hashCode());
		result = prime * result
				+ ((resources == null) ? 0 : resources.hashCode());
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
		Task other = (Task) obj;
		if (other.getName().equals(getName()))
			return true;
		if (caseIDs == null)
		{
			if (other.caseIDs != null)
				return false;
		} else if (!caseIDs.equals(other.caseIDs))
			return false;
		if (description == null)
		{
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (precedingTaskIDs == null)
		{
			if (other.precedingTaskIDs != null)
				return false;
		} else if (!precedingTaskIDs.equals(other.precedingTaskIDs))
			return false;
		if (resources == null)
		{
			if (other.resources != null)
				return false;
		} else if (!resources.equals(other.resources))
			return false;
		return true;
	}

	@Override
	public String toString(){
		return "Task("+this.getDescription()+")";
	}

	/** @see SLConvertible#fromSL(Term) */
	@Override
	public <N extends ASIMOVNode<N>> Task fromSL(final N term)
	{
		// FIXME implement
		throw new IllegalStateException("Not Implemented!");
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
	public Task fromXML(final Object xmlBean)
	{
		// FIXME implement
		throw new IllegalStateException("Not Implemented!");
	}
}