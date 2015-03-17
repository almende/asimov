package io.asimov.model;

import io.asimov.model.events.Event;
import io.coala.log.LogUtil;

import java.rmi.RemoteException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;

import org.apache.log4j.Logger;

/**
 * {@link AbstractPersonTraceEventProducer}
 * 
 * @date $Date: 2014-11-24 12:10:07 +0100 (ma, 24 nov 2014) $
 * @version $Revision: 1122 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public abstract class AbstractPersonTraceEventProducer extends
		AbstractPersonTraceModelComponent implements
		PersonTraceEventProducer
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private static final Logger LOG = LogUtil
			.getLogger(AbstractPersonTraceEventProducer.class);

	/**
	 * {@link AbstractPersonTraceEventProducer} constructor
	 * 
	 * @param model
	 * @param name
	 */
	protected AbstractPersonTraceEventProducer(
			final PersonTraceModel model, final String name)
	{
		super(model, name);
	}

	/**
	 * @param startActivity
	 * @throws SimRuntimeException
	 * @throws RemoteException
	 */
	protected void scheduleTraceEvent(final Event<?> event)
			throws RemoteException, SimRuntimeException
	{
		scheduleTraceEvent(event, event.getExecutionTime().doubleValue());
	}

	/**
	 * @param startActivity
	 * @throws SimRuntimeException
	 * @throws RemoteException
	 */
	protected void scheduleTraceEvent(final Event<?> event,
			final Number absExecTime) throws RemoteException,
			SimRuntimeException
	{
		final double interval = simTime(extrapolatePeriod);
		if (extrapolateRepeat > 0 && absExecTime.doubleValue() > interval)
		{
			LOG.warn("Ignoring event at t=" + absExecTime
					+ " beyond exrapolate interval: " + extrapolatePeriod);
			return;
		}

		final int n = 1 + Math.max(0, extrapolateRepeat);
		for (int i = 0; i < n; i++)
		{
			final double t = i * interval + absExecTime.doubleValue();
			final PersonTraceEventWrapper wrappedEvent = new PersonTraceEventWrapper(
					this, t, event);
			getSimulator().scheduleEvent(
					new SimEvent(t, this, this, FIRE_TRACE_EVENT_METHOD_ID,
							new Object[] { wrappedEvent }));
		}
	}

	/** */
	private static final String FIRE_TRACE_EVENT_METHOD_ID = "fireTraceEvent";

	/** */
	protected void fireTraceEvent(final PersonTraceEventWrapper event)
	{
		try
		{
			fireEvent(event);
		} catch (final Throwable t)
		{
			LOG.warn("Problem while handling event: " + event);
		}
	}

	/** @see PersonTraceEventProducer#addListener(PersonTraceEventListener) */
	@Override
	public void addListener(final PersonTraceEventListener listener)
	{
		addListener(new PersonTraceEventWrapperListener(listener),
				PersonTraceEventWrapper.OCCUPANT_EVENT_TYPE);
	}
}
