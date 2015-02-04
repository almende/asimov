package io.asimov.model.events;

import io.asimov.model.AbstractEntity;
import io.coala.time.SimTime;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Embedded;
import javax.persistence.Entity;

import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 * {@link Event}
 * 
 * @date $Date: 2014-11-06 15:23:37 +0100 (do, 06 nov 2014) $
 * @version $Revision: 1113 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Entity
@NoSql(dataType = "events", dataFormat = DataFormatType.MAPPED)
public class Event<T extends Event<T>> extends AbstractEntity<T>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	@Embedded
	@Field(name = "type")
	private EventType type;

	/** */
	@Basic
	@Field(name = "replicationID")
	private String replicationID;

	/** the moment in (sim) time that the event takes place in sim time units */
	@Embedded
	@Field(name = "generationTime")
	// @JsonSerialize(using = SimTimeJsonSerializer.class)
	// @JsonDeserialize(using = SimTimeJsonDeserializer.class)
	private SimTime generationTime;

	/** the moment in (sim) time that the event takes place in sim time units */
	@Embedded
	@Field(name = "executionTime")
	// @JsonSerialize(using = SimTimeJsonSerializer.class)
	// @JsonDeserialize(using = SimTimeJsonDeserializer.class)
	private SimTime executionTime;

	/** the (wallclock) time */
	@Basic
	@Field(name = "statusChanges")
	private List<EventStatusChangeEntry> statusChanges;

	/** */
	@Basic
	@Field(name = "processID")
	private String processID;
	
	/** */
	@Basic
	@Field(name = "processInstanceID")
	private String processInstanceID;

	/** @return the type */
	public EventType getType()
	{
		return this.type;
	}

	/** @param type the {@link EventType} to set */
	protected void setType(final EventType type)
	{
		this.type = type;
	}

	/**
	 * @param name the (new) {@link EventType}
	 * @return this {@link Event} object
	 */
	@SuppressWarnings("unchecked")
	public T withType(final EventType type)
	{
		setType(type);
		return (T) this;
	}

	/**
	 * @return the replicationID
	 */
	public String getReplicationID()
	{
		return this.replicationID;
	}

	/**
	 * @param replicationID the replication to set
	 */
	protected void setReplicationID(final String replicationID)
	{
		this.replicationID = replicationID;
	}

	/**
	 * @param replicationID the replication to set
	 */
	@SuppressWarnings("unchecked")
	public T withReplicationID(final String replicationID)
	{
		setReplicationID(replicationID);
		return (T) this;
	}

	/**
	 * @return the sourceID
	 */
	public String getSourceID()
	{
		if (getGenerationTime() == null)
			return null;
		return getGenerationTime().getClockID().toString();
	}

	/**
	 * @return the generationTime
	 */
	public SimTime getGenerationTime()
	{
		return this.generationTime;
	}

	/**
	 * @param generationTime the generationTime to set
	 */
	protected void setGenerationTime(final SimTime generationTime)
	{
		this.generationTime = generationTime;
	}

	/**
	 * @param time the generationTime to set
	 */
	@SuppressWarnings("unchecked")
	public T withSourceTime(final SimTime time)
	{
		setGenerationTime(time);
		return (T) this;
	}

	/**
	 * @return the executionTime
	 */
	public SimTime getExecutionTime()
	{
		return this.executionTime;
	}

	/**
	 * @param simTime the executionTime to set
	 */
	protected void setExecutionTime(final SimTime simTime)
	{
		this.executionTime = simTime;
	}

	/**
	 * @param simTime the executionTime to set
	 */
	@SuppressWarnings("unchecked")
	public T withExecutionTime(final SimTime simTime)
	{
		setExecutionTime(simTime);
		return (T) this;
	}

	/**
	 * @return the statusChanges
	 */
	public List<EventStatusChangeEntry> getStatusChanges()
	{
		return this.statusChanges;
	}

	/**
	 * @param statusChanges the statusChanges to set
	 */
	protected void setStatusChanges(
			final List<EventStatusChangeEntry> statusChanges)
	{
		this.statusChanges = statusChanges;
	}

	/**
	 * @param statusChanges the statusChanges to set
	 */
	@SuppressWarnings("unchecked")
	public T withStatusChange(final EventStatusChangeEntry... statusChanges)
	{
		if (statusChanges != null && statusChanges.length != 0)
			for (EventStatusChangeEntry statusChange : statusChanges)
				getStatusChanges().add(statusChange);
		return (T) this;
	}

	/**
	 * @return the processID
	 */
	public String getProcessID()
	{
		return this.processID;
	}

	/**
	 * @param processID the processID to set
	 */
	public void setProcessID(final String processID)
	{
		this.processID = processID;
	}

	/**
	 * @param processID the processID to set
	 */
	@SuppressWarnings("unchecked")
	public T withProcessID(final String processID)
	{
		setProcessID(processID);
		return (T) this;
	}
	
	/**
	 * @return the processInstanceID
	 */
	public String getProcessInstanceID()
	{
		return this.processInstanceID;
	}

	/**
	 * @param processInstanceID the processInstanceID to set
	 */
	public void setProcessInstanceID(final String processInstanceID)
	{
		this.processInstanceID = processInstanceID;
	}

	/**
	 * @param processInstanceID the processInstanceID to set
	 */
	@SuppressWarnings("unchecked")
	public T withProcessInstanceID(final String processInstanceID)
	{
		setProcessInstanceID(processInstanceID);
		return (T) this;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = (super.getValue() == null) ? 23 : super.hashCode();
		result = prime * result
				+ ((executionTime == null) ? 0 : executionTime.hashCode());
		result = prime * result
				+ ((generationTime == null) ? 0 : generationTime.hashCode());
		result = prime * result
				+ ((processID == null) ? 0 : processID.hashCode());
		result = prime * result
				+ ((processInstanceID == null) ? 0 : processInstanceID.hashCode());
		result = prime * result
				+ ((replicationID == null) ? 0 : replicationID.hashCode());
		result = prime * result
				+ ((statusChanges == null) ? 0 : statusChanges.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Event<?> other = (Event<?>) obj;
		if (executionTime == null)
		{
			if (other.executionTime != null)
				return false;
		} else if (other.executionTime == null || !executionTime.equals(other.executionTime))
			return false;
		if (generationTime == null)
		{
			if (other.generationTime != null)
				return false;
		} else if (other.generationTime == null || !generationTime.equals(other.generationTime))
			return false;
		if (processID == null)
		{
			if (other.processID != null)
				return false;
		} else if (!processID.equals(other.processID))
			return false;
		if (processInstanceID == null)
		{
			if (other.processInstanceID != null)
				return false;
		} else if (!processInstanceID.equals(other.processInstanceID))
			return false;
		if (replicationID == null)
		{
			if (other.replicationID != null)
				return false;
		} else if (!replicationID.equals(other.replicationID))
			return false;
		if (statusChanges == null)
		{
			if (other.statusChanges != null)
				return false;
		} else if (!statusChanges.equals(other.statusChanges))
			return false;
		if (type == null)
		{
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	/**
	 * @param eventType
	 * @return
	 */
	public boolean hasType(final EventType eventType)
	{
		return eventType != null && getType() != null
				&& getType().getName().equals(eventType.getName());
	}


}
