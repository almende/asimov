package io.asimov.model;

import io.asimov.db.Datasource;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.Event;
import io.asimov.model.events.EventType;
import io.asimov.model.process.Process;
import io.asimov.model.process.Task;
import io.asimov.xml.TEventTrace;
import io.asimov.xml.TSkeletonActivityType;
import io.asimov.xml.TEventTrace.EventRecord;
import io.coala.log.LogUtil;
import io.coala.model.ModelID;
import io.coala.time.SimTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

/**
 * {@link TraceService} basically wraps a singleton {@link Datasource}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public class TraceService extends AbstractPersonTraceEventProducer
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private static final Logger LOG = LogUtil.getLogger(TraceService.class);

	/** */
	private static Map<String, TraceService> theInstances = null;


	/**
	 * @param replicationID
	 * @return
	 */
	public synchronized static final TraceService getInstance(
			final ModelID replicationID)
	{
		return getInstance(null, replicationID.getValue());
	}

	/**
	 * @param replicationID
	 * @return
	 */
	public synchronized static final TraceService getInstance(
			final String replicationID)
	{
		return getInstance(null, replicationID);
	}

	/**
	 * @param model
	 * @param replicationID
	 * @return
	 */
	public synchronized static final TraceService getInstance(
			final ResourceTraceModel model, final ModelID replicationID)
	{
		return getInstance(model, replicationID.getValue());
	}

	/**
	 * @param model
	 * @param replicationID
	 * @return
	 */
	public synchronized static final TraceService getInstance(
			final ResourceTraceModel model, final String replicationID)
	{
		if (theInstances == null)
			theInstances = new HashMap<String, TraceService>();
		TraceService instance = theInstances.get(replicationID);
		if (instance == null)
		{
			instance = new TraceService(model, replicationID);
			try
			{
				instance.replicationID = replicationID;
			} catch (Exception e)
			{
				LOG.error("Unable to connect to persistence layer", e);
				return null;
			}
			theInstances.put(replicationID, instance);
		}
		return instance;
	}

	/** */
	private ExecutorService eventLogger = Executors.newSingleThreadExecutor();

	/** */
	private String replicationID;

	/**
	 * {@link TraceService} constructor
	 * 
	 * @param model
	 * @param name
	 */
	protected TraceService(final ResourceTraceModel model, final String name)
	{
		super(model, name);
	}

	/**
	 * @param ds
	 * @param processID
	 * @param processInstanceID
	 * @param activityID
	 * @param agentID
	 * @param eventType
	 * @param timeStamp
	 * @param refID
	 * @param buildingElementName
	 * @return
	 */
	public Event<?> saveEvent(final Datasource ds,
			final String processID, final String processInstanceID,
			final String activityID, final String activityInstanceID,final List<String> resourceRefs,
			final EventType eventType, final SimTime timeStamp
			)
	{
		LOG.info("Composing savable event");
		

		String activityName = null;

		Iterable<Process> processes;
		synchronized (ds)
		{
			processes = ds.findProcesses();
		}
		for (Process p : processes)
		{
			if (activityName != null)
				break;
			Set<Task> tasks = p.getTasks();
			for (Task t : tasks)
			{
				if (activityName != null)
					break;
				if (t.getName().equals(activityID))
					activityName = t.getDescription();
			}
		}

		final ActivityEvent event = new ActivityEvent().withType(eventType)
		.withExecutionTime(timeStamp)
		.withReplicationID(replicationID)
		.withInvolvedResources(resourceRefs);
		
		if (processID != null)
			((Event<?>) event).setProcessID(processID);
		if (processInstanceID != null)
			((Event<?>) event).setProcessInstanceID(processInstanceID);
		if (activityID != null)
			((ActivityEvent) event)
				.setActivity((activityName == null) ? activityID : activityName);
		if (activityInstanceID != null)
			((ActivityEvent) event).setActivityInstanceId(activityInstanceID);
		this.eventLogger.submit(new Callable<Void>()
		{
			@Override
			public Void call()
			{
				try
				{
					synchronized (ds)
					{
						LOG.info("Saving event: " + event);
						ds.save((Event<?>) event);
					}
					// FIXME: release event from entity management (if any)
					// LOG.trace("Persisted event: " + event);
				} catch (final Exception e)
				{
					LOG.error("Problem persisting event: " + event, e);
				}
				return null;
			}
		});
		return event;
	}

	public TEventTrace toXML(final Datasource ds, boolean includeResourceDescriptors, boolean includeActivityDescriptors)
	{
		long firstEventTime = Long.MAX_VALUE, lastEventTime = Long.MIN_VALUE;
		TEventTrace result = new TEventTrace();
		for (ActivityEvent event : getActivityEvents(ds))
		{
			boolean verboseEvent = (
					event.hasType(EventType.ACTIVITY_CREATED)
					|| event.hasType(EventType.PROCESS_CREATED)
					|| event.hasType(EventType.RESOURCE_CREATED));
			firstEventTime = Math.min(firstEventTime, event.getExecutionTime()
					.getIsoTime().getTime());
			lastEventTime = Math.max(lastEventTime, event.getExecutionTime()
					.getIsoTime().getTime());
			final EventRecord xmlEvent = event.toXML();
			if ((verboseEvent || includeResourceDescriptors) && xmlEvent.getResourceRef() != null && !xmlEvent.getResourceRef().isEmpty()) {
				int i = 0;
				for (final String resourceRef : xmlEvent.getResourceRef()) {
					if (i == 0) {
						final ASIMOVResourceDescriptor r = ds.findResourceDescriptorByID(resourceRef);
						xmlEvent.setActingResource(r.toXML());
					} else {
						final ASIMOVResourceDescriptor r = ds.findResourceDescriptorByID(resourceRef);
						xmlEvent.getInvolvedResource().add(r.toXML());
					}
					i++;
				}
			}
			if ((verboseEvent || includeActivityDescriptors) && event.getActivity() != null) {
				Process p = ds.findProcessByID(event.getProcessID());
				for (TSkeletonActivityType activityType : p.toXML().getActivity()) {
					if (activityType.getName().equals(xmlEvent.getActivityRef())) {
						xmlEvent.setActivityDescription(activityType);
						break;
					}
				}
			}
			result.getEventRecord().add(xmlEvent);
		}
		return result;
	}

	public List<Event<?>> getEvents(final Datasource ds)
	{
		LOG.info("Getting events for " + this.replicationID);
		final List<Event<?>> events = new ArrayList<Event<?>>();
		for (Event<?> event : ds.findEvents(EventType.TRANSIT_TO_RESOURCE, EventType.TRANSIT_FROM_RESOURCE,
				EventType.START_ACTIVITY, EventType.STOP_ACTIVITY))
			events.add(event);
		

		EventComparatorByExecutionTime compare = new EventComparatorByExecutionTime();

		Collections.sort(events, compare);
		Collections.sort(events, new EventComparatorByExecutionTime());
		return events;
	}

	

	private class EventComparatorByExecutionTime implements
			Comparator<Event<?>>
	{

		@Override
		public int compare(Event<?> x, Event<?> y)
		{
			return x.getExecutionTime().compareTo(y.getExecutionTime());
		}
	}

	/**
	 * Retrieve the simulation activity events
	 * 
	 * @return {@link Iterable} of {@link ActivityEvent}
	 * @throws Exception
	 */
	public Iterable<ActivityEvent> getActivityEvents(final Datasource ds)
	{
		LOG.info("Getting events for " + this.replicationID);
		final List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		for (ActivityEvent event : ds.findActivityEvents())
			events.add(event);
		

		EventComparatorByExecutionTime compare = new EventComparatorByExecutionTime();

		Collections.sort(events, compare);
		Collections.sort(events, new EventComparatorByExecutionTime());
		return events;
	}

	

}
