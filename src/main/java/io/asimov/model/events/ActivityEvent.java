package io.asimov.model.events;

import io.asimov.db.Datasource;
import io.asimov.model.ASIMOVResourceDescriptor;
import io.asimov.model.XMLConvertible;
import io.asimov.model.process.Process;
import io.asimov.model.xml.XmlUtil;
import io.asimov.xml.TSkeletonActivityType;
import io.asimov.xml.TEventTrace.EventRecord;
import io.coala.exception.CoalaRuntimeException;
import io.coala.log.LogUtil;
import io.coala.time.ClockID;
import io.coala.time.SimTime;
import io.coala.time.TimeUnit;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
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
public class ActivityEvent extends Event<ActivityEvent> implements XMLConvertible<EventRecord, ActivityEvent>
{

	/** */
	private static final Logger LOG = LogUtil.getLogger(ActivityEvent.class);

	/** */
	private static final long serialVersionUID = 1L;

	/** the activity type that is performed */

	private String activity;
	
	/** the activity instance that is performed */

	private String activityInstanceId;

	/** the person that is performing the activity */
	private List<String> resources;

	
	@Override
	@Field(name = "name")
	public String getName()
	{
		return super.getName();
	}

	/**
	 * @return the activityId
	 */
	public String getActivity()
	{
		return this.activity;
	}

	/**
	 * @param activity the activityId to set
	 */
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
	public List<String> getInvolvedResources()
	{
		return this.resources;
	}

	/**
	 * @param person the person to set
	 */
	protected void setInvolvedResources(final List<String> resources)
	{
		this.resources = resources;
	}

	/**
	 * @param person the person to set
	 */
	public ActivityEvent withInvolvedResources(final List<String> resources)
	{
		setInvolvedResources(resources);
		return this;
	}

	
	/** @see XMLConvertible#toXML() */
	@Override
	public EventRecord toXML()
	{
		final EventRecord result = new EventRecord();
		result.setActivityType(getType().getName()); // event type
		result.setActivityInstanceRef(getActivityInstanceId());
		result.getResourceRef().addAll(getInvolvedResources());
		result.setProcessRef(getProcessID());
		result.setActivityRef(getActivity());
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
	
	public EventRecord toVerboseXML(Datasource ds) {
		final EventRecord xmlEvent = this.toXML();
		if (xmlEvent.getResourceRef() != null && !xmlEvent.getResourceRef().isEmpty()) {
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
		if (xmlEvent.getActivityRef() != null) {
			Process p = ds.findProcessByID(xmlEvent.getProcessRef());
			for (TSkeletonActivityType activityType : p.toXML().getActivity()) {
				if (activityType.getName().equals(xmlEvent.getActivityRef())) {
					xmlEvent.setActivityDescription(activityType);
					break;
				}
			}
		}
		return xmlEvent;
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
		final EventType actType = new EventType().withName(event.getActivityType());
		final ClockID sourceID = new ClockID(null, getSourceID());
		final long simTimeMS = XmlUtil.toDateTime(event.getTimeStamp())
				.getMillis() - offset.getTime();

		// TODO find resource-sub type from ref
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
				.withActivityInstanceId(event.getActivityInstanceRef())
				.withProcessID(event.getProcessRef())
				.withProcessInstanceID(event.getProcessInstanceRef())
				.withInvolvedResources(event.getResourceRef())
//				.withReplicationID(null)
				.withType(actType)
				.withExecutionTime(new SimTime(// Replication.BASE_UNIT,
						sourceID, time, timeUnit, offset))
				.withProcessInstanceID(event.getProcessInstanceRef());
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
	public ActivityEvent withActivityInstanceId(final String activityInstanceId) {
		setActivityInstanceId(activityInstanceId);
		return this;
	}
}
