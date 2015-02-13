package io.asimov.model;

import io.arum.model.events.MaterialEvent;
import io.arum.model.events.MovementEvent;
import io.arum.model.events.PersonEvent;
import io.arum.model.resource.assemblyline.AssemblyLine;
import io.arum.model.resource.person.Person;
import io.asimov.db.Datasource;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.Event;
import io.asimov.model.events.EventType;
import io.asimov.model.process.Process;
import io.asimov.model.process.Task;
import io.asimov.xml.TEventTrace;
import io.coala.agent.AgentID;
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
			final PersonTraceModel model, final ModelID replicationID)
	{
		return getInstance(model, replicationID.getValue());
	}

	/**
	 * @param model
	 * @param replicationID
	 * @return
	 */
	public synchronized static final TraceService getInstance(
			final PersonTraceModel model, final String replicationID)
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
	protected TraceService(final PersonTraceModel model, final String name)
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
	public PersonEvent<?> saveEvent(final Datasource ds,
			final String processID, final String processInstanceID,
			final String activityID, final AgentID agentID,
			final EventType eventType, final SimTime timeStamp,
			final String refID, final String buildingElementName)
	{
		LOG.info("Composing savable event");
		String agentName = agentID.getValue();
		Person theActingPerson = null;
		synchronized (ds)
		{
			theActingPerson = ds.findPersonByID(agentName);
		}

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

		final PersonEvent<?> event;
		if (eventType.equals(EventType.START_ACTIVITY)
				|| eventType.equals(EventType.STOP_ACTIVITY))
		{

			event = new ActivityEvent().withType(eventType)
					.withExecutionTime(timeStamp)
					.withReplicationID(replicationID)
					.withAssemblyLineName(buildingElementName);
			if (theActingPerson != null)
			{
				event.withPerson(theActingPerson);
			} else
			{
				event.withPerson(new Person().withName(agentName));
			}
		} else if (eventType.equals(EventType.START_USE_MATERIAL)
				|| eventType.equals(EventType.STOP_USE_MATERIAL))
		{
			AssemblyLine assemblyLine = null;
			String assemblyLineName = null;

			String ref = buildingElementName;
			LOG.info("Checking wether " + ref
					+ " is a assemblyLine in replication: " + replicationID);
			try
			{
				synchronized (ds)
				{
					AssemblyLine r = ds.findAssemblyLineByID(ref);
					if (r != null)
					{
						assemblyLineName = r.getName();
						assemblyLine = r;
						LOG.info("Material " + refID
								+ " is being used for assemblyLine " + assemblyLineName
								+ " in replication: " + replicationID);
					} else
					{
						List<String> assemblyLineNames = new ArrayList<String>();
						for (AssemblyLine aAssemblyLine : ds.findAssemblyLines())
						{
							assemblyLineNames.add(aAssemblyLine.getName());
						}
						LOG.error("Did not find assemblyLine with name " + ref + " in"
								+ assemblyLineNames);
					}
				}
			} catch (Exception e1)
			{
				LOG.error(
						"Cannot find assemblyLines for material usage in persistence layer.",
						e1);
			}

			event = new MaterialEvent().withType(eventType)
					.withExecutionTime(timeStamp)
					.withReplicationID(replicationID).withAssemblyLine(assemblyLine)
					.withMaterial(refID);
			if (theActingPerson != null)
			{
				event.withPerson(theActingPerson);
			} else
			{
				event.withPerson(new Person().withName(agentName));
			}
		} else if (eventType.equals(EventType.ARIVE_AT_ASSEMBLY)
				|| eventType.equals(EventType.LEAVE_ASSEMBLY))
		{

			String ref = buildingElementName;
			LOG.info("Checking wether " + ref
					+ " is a assemblyLine or a door in replication: " + replicationID);
			String assemblyLineName = null;
			AssemblyLine assemblyLine = null;
			try
			{
				synchronized (ds)
				{
					AssemblyLine r = ds.findAssemblyLineByID(ref);
					if (r != null)
					{
						assemblyLineName = r.getName();
						assemblyLine = r;
						LOG.info("Determined " + assemblyLineName
								+ " is a assemblyLine in replication: " + replicationID);
					}
				}
			} catch (Exception e1)
			{
				LOG.error("Cannot find assemblyLines in persistence layer.", e1);
			}


			final MovementEvent movementEvent = new MovementEvent()
					.withType(eventType).withExecutionTime(timeStamp)
					.withReplicationID(replicationID)

					.withAssemblyLine(assemblyLine);
			event = movementEvent;

			if (theActingPerson != null)
			{
				movementEvent.withPerson(theActingPerson);
			} else
			{
				movementEvent.withPerson(new Person().withName(agentName));
			}
		} else
			throw new IllegalStateException("Event not set");

		((Event<?>) event).setProcessID(processID);
		((Event<?>) event).setProcessInstanceID(processInstanceID);
		((PersonEvent<?>) event)
				.setActivity((activityName == null) ? activityID : activityName);

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

	public TEventTrace toXML(final Datasource ds)
	{
		long firstEventTime = Long.MAX_VALUE, lastEventTime = Long.MIN_VALUE;
		final List<PersonEvent<?>> events = getEvents(ds);
		TEventTrace result = new TEventTrace();
		if (events.isEmpty())
			return result;
		for (PersonEvent<?> event : events)
		{
			if (event instanceof MovementEvent
					&& ((MovementEvent) event).getAssemblyLine() == null)
				continue;
			if (event instanceof MovementEvent
					&& ((MovementEvent) event).getAssemblyLine().getName()
							.equalsIgnoreCase("world"))
				continue;
			firstEventTime = Math.min(firstEventTime, event.getExecutionTime()
					.getIsoTime().getTime());
			lastEventTime = Math.max(lastEventTime, event.getExecutionTime()
					.getIsoTime().getTime());
			result.getEventRecord().add(event.toXML());
		}
		return result;
	}

	public List<PersonEvent<?>> getEvents(final Datasource ds)
	{
		LOG.info("Getting events for " + this.replicationID);
		final List<PersonEvent<?>> events = new ArrayList<PersonEvent<?>>();
//		for (Event<?> event : ds.findEvents(EventType.ENTER, EventType.LEAVE,
//				EventType.START_ACTIVITY, EventType.STOP_ACTIVITY,
//				EventType.START_USE_EQUIPMENT, EventType.STOP_USE_EQUIPMENT))
//			events.add((PersonEvent<?>) event);
		for (ActivityEvent event : getActivityEvents(ds))
		{
			events.add(event);
		}
		for (MaterialEvent event : getMaterialEvents(ds))
		{
			events.add(event);
		}
		for (MovementEvent event : getMovementEvents(ds))
		{
			events.add(event);
		}
		


		EventComparatorByExecutionTime compare = new EventComparatorByExecutionTime();

		Collections.sort(events, compare);
		Collections.sort(events, new EventComparatorByExecutionTime());
		return events;
	}

	public List<PersonEvent<?>> getNonMovementEvents(final Datasource ds)
	{
		List<PersonEvent<?>> events = new ArrayList<PersonEvent<?>>();
		for (ActivityEvent event : getActivityEvents(ds))
		{
			events.add(event);
		}
		for (MaterialEvent event : getMaterialEvents(ds))
		{
			events.add(event);
		}

		EventComparatorByExecutionTime compare = new EventComparatorByExecutionTime();

		Collections.sort(events, compare);
		return events;
	}

	private class EventComparatorByExecutionTime implements
			Comparator<PersonEvent<?>>
	{

		@Override
		public int compare(PersonEvent<?> x, PersonEvent<?> y)
		{
			return x.getExecutionTime().compareTo(y.getExecutionTime());
		}
	}

	/**
	 * Retrieve the simulation movement events
	 * 
	 * @return {@link Iterable} of {@link MovementEvent}
	 * @throws Exception
	 */
	public Iterable<MovementEvent> getMovementEvents(final Datasource ds)
	{
		return ds.findMovementEvents();
	}
	
	/**
	 * Retrieve the simulation activity events
	 * 
	 * @return {@link Iterable} of {@link ActivityEvent}
	 * @throws Exception
	 */
	public Iterable<ActivityEvent> getActivityEvents(final Datasource ds)
	{
		return ds.findActivityEvents();
	}

	/**
	 * Retrieve the simulation material events
	 * 
	 * @return {@link Iterable} of {@link MaterialEvent}
	 * @throws Exception
	 */
	public Iterable<MaterialEvent> getMaterialEvents(final Datasource ds)
	{
		return ds.findMaterialEvents(EventType.START_USE_MATERIAL,
				EventType.STOP_USE_MATERIAL);
	}

}
