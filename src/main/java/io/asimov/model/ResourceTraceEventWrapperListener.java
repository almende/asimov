package io.asimov.model;

import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;

/**
 * {@link ResourceTraceEventWrapperListener} wraps a
 * {@link ResourceTraceEventListener} on which it triggers respective methods
 * based on the content type of the {@link ResourceTraceEventWrapper} events it
 * listens to
 * 
 * @date $Date: 2014-09-11 15:17:30 +0200 (do, 11 sep 2014) $
 * @version $Revision: 1049 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public class ResourceTraceEventWrapperListener implements
		EventListenerInterface
{
	/** */
	private final ResourceTraceEventListener listener;

	public ResourceTraceEventWrapperListener(
			final ResourceTraceEventListener listener)
	{
		this.listener = listener;
	}

	@Override
	public void notify(final EventInterface event)
	{
		if (event instanceof ResourceTraceEventWrapper == false)
			throw new IllegalStateException("Unexpected event type: "
					+ event.getClass().getName());
		
		final ResourceTraceEventWrapper ote = (ResourceTraceEventWrapper) event;
		switch (ote.getPersonEventType())
		{
		case ACTIVITY:
			this.listener.onActivity(ote.getActivity());
			break;
		default:
			throw new IllegalStateException("Unsupported event type: "
					+ ote.getPersonEventType());
		}
	}
}