package io.asimov.model.process;

import io.asimov.model.AbstractNamed;
import io.asimov.model.XMLConvertible;
import io.coala.jsa.sl.SLConvertible;
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TermSequenceNode;
import jade.semantics.lang.sl.tools.SL;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * {@link Transition} describes possible {@link Activity} transitions for some
 * {@link ProcessTYpe} that should translate into sequences of {@link Next}s
 * 
 * @see adapt4eesim.A4EEAgents.FerilliFactory.Transition
 * 
 * @date $Date: 2014-03-28 11:22:14 +0100 (Fri, 28 Mar 2014) $
 * @version $Revision: 806 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class Transition extends AbstractNamed<Transition> implements
		SLConvertible<Transition>, XMLConvertible<Object, Transition>
{

	/** */
	private static final long serialVersionUID = 1L;

	// use name for transition identifier

	/** */
	private Set<Task> fromTasks = new HashSet<Task>();

	/** */
	private Set<Task> toTasks = new HashSet<Task>();

	/** */
	private Set<String> traceIDs = new HashSet<String>();

	public Transition()
	{
		withName("p"+(++counter));
	}

	/**
	 * @return the fromTasks
	 */
	public Set<Task> getFromTasks()
	{
		return this.fromTasks;
	}

	/**
	 * @param fromTasks the fromTasks to set
	 */
	protected void setFromTasks(final Set<Task> fromTasks)
	{
		this.fromTasks = fromTasks;
	}

	/**
	 * @param fromTasks the fromTasks to set
	 */
	public Transition withFromTasks(final Task... fromTasks)
	{
		if (fromTasks != null)
			for (Task fromTask : fromTasks)
				getFromTasks().add(fromTask);
		return this;
	}

	/**
	 * @return the toTasks
	 */
	public Set<Task> getToTasks()
	{
		return this.toTasks;
	}

	/**
	 * @param toTasks the toTasks to set
	 */
	protected void setToTasks(final Set<Task> toTasks)
	{
		this.toTasks = toTasks;
	}

	/**
	 * @param toTasks the toTasks to set
	 */
	public Transition withToTasks(final Task... toTasks)
	{
		if (toTasks != null)
			for (Task toTask : toTasks)
				getToTasks().add(toTask);
		return this;
	}

	/**
	 * @return the traceIDs
	 */
	public Set<String> getTraceIDs()
	{
		return this.traceIDs;
	}

	/**
	 * @param traceIDs the traceIDs to set
	 */
	protected void setTraceIDs(final Set<String> traceIDs)
	{
		this.traceIDs = traceIDs;
	}

	/**
	 * @param traceIDs the traceIDs to set
	 */
	public Transition withTraceIDs(final String... traceIDs)
	{
		if (traceIDs != null)
			for (String traceID : traceIDs)
				getTraceIDs().add(traceID);
		return this;
	}

	/** */
	protected static long counter = 0;

	/** */
	public final static String TERM_NAME = "TRANSITION";

	/** */
	public static final String INPUT_TASK_NAMES = "INPUT_TASK_NAMES";

	/** */
	public static final String OUTPUT_TASK_NAMES = "OUTPUT_TASK_NAMES";

	/** */
	public static final String TRANSITION_ID = "TRANSITION_ID";

	/** */
	public static final String CASE_IDS = "CASE_IDS";

	/** */
	public static final Term PATTERN = SL.term(String
			.format("(%s :%s ??%s :%s ??%s :%s ??%s :%s ??%s)", TERM_NAME,
					INPUT_TASK_NAMES, INPUT_TASK_NAMES, OUTPUT_TASK_NAMES,
					OUTPUT_TASK_NAMES, TRANSITION_ID, TRANSITION_ID, CASE_IDS,
					CASE_IDS));

	/** @see SLConvertible#toSL() */
	@Override
	@SuppressWarnings("unchecked")
	public Term toSL()
	{
		final ListOfTerm inputTaskTerms = new ListOfTerm();
		final ListOfTerm outputTaskTerms = new ListOfTerm();
		final ListOfTerm caseIdTerms = new ListOfTerm();

		for (Task fromTask : getFromTasks())
			inputTaskTerms.add(Task.PATTERN.match(fromTask.toSL()).term(
					Task.TASK_NAME));
		for (Task toTask : getToTasks())
			outputTaskTerms.add(Task.PATTERN.match(toTask.toSL()).term(
					Task.TASK_NAME));
		for (String traceID : getTraceIDs())
			caseIdTerms.add(SL.string(traceID));
		return ((Term) SL.instantiate(PATTERN))
				.instantiate(TRANSITION_ID, SL.string(getName()))
				.instantiate(INPUT_TASK_NAMES,
						new TermSequenceNode(inputTaskTerms))
				.instantiate(OUTPUT_TASK_NAMES,
						new TermSequenceNode(outputTaskTerms))
				.instantiate(CASE_IDS, new TermSequenceNode(caseIdTerms));
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
	public Transition fromXML(final Object xmlBean)
	{
		// FIXME implement
		throw new IllegalStateException("Not Implemented!");
	}

	/** @see SLConvertible#fromSL(Term) */
	@Override
	public Transition fromSL(final Node term)
	{
		// FIXME implement
		throw new IllegalStateException("Not Implemented!");
	}

}