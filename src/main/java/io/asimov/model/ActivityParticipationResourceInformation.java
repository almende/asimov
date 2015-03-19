package io.asimov.model;

import io.coala.agent.AgentID;
import io.coala.json.JsonUtil;
import io.coala.time.SimDuration;
import io.coala.time.SimTime;

import java.io.Serializable;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@link ActivityParticipationResourceInformation}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public class ActivityParticipationResourceInformation implements Serializable
{

	/** */
	private static final long serialVersionUID = -8640586607819609614L;
	
	public ActivityParticipationResourceInformation() {
		// Zero argument bean constructor.
	}
	
	public String toString()
	{
		return JsonUtil.toJSONString(this);
	}
	
	/** FIXME really allocate the real thing when allocated this is just a quick fix for KPI generation*/
	@JsonIgnore
	public static HashMap<String,String> equipmentInstanceForRoom = new HashMap<String, String>();
	
	/** */
	private AgentID resourceAgent;
	
	/** */
	private String resourceInstanceName;
	
	/** */
	private String resourceName;
	
	/** */
	private SimDuration resourceUsageDuration = null;
	
	/** */
	private SimTime scheduledExecutionTime = null;

	/** */
	private String activityName = null;
	
	/** */
	private String activityInstanceId = null;
	
	private String processID = null;
	
	private String processInstanceId = null;

	/** */
	private String resourceType = null;
	
	private boolean moveable;

	/**
	 * @return the resourceAgent
	 */
	public AgentID getResourceAgent()
	{
		return this.resourceAgent;
	}

	/**
	 * @param resourceAgent the resourceAgent to set
	 * @return {@link ActivityParticipationResourceInformation}
	 */
	public ActivityParticipationResourceInformation withResourceAgent(AgentID resourceAgent) {
		setResourceAgent(resourceAgent);
		return this;
	}
	
	/**
	 * @param resourceName the resourceName to set
	 * @return {@link ActivityParticipationResourceInformation}
	 */
	public ActivityParticipationResourceInformation withResourceName(String resourceName) {
		setResourceName(resourceName);
		return this;
	}
	
	/**
	 * @param resourceUsageDuration the resourceUsageDuration to set
	 * @return {@link ActivityParticipationResourceInformation}
	 */
	public ActivityParticipationResourceInformation withResourceUsageDuration(SimDuration resourceUsageDuration) {
		setResourceUsageDuration(resourceUsageDuration);
		return this;
	}
	
	/**
	 * @param resourceType the resourceType to set
	 * @return {@link ActivityParticipationResourceInformation}
	 */
	public ActivityParticipationResourceInformation withResourceType(String resourceType) {
		setResourceType(resourceType);
		return this;
	}
	
	
	/**
	 * @param moveable a boolean indicating if the resource is considered to be moveable or not
	 * @return {@link ActivityParticipationResourceInformation}
	 */
	public ActivityParticipationResourceInformation withMoveability(boolean moveable) {
		setMoveable(moveable);
		return this;
	}
	
	/**
	 * @param resourceAgent the resourceAgent to set
	 */
	public void setResourceAgent(AgentID resourceAgent) {
	this.resourceAgent = resourceAgent;}
	

	/**
	 * @return the resourceName
	 */
	public String getResourceName()
	{
		return this.resourceName;
	}

	/**
	 * @param resourceName the resourceName to set
	 */
	public void setResourceName(String resourceName) {
	this.resourceName = resourceName;}
	

	/**
	 * @return the resourceUsageDuration
	 */
	public SimDuration getResourceUsageDuration()
	{
		return this.resourceUsageDuration;
	}

	/**
	 * @param resourceUsageDuration the resourceUsageDuration to set
	 */
	public void setResourceUsageDuration(SimDuration resourceUsageDuration) {
	this.resourceUsageDuration = resourceUsageDuration;}
	

	/**
	 * @return the activityName
	 */
	public String getActivityName()
	{
		return this.activityName;
	}

	/**
	 * @param activityName the activityName to set
	 */
	public void setActivityName(String activityName) {
	this.activityName = activityName;}
	

	/**
	 * @return the resourceType
	 */
	public String getResourceType()
	{
		return this.resourceType;
	}

	/**
	 * @param resourceType the resourceType to set
	 */
	public void setResourceType(String resourceType) {
	this.resourceType = resourceType;}

	/**
	 * @param activityID
	 * @return
	 */
	public ActivityParticipationResourceInformation withActivityName(String activityID)
	{
		setActivityName(activityID);
		return this;
	}

	/** @see java.lang.Object#hashCode() */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((activityName == null) ? 0 : activityName.hashCode());
		result = prime * result
				+ ((resourceAgent == null) ? 0 : resourceAgent.hashCode());
		result = prime * result
				+ ((resourceName == null) ? 0 : resourceName.hashCode());
		result = prime * result
				+ ((processID == null) ? 0 : processID.hashCode());
		result = prime * result
				+ ((processInstanceId == null) ? 0 : processInstanceId.hashCode());
		result = prime * result
				+ ((resourceType == null) ? 0 : resourceType.hashCode());
		result = prime
				* result
				+ ((resourceUsageDuration == null) ? 0 : resourceUsageDuration
						.hashCode());
		result = prime
				* result
				+ ((activityInstanceId == null) ? 0 : activityInstanceId
						.hashCode());
		return result;
	}

	/** @see java.lang.Object#equals(java.lang.Object) */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActivityParticipationResourceInformation other = (ActivityParticipationResourceInformation) obj;
		if (activityName == null)
		{
			if (other.activityName != null)
				return false;
		} else if (!activityName.equals(other.activityName))
			return false;
		if (resourceAgent == null)
		{
			if (other.resourceAgent != null)
				return false;
		} else if (!resourceAgent.equals(other.resourceAgent))
			return false;
		if (resourceName == null)
		{
			if (other.resourceName != null)
				return false;
		} else if (!resourceName.equals(other.resourceName))
			return false;
		if (resourceType != other.resourceType)
			return false;
		if (processID == null)
		{
			if (other.processID != null)
				return false;
		} else if (!processID.equals(other.processID))
			return false;
		if (processInstanceId == null)
		{
			if (other.processInstanceId != null)
				return false;
		} else if (!processInstanceId.equals(other.processInstanceId))
			return false;
		if (resourceUsageDuration == null)
		{
			if (other.resourceUsageDuration != null)
				return false;
		} else if (!resourceUsageDuration.equals(other.resourceUsageDuration))
			return false;
		if (activityInstanceId == null)
		{
			if (other.activityInstanceId != null)
				return false;
		} else if (!activityInstanceId.equals(other.resourceUsageDuration))
			return false;
		return true;
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
	public void setProcessID(String processID)
	{
		this.processID = processID;
	}
	
	/**
	 * @param processID the processID to set
	 * @return the {@link ActivityParticipationResourceInformation}
	 */
	public ActivityParticipationResourceInformation withProcessID(String processID){
		setProcessID(processID);
		return this;
	}

	/**
	 * @return the processInstanceId
	 */
	public String getProcessInstanceId()
	{
		return this.processInstanceId;
	}

	/**
	 * @param processInstanceId the processInstanceId to set
	 */
	public void setProcessInstanceId(String processInstanceId)
	{
		this.processInstanceId = processInstanceId;
	}
	

	/**
	 * @param processID the processInstanceID to set
	 * @return the {@link ActivityParticipationResourceInformation}
	 */
	public ActivityParticipationResourceInformation withInstanceProcessID(String processInstanceId){
		setProcessInstanceId(processInstanceId);
		return this;
	}

	/**
	 * @return the resourceInstanceName
	 */
	public String getResourceInstanceName()
	{
		return this.resourceInstanceName;
	}

	/**
	 * @param resourceInstanceName the resourceInstanceName to set
	 */
	public void setResourceInstanceName(String resourceInstanceName)
	{
		this.resourceInstanceName = resourceInstanceName;
	}
	
	/**
	 * 
	 * @param resourceInstanceName the resourceInstanceName to set
	 * @return the {@link ActivityParticipationResourceInformation}
	 */
	public ActivityParticipationResourceInformation withResourceInstanceName(String resourceInstanceName)
	{
		setResourceInstanceName(resourceInstanceName);
		return this;
	}

	/**
	 * 
	 * @param scheduledExecutionTime the time it should be invoked
	 */
	public void setScheduledExecutionTime(SimTime scheduledExecutionTime)
	{
		this.scheduledExecutionTime = scheduledExecutionTime;
	}
	
	/**
	 * 
	 * @return the scheduled time of invokement
	 */
	public SimTime getScheduledExecutionTime()
	{
		return this.scheduledExecutionTime;
	}

	/**
	 * @return the activityInstanceId
	 */
	public String getActivityInstanceId() {
		return activityInstanceId;
	}

	/**
	 * @param activityInstanceId the activityInstanceId to set
	 */
	public void setActivityInstanceId(String activityInstanceId) {
		this.activityInstanceId = activityInstanceId;
	}

	/**
	 * 
	 * @param activityInstanceId the activityInstanceId to set
	 * @return the {@link ActivityParticipationResourceInformation}
	 */
	public ActivityParticipationResourceInformation withActivityInstanceId(String activityInstanceId)
	{
		setActivityInstanceId(activityInstanceId);
		return this;
	}

	public boolean isMoveable() {
		return moveable;
	}

	public void setMoveable(boolean moveable) {
		this.moveable = moveable;
	}

}
