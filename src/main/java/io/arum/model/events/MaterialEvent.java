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
import io.asimov.xml.TMaterialUsedEvent;
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
 * {@link MaterialEvent}
 * 
 * @date $Date: 2014-11-24 12:10:07 +0100 (ma, 24 nov 2014) $
 * @version $Revision: 1122 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Entity
@NoSql(dataType = "materialEvents")
public class MaterialEvent extends Event<MaterialEvent> implements
		PersonEvent<MaterialEvent>
{

	/** */
	private static final Logger LOG = LogUtil.getLogger(MaterialEvent.class);

	/** */
	private static final long serialVersionUID = 1L;

	/** the material that is being used */
	private String material;

	/** the assemblyLine that is entered or left */
	@ManyToOne
	private AssemblyLine assemblyLine;

	/** the person that is entering or leaving */
	@ManyToOne
	private Person person;

	private String activity;

	private String activityInstanceId;

	@Override
	@Field(name = "name")
	public String getName()
	{
		return super.getName();
	}

	/**
	 * @return the materialId
	 */
	public String getMaterial()
	{
		return this.material;
	}

	/**
	 * @param material the materialId to set
	 */
	protected void setMaterial(final String material)
	{
		this.material = material;
	}

	/**
	 * @param material the materialId to set
	 */
	public MaterialEvent withMaterial(final String material)
	{
		setMaterial(material);
		return this;
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
	public MaterialEvent withAssemblyLine(final AssemblyLine assemblyLine)
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
	public MaterialEvent withPerson(final Person person)
	{
		setPerson(person);
		return this;
	}

	public MaterialEvent copy(final double simTime, final EventType type)
	{
		return new MaterialEvent()
				.withReplicationID(getReplicationID())
				.withExecutionTime(
						new SimTime(
								// Replication.BASE_UNIT,
								getExecutionTime().getClockID(), simTime,
								getExecutionTime().getUnit(),
								getExecutionTime().calcOffset()))
				.withActivityInstanceId(getActivityInstanceId())
				.withType(type).withAssemblyLine(getAssemblyLine())
				.withMaterial(getMaterial());
	}

	
	/** @see XMLConvertible#toXML() */
	@Override
	public EventRecord toXML()
	{
		final EventRecord result = new EventRecord();
		result.setActivityType(getType().toXML());
		result.setMaterialRef(getMaterial());
		result.setPersonRef(getPerson().getName());
		for (PersonRole r: getPerson().getTypes())
			result.getPersonRoleRef().add(r.getName());
		result.setAssemblyLineRef(getAssemblyLine().getName());
		result.setProcessRef(getProcessID());
		result.setActivityRef(getActivity());
		result.setActivityInstanceRef(getActivityInstanceId());
		result.setProcessInstanceRef(getProcessInstanceID());
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
	public MaterialEvent fromXML(final EventRecord xmlBean)
	{
		// FIXME implement
		throw new IllegalStateException("Not Implemented");
	}


	// FIXME replace the XMLBeans version of fromXML() with this one
	public MaterialEvent fromXML(final EventRecord event,
			final TimeUnit timeUnit, final Date offset)
	{
		final EventType usageType = event.getActivityType() == ActivityEnumerator.PERSON_STARTS_ASSEMBLY_WITH_MATERIAL_ARTIFACT_MA_CORRESPONDS_TO_MA_IN_EVENT_RECORD ? EventType.START_USE_MATERIAL
				: EventType.STOP_USE_MATERIAL;
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
		return withType(usageType).withMaterial(event.getMaterialRef())
				.withAssemblyLine(new AssemblyLine().withName(event.getAssemblyLineRef()))
				.withExecutionTime(new SimTime(// Replication.BASE_UNIT,
						sourceID, time, timeUnit, offset))
				.withActivity(event.getActivityRef())
				.withMaterial(event.getMaterialRef())
				.withProcessID(event.getProcessRef())
				.withActivityInstanceId(event.getActivityInstanceRef())
				.withProcessInstanceID(event.getProcessInstanceRef());
	}

	/** @see eu.a4ee.model.bean.PersonEvent#setActivity(java.lang.String) */
	@Override
	public void setActivity(String activity)
	{
		this.activity = activity;

	}

	public MaterialEvent withActivity(final String activity)
	{
		setActivity(activity);
		return this;
	}

	/** @see eu.a4ee.model.bean.PersonEvent#getActivity() */
	@Override
	public String getActivity()
	{
		return this.activity;
	}
	
	/**
	 * gets the activityInstanceId for this event
	 * @return the instance of the activity being perfomred
	 */
	public String getActivityInstanceId() {
		return activityInstanceId;
	}

	/**
	 * sets the activityInstanceId
	 * @param activityInstanceId
	 */
	public void setActivityInstanceId(final String activityInstanceId) {
		this.activityInstanceId = activityInstanceId;
	}

	/**
	 * gets the ActivityEvent with the activityInstanceId set
	 * @param activityInstanceId
	 * @return
	 */
	public MaterialEvent withActivityInstanceId(final String activityInstanceId) {
		setActivityInstanceId(activityInstanceId);
		return this;
	}
}
