package io.asimov.model;

import io.arum.model.events.MaterialEvent;
import io.arum.model.events.MovementEvent;
import io.arum.model.events.PersonEvent;
import io.asimov.model.events.ActivityEvent;
import io.asimov.xml.TEventTrace.EventRecord;
import nl.tudelft.simulation.event.EventType;
import nl.tudelft.simulation.event.TimedEvent;

/**
 * {@link PersonTraceEventWrapper} wraps any {@link PersonEvent} bean inside
 * a DSOL {@link TimedEvent}
 * 
 * @date $Date: 2014-09-11 15:17:30 +0200 (do, 11 sep 2014) $
 * @version $Revision: 1049 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public class PersonTraceEventWrapper extends TimedEvent implements
		XMLConvertible<EventRecord, PersonTraceEventWrapper>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	public static final EventType OCCUPANT_EVENT_TYPE = new EventType(
			"person");

	/** */
	private final PersonTraceEventType type;

	/**
	 * {@link DsolMovementEvent} constructor
	 * 
	 * @param absSimTime
	 * @param oldRate
	 * @param newRate
	 */
	public PersonTraceEventWrapper(final PersonEvent<?> event)
	{
		this(null, event.getExecutionTime().getValue().doubleValue(), event);
	}

	/**
	 * {@link DsolMovementEvent} constructor
	 * 
	 * @param absSimTime
	 * @param oldRate
	 * @param newRate
	 */
	public PersonTraceEventWrapper(final Number absSimTime,
			final PersonEvent<?> event)
	{
		this(null, absSimTime, event);
	}

	/**
	 * {@link DsolMovementEvent} constructor
	 * 
	 * @param source
	 * @param absSimTime
	 * @param oldRate
	 * @param newRate
	 */
	public PersonTraceEventWrapper(final PersonTraceEventProducer source,
			final Number absSimTime, final PersonEvent<?> event)
	{
		super(OCCUPANT_EVENT_TYPE, source, event, absSimTime.doubleValue());
		if (event == null)
			this.type = PersonTraceEventType.DONE;
		else if (event instanceof ActivityEvent)
			this.type = PersonTraceEventType.ACTIVITY;
		else if (event instanceof MovementEvent)
			this.type = PersonTraceEventType.MOVEMENT;
		else if (event instanceof MaterialEvent)
			this.type = PersonTraceEventType.USAGE;
		else
			throw new IllegalStateException("Person event type unsupported: "
					+ event.getClass().getName());
	}

	public PersonTraceEventType getPersonEventType()
	{
		return this.type;
	}

	
	/**
	 * @return {@code true} if wrapped {@link PersonEvent} is a
	 *         {@link ActivityEvent}, {@code false} otherwise
	 */
	public boolean isActivityEvent()
	{
		return getPersonEventType() == PersonTraceEventType.ACTIVITY;
	}

	/**
	 * @return {@code true} if wrapped {@link PersonEvent} is a
	 *         {@link MaterialEvent}, {@code false} otherwise
	 */
	public boolean isUsageEvent()
	{
		return getPersonEventType() == PersonTraceEventType.USAGE;
	}

	/**
	 * @return {@code true} if wrapped {@link PersonEvent} is a
	 *         {@link MovementEvent}, {@code false} otherwise
	 */
	public boolean isMovementEvent()
	{
		return getPersonEventType() == PersonTraceEventType.MOVEMENT;
	}

	
	public ActivityEvent getActivity()
	{
		return isActivityEvent() ? (ActivityEvent) getContent() : null;
	}

	public MaterialEvent getUsage()
	{
		return isUsageEvent() ? (MaterialEvent) getContent() : null;
	}

	public MovementEvent getMovement()
	{
		return isMovementEvent() ? (MovementEvent) getContent() : null;
	}

	/** @see XMLConvertible#fromXML(java.lang.Object) */
	@Override
	public PersonTraceEventWrapper fromXML(final EventRecord event)
	{
		return
		// isMovementEvent() ? getMovement().fromXML(event, null, null)
		// : isActivityEvent() ? getActivity().fromXML(event, null, null)
		// : isUsageEvent() ? getUsage().fromXML(event, null, null) :
		null;
	}

	/** @see XMLConvertible#toXML() */
	@Override
	public EventRecord toXML()
	{
		return isMovementEvent() ? getMovement().toXML()
				: isActivityEvent() ? getActivity().toXML()
						: isUsageEvent() ? getUsage().toXML() : null;
	}

}