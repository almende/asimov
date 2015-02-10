package io.arum.model.events;

import io.arum.model.resource.assemblyline.AssemblyLine;
import io.arum.model.resource.person.Person;
import io.arum.model.resource.person.PersonRole;
import io.asimov.model.XMLConvertible;
import io.asimov.model.events.Event;
import io.asimov.model.events.EventType;
import io.asimov.model.xml.XmlUtil;
import io.asimov.xml.ActivityEnumerator;
import io.asimov.xml.TEvent;
import io.asimov.xml.TEventTrace.EventRecord;
import io.coala.exception.CoalaRuntimeException;
import io.coala.log.LogUtil;
import io.coala.time.ClockID;
import io.coala.time.SimTime;
import io.coala.time.TimeUnit;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.log4j.Logger;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;
import org.joda.time.DateTime;

/**
 * {@link MovementEvent}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Entity
@NoSql(dataType = "moveEvents")
public class MovementEvent extends Event<MovementEvent> implements
		PersonEvent<MovementEvent>
{

	/** */
	private static final Logger LOG = LogUtil.getLogger(MovementEvent.class);

	/** */
	private static final long serialVersionUID = 1L;

	/** the assemblyLine that is entered or left */
	@ManyToOne
	private AssemblyLine assemblyLine;

	/** the person that is entering or leaving */
	@ManyToOne
	private Person person;

	private String activity;

	@Override
	@Field(name = "name")
	public String getName()
	{
		return super.getName();
	}

	/**
	 * @return the assemblyLine
	 */
	public AssemblyLine getAssemblyLine()
	{
		return this.assemblyLine;
	}

	/**
	 * @param assemblyLine the assemblyLine to set
	 */
	protected void setAssemblyLine(final AssemblyLine assemblyLine)
	{
		this.assemblyLine = assemblyLine;
	}

	/**
	 * @param assemblyLine the assemblyLine to set
	 */
	public MovementEvent withAssemblyLine(final AssemblyLine assemblyLine)
	{
		setAssemblyLine(assemblyLine);
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
	public MovementEvent withPerson(final Person person)
	{
		setPerson(person);
		return this;
	}

	public MovementEvent copy(final double simTime, final EventType type)
	{
		return new MovementEvent()
				.withReplicationID(getReplicationID())
				.withExecutionTime(
						new SimTime(
								// Replication.BASE_UNIT,
								getExecutionTime().getClockID(), simTime,
								getExecutionTime().getUnit(),
								getExecutionTime().calcOffset()))
				.withType(type).withAssemblyLine(getAssemblyLine()).withPerson(getPerson());
	}

	

	/** @see XMLConvertible#toXML() */
	@Override
	public EventRecord toXML()
	{
		final EventRecord result = new EventRecord();
		result.setActivityType(getType().toXML());
		result.setAssemblyLineRef(getAssemblyLine().getName());
		result.setPersonRef(getPerson().getName());
		for (PersonRole r: getPerson().getTypes())
			result.getPersonRoleRef().add(r.getName());
		result.setProcessRef(getProcessID());
		result.setProcessInstanceRef(getProcessInstanceID());
		result.setActivityRef(getActivity());
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

	/** @see XMLConvertible#fromXML(Object) */
	@Override
	public MovementEvent fromXML(final EventRecord xmlBean)
	{
		// FIXME implement
		LOG.warn("Not Implemented", new Exception("Not Implemented"));
		return null;
	}

	public MovementEvent fromXML(
			final TEvent event,
			final TimeUnit timeUnit, final Date offset, final String spaceName, final List<String> personGroupRefs, EventType type)
	{
		final EventType moveType = type;
		final ClockID sourceID = new ClockID(null, getSourceID());
		
		final long simTimeMS = event.getPersonMoved().getTime().toGregorianCalendar().getTime().getTime() - offset.getTime();

		// TODO find assemblyLine/door type from ref
		Number time;
		try
		{
			time = timeUnit.convertFrom(simTimeMS, TimeUnit.MILLIS);
		} catch (final CoalaRuntimeException e)
		{
			time = simTimeMS;
		}
		Person p = new Person().withName(event.getPersonMoved().getPersonRef());
		for (String r : personGroupRefs)
			p.getTypes().add(new PersonRole().withName(r));
		return withType(moveType)
				.withAssemblyLine(new AssemblyLine().withName(spaceName))
				.withPerson(p)
				.withExecutionTime(new SimTime(// Replication.BASE_UNIT,
						sourceID, time, timeUnit, offset));
	}
	
	// FIXME replace the XMLBeans version of fromXML() with this one
	public MovementEvent fromXML(
			final io.asimov.xml.TEventTrace.EventRecord event,
			final TimeUnit timeUnit, final Date offset)
	{
		final EventType moveType = event.getActivityType() == ActivityEnumerator.PERSON_ARRIVES_AT_ASSEMBLY_LINE_AL_CORRESPONDS_TO_AL_IN_EVENT_RECORD ? EventType.ARIVE_AT_ASSEMBLY
				: EventType.LEAVE_ASSEMBLY;
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
		Person p = new Person().withName(event.getPersonRef());
		for (String r : event.getPersonRoleRef())
			p.getTypes().add(new PersonRole().withName(r));
		return withType(moveType)
				.withAssemblyLine(new AssemblyLine().withName(event.getAssemblyLineRef()))
				.withPerson(p)
				.withExecutionTime(new SimTime(// Replication.BASE_UNIT,
						sourceID, time, timeUnit, offset))
						.withActivity(event.getActivityRef())
						.withProcessID(event.getProcessRef())
						.withProcessInstanceID(event.getProcessInstanceRef());
	}

	/** @see io.asimov.model.bean.PersonEvent#setActivity(java.lang.String) */
	@Override
	public void setActivity(final String activity)
	{
		this.activity = activity;
	}
	
	public MovementEvent withActivity(final String activity){
		setActivity(activity);
		return this;
	}

	/** @see io.asimov.model.bean.PersonEvent#getActivity() */
	@Override
	public String getActivity()
	{
		return this.activity;
	}
}
