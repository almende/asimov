package io.asimov.model.events;

import io.arum.model.events.PersonEvent;
import io.arum.model.resource.person.Person;
import io.arum.model.resource.person.PersonRole;
import io.asimov.model.XMLConvertible;
import io.asimov.model.xml.XmlUtil;
import io.asimov.xml.ActivityEnumerator;
import io.asimov.xml.TEventTrace.EventRecord;
import io.coala.exception.CoalaRuntimeException;
import io.coala.log.LogUtil;
import io.coala.time.ClockID;
import io.coala.time.SimTime;
import io.coala.time.TimeUnit;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.log4j.Logger;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;
import org.joda.time.DateTime;

/**
 * {@link ActivityEvent}
 * 
 * @date $Date: 2014-11-11 13:10:47 +0100 (di, 11 nov 2014) $
 * @version $Revision: 1116 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Entity
@NoSql(dataType = "activityEvents")
public class ActivityEvent extends Event<ActivityEvent> implements
		PersonEvent<ActivityEvent>
{

	/** */
	private static final Logger LOG = LogUtil.getLogger(ActivityEvent.class);

	/** */
	private static final long serialVersionUID = 1L;

	/** the assemblyLine that is entered or left */

	private String activity;

	/** the person that is entering or leaving */
	@ManyToOne
	private Person person;

	/**
	 * The name of the assemblyLine this activty was performed in.
	 */
	private String assemblyLineName;

	@Override
	@Field(name = "name")
	public String getName()
	{
		return super.getName();
	}

	/**
	 * @return the activityId
	 */
	@Override
	public String getActivity()
	{
		return this.activity;
	}

	/**
	 * @param activity the activityId to set
	 */
	@Override
	public void setActivity(final String activity)
	{
		this.activity = activity;
	}

	/**
	 * @param activity the activityId to set
	 */
	public ActivityEvent withActivity(final String activity)
	{
		setActivity(activity);
		return this;
	}

	/**
	 * @return the person
	 */
	public Person getPerson()
	{
		return this.person;
	}

	/**
	 * @param person the person to set
	 */
	protected void setPerson(final Person person)
	{
		this.person = person;
	}

	/**
	 * @param person the person to set
	 */
	public ActivityEvent withPerson(final Person person)
	{
		setPerson(person);
		return this;
	}

	
	/** @see XMLConvertible#toXML() */
	@Override
	public EventRecord toXML()
	{
		final EventRecord result = new EventRecord();
		result.setActivityType(getType().toXML()); // event type
		result.setPersonRef(getPerson().getName());
		result.setPersonRoleRef(getPerson().getType().getName());
		result.setProcessRef(getProcessID());
		result.setActivityRef(getActivity());
		result.setProcessInstanceRef(getProcessInstanceID());
		result.setAssemblyLineRef(getAssemblyLineName());
		try
		{
			result.setTimeStamp(XmlUtil.toXML(new DateTime(getExecutionTime()
					.getIsoTime())));
		} catch (final DatatypeConfigurationException e)
		{
			LOG.warn("Problem converting sim event time to XML event date", e);
		}
		return result;
	}

	
	/** @see XMLConvertible#fromML() */
	@Override
	public ActivityEvent fromXML(final EventRecord xmlBean)
	{
		// FIXME implement
		LOG.warn("Not Implemented", new Exception("Not Implemented"));
		return null;
	}

	// FIXME replace the XMLBeans version of fromXML() with this one
	public ActivityEvent fromXML(
			final io.asimov.xml.TEventTrace.EventRecord event,
			final TimeUnit timeUnit, final Date offset)
	{
		final EventType actType = event.getActivityType() == ActivityEnumerator.PERSON_STARTS_EXECUTING_SOME_BUSINESS_ACTIVITY ? EventType.START_ACTIVITY
				: EventType.STOP_ACTIVITY;
		final ClockID sourceID = new ClockID(null, getSourceID());
		final long simTimeMS = XmlUtil.toDateTime(event.getTimeStamp())
				.getMillis() - offset.getTime();

		// TODO find assemblyLine/door type from ref
		Number time;
		try
		{
			time = timeUnit.convertFrom(simTimeMS, TimeUnit.MILLIS);
		} catch (final CoalaRuntimeException e)
		{
			time = simTimeMS;
		}
		return withType(actType)
				.withActivity(event.getActivityRef())
				.withProcessID(event.getProcessRef())
				.withProcessInstanceID(event.getProcessInstanceRef())
				.withAssemblyLineName(event.getAssemblyLineRef())
//				.withReplicationID(null)
				.withType(actType)
				.withPerson(
						new Person().withName(event.getPersonRef())
								.withType(
										new PersonRole().withName(event
												.getPersonRoleRef())))
				.withExecutionTime(new SimTime(// Replication.BASE_UNIT,
						sourceID, time, timeUnit, offset))
				.withProcessInstanceID(event.getProcessInstanceRef());
	}

	/**
	 * @param buildingElementName
	 * @return
	 */
	public ActivityEvent withAssemblyLineName(final String buildingElementName)
	{
		setAssemblyLineName(buildingElementName);
		return this;
	}

	/**
	 * @return the assemblyLineName
	 */
	public String getAssemblyLineName()
	{
		return this.assemblyLineName;
	}

	/**
	 * @param assemblyLineName the assemblyLineName to set
	 */
	public void setAssemblyLineName(final String assemblyLineName)
	{
		this.assemblyLineName = assemblyLineName;
	}
}
