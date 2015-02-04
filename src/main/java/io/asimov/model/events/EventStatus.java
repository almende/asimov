package io.asimov.model.events;

import io.asimov.model.XMLConvertible;

import javax.persistence.Embeddable;

/**
 * 
 * {@link EventStatus}
 * 
 * @date $Date: 2014-03-28 11:22:14 +0100 (Fri, 28 Mar 2014) $
 * @version $Revision: 806 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Embeddable
enum EventStatus implements
		XMLConvertible<Object, EventStatus>
{
	/** event was scheduled in simulator */
	planned("TIMED_ACTION_STATE_PLANNED"),

	/** event in simulator */
	started("TIMED_ACTION_STATE_STARTED"),

	/** */
	done("TIMED_ACTION_STATE_DONE"),

	/** */
	cancelled("TIMED_ACTION_STATE_CANCELLED"),

	/** */
	failed("TIMED_ACTION_STATE_FAILED"),

	;

	private String term;

	private EventStatus(final String jsaTerm)
	{
		this.term = jsaTerm;
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
	public EventStatus fromXML(final Object xmlBean)
	{
		// FIXME implement
		return this;
	}
}