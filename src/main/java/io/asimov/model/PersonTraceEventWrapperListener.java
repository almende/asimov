package io.asimov.model;

import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;

/**
 * {@link PersonTraceEventWrapperListener} wraps a
 * {@link PersonTraceEventListener} on which it triggers respective methods
 * based on the content type of the {@link PersonTraceEventWrapper} events it
 * listens to
 * 
 * @date $Date: 2014-09-11 15:17:30 +0200 (do, 11 sep 2014) $
 * @version $Revision: 1049 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public class PersonTraceEventWrapperListener implements
		EventListenerInterface
{
	/** */
	private final PersonTraceEventListener listener;

	public PersonTraceEventWrapperListener(
			final PersonTraceEventListener listener)
	{
		this.listener = listener;
	}

	@Override
	public void notify(final EventInterface event)
	{
		if (event instanceof PersonTraceEventWrapper == false)
			throw new IllegalStateException("Unexpected event type: "
					+ event.getClass().getName());
		
		final PersonTraceEventWrapper ote = (PersonTraceEventWrapper) event;
		switch (ote.getPersonEventType())
		{
		case ACTIVITY:
			this.listener.onActivity(ote.getActivity());
			break;
		case MOVEMENT:
			this.listener.onMovement(ote.getMovement());
			break;
		case USAGE:
			this.listener.onUsage(ote.getUsage());
			break;
		default:
			throw new IllegalStateException("Unsupported event type: "
					+ ote.getPersonEventType());
		}
	}
}