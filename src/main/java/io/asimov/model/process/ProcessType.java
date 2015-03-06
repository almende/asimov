package io.asimov.model.process;

import io.asimov.model.AbstractEntity;
import io.asimov.model.XMLConvertible;
import io.asimov.model.sl.ASIMOVNode;
import io.asimov.model.sl.ASIMOVTerm;
import io.asimov.reasoning.sl.SLConvertible;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 * {@link ProcessType}
 * 
 * @date $Date: 2014-03-28 11:22:14 +0100 (Fri, 28 Mar 2014) $
 * @version $Revision: 806 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@NoSql(dataType = "processes")
public class ProcessType extends AbstractEntity<ProcessType> implements
		SLConvertible<ProcessType>, XMLConvertible<Object, ProcessType>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** the main process type */
	public static final ProcessType LIVE = new ProcessType().withName("live");

	/** the allowed activity types that can occur in this process type */
	// @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
	private Map<String, Task> tasks = new HashMap<String, Task>();

	/** map this {@link Process}'s {@link Transition}s by id */
	// @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
	private Map<String, Transition> transitions = new HashMap<String, Transition>();

	/** needed to determine the probability density of each {@link Transition} */
	private Set<String> traceIDs = new HashSet<String>();

	/** @return the activities */
	public Task getTask(final String taskID)
	{
		return this.tasks.get(taskID);
	}

	/** @return the activities */
	public Set<Task> getTasks()
	{
		return new HashSet<Task>(this.tasks.values());
	}

	/** @param tasks the activities to set */
	protected void setTasks(final Map<String, Task> tasks)
	{
		this.tasks = tasks;
	}

	/** @param activities the activities to set */
	public void withTasks(final Task... tasks)
	{
		if (tasks != null && tasks.length != 0)
			for (Task task : tasks)
				this.tasks.put(task.getName(), task);
	}

	/** @return the transitions */
	public Transition getTransition(final String transitionID)
	{
		return this.transitions.get(transitionID);
	}

	/** @return the transitions */
	public Set<Transition> getTransitions()
	{
		return new HashSet<Transition>(this.transitions.values());
	}

	/** @param transitions the transitions to set */
	protected void setTransitions(final Map<String, Transition> transitions)
	{
		this.transitions = transitions;
	}

	/** @param transitions the transitions to set */
	public ProcessType withTransition(final Transition... transitions)
	{
		if (transitions != null && transitions.length != 0)
			for (Transition transition : transitions)
				this.transitions.put(transition.getName(), transition);
		return this;
	}

	/** @return the traceIDs */
	public Set<String> getTraceIDs()
	{
		return this.traceIDs;
	}

	/** @param traceIDs the traceIDs to set */
	protected void setTraceIDs(final Set<String> traceIDs)
	{
		this.traceIDs = traceIDs;
	}

	/** @param traceIDs the traceIDs to set */
	public ProcessType withTraceIDs(final String... traceIDs)
	{
		if (traceIDs != null && traceIDs.length != 0)
			for (String traceID : traceIDs)
				getTraceIDs().add(traceID);
		return this;
	}

	/** @see SLConvertible#toSL() */
	@Override
	@SuppressWarnings("unchecked")
	public ASIMOVTerm toSL()
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
	public ProcessType fromXML(final Object xmlBean)
	{
		// FIXME implement
		throw new IllegalStateException("Not Implemented!");
	}

	/** @see SLConvertible#fromSL(Term) */
	@Override
	public <N extends ASIMOVNode<N>> ProcessType fromSL(N term)
	{
		// FIXME implement
		throw new IllegalStateException("Not Implemented!");
	}

}
