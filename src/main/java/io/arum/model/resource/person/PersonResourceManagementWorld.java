package io.arum.model.resource.person;

import io.arum.model.events.MovementEvent;
import io.asimov.agent.resource.ResourceManagementWorld;
import io.asimov.model.Body;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.EventType;
import io.coala.capability.CapabilityFactory;
import io.coala.time.SimDuration;
import io.coala.time.SimTime;
import rx.Observable;

/**
 * {@link PersonResourceManagementWorld}
 * 
 * TODO Move to even more generic worldView in sim-common
 * 
 * @version $Revision: 1068 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public interface PersonResourceManagementWorld
		extends ResourceManagementWorld<Person>
{

	interface Factory extends CapabilityFactory<PersonResourceManagementWorld>
	{
		// empty
	}
	
	/**
	 * @param agentID
	 * @param eventType
	 * @param timeStamp
	 * @param refID
	 * @param buildingElementName
	 */
//	void performEvent(AgentID agentID, EventType eventType, SimTime timeStamp,
//			String refID, String buildingElementName);

	/**
	 * @param timeOffset
	 * @param sourceBody
	 * @param targetBody
	 * @return
	 */
	SimDuration calculateTravelTime(SimTime timeOffset, Body sourceBody,
			Body targetBody);

	/**
	 * @param timeOffset
	 * @param sourceBody
	 * @param targetBody
	 * @param walkingSpeedInMilliMeterPerSecond
	 * @return
	 */
	SimDuration calculateTravelTime(SimTime timeOffset, Body sourceBody,
			Body targetBody, Long walkingSpeedInMilliMeterPerSecond);
	
	/**
	 * 
	 * @param timeOffset
	 * @param targetBody
	 * @return
	 */
//	public SimDuration calculateTravelTimeToResource(SimTime timeOffset,
//			Occupant targetBody);
	
	/**
	 * 
	 * @param timeOffset
	 * @param targetBody
	 * @return
	 */
	public SimDuration calculateTravelTimeToBody(SimTime timeOffset,
			Body targetBody);
	
	/**
	 * To determine the start of a working day
	 * @param time the time the occupant entered the building/campus/etc.
	 */
	void enteredSite(SimTime time);
	
	/**
	 * To determine the end of a working day
	 * @param time the time the occupant left the building/campus/etc.
	 */
	void leftSite(SimTime time);
	
	/**
	 * @Deprecated
	 * To determine the desired end of a working day
	 * @return the time the occupant should leave the building/campus/etc.
	 */
	//public SimTime desiredSiteLeaveTime(SimTime now);
	
	/**
	 * To determine the desired start of a working day
	 * @patram now the time to calculate the desired enter time on
	 * @return the relative duration from now  the occupant should enter the building/campus/etc.
	 */
	public SimDuration onSiteDelay(SimTime now);

	/**
	 * @param activityName
	 * @param beName
	 * @param eventType
	 * @return activityInstanceID
	 */
	void performActivityChange(String processID, String processInstanceID,  String activityName,  String activityInstanceId, String beName, EventType eventType) throws Exception;

	/**
	 * @param beName
	 * @param eventType
	 * @return equipmentInstanceID
	 */
	void performOccupancyChange(String processID,  String processInstanceID, String activityName, String activityInstanceId, String beName, EventType eventType) throws Exception;

	/** @param event the newly triggered {@link MovementEvent} */
	Observable<MovementEvent> onMovement();

	/** @param event the newly triggered {@link ActivityEvent} */
	Observable<ActivityEvent> onActivity();
	

}
