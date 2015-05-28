package io.asimov.model.process;

import io.asimov.model.AbstractEntity;
import io.asimov.model.XMLConvertible;
import io.asimov.model.sl.ASIMOVNode;
import io.asimov.model.sl.ASIMOVTerm;
import io.asimov.reasoning.sl.SLConvertible;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.ManyToOne;

import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 * {@link Activity} is of type {@link Task} and occurs as part of some
 * {@link Process} trace
 * 
 * @date $Date: 2014-03-28 11:22:14 +0100 (Fri, 28 Mar 2014) $
 * @version $Revision: 806 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@NoSql(dataType = "activities")
public class Activity extends AbstractEntity<Activity> implements
		SLConvertible<Activity>, XMLConvertible<Object, Activity>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	public final static String TERM_NAME = "ACTIVITY";

	/** */
	public final static String TASK = "TASK";

	/** */
	public final static String ACTIVITY_NAME = Task.TASK_NAME;

	/** */
	public final static String ACTIVITY_TIME_TOKEN = "ACTIVITY_TIME_TOKEN";

	/** */
	public final static String PROCESS_AGENT_AID = "PROCESS_AGENT_AID";

	/** */
	public static final ASIMOVTerm PATTERN = new ASIMOVTerm().withName(TERM_NAME)
			.instantiate().add(TASK,Task.PATTERN)
			.add(ACTIVITY_NAME, null)
			.add(ACTIVITY_TIME_TOKEN,null)
			.add(PROCESS_AGENT_AID, null);

	/** */
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
	@Field(name = "type")
	private Task type;

	/** */
	@Basic
	@Field(name = "processID")
	private String processID;

	/** @param type the {@link Task} to set */
	protected void setType(final Task type)
	{
		this.type = type;
	}

	/**
	 * @param name the (new) {@link Task}
	 * @return this {@link Activity} object
	 */
	public Activity withType(final Task type)
	{
		setType(type);
		return this;
	}

	/** @return the type */
	public Task getType()
	{
		return this.type;
	}

	/**
	 * @return the processID
	 */
	public String getProcessID()
	{
		return this.processID;
	}

	/**
	 * @param processID the processID to set
	 */
	protected void setProcessID(final String processID)
	{
		this.processID = processID;
	}

	/**
	 * @param processID the processID to set
	 */
	public Activity withProcessID(final String processID)
	{
		setProcessID(processID);
		return this;
	}

	/** @see SLConvertible#toSL() */
	@SuppressWarnings("unchecked")
	@Override
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
	public Activity fromXML(Object xmlBean)
	{
		// FIXME implement
		throw new IllegalStateException("Not Implemented!");
	}

	/** @see SLConvertible#fromSL(Term) */
	@Override
	public <N extends ASIMOVNode<N>> Activity fromSL(N term)
	{
		// FIXME implement
		throw new IllegalStateException("Not Implemented!");
	}

}
