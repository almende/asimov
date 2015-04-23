package io.asimov.agent.resource;

import io.asimov.model.ASIMOVResourceDescriptor;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.EventType;
import io.coala.capability.CapabilityFactory;
import io.coala.time.SimTime;

import java.util.List;

import rx.Observable;

public interface GenericResourceManagementWorld extends ResourceManagementWorld<ASIMOVResourceDescriptor> {
	
	interface Factory extends CapabilityFactory<GenericResourceManagementWorld>
	{
		// empty
	}

	public Observable<ActivityEvent> onActivityEvent();
	
	/**
	 * @param activityName
	 * @param resourceName
	 * @param eventType
	 * @return activityInstanceID
	 */
	void performActivityChange(String processID, String processInstanceID,  String activityName,  String activityInstanceId, List<String> resourceNames, EventType eventType) throws Exception;

	/**
	 * To determine the start of a active simulation period for this resource
	 * @param time the time the resource entered the building/campus/etc.
	 */
	void enteredSite(SimTime time);
	
	/**
	 * To determine the end of a active simulation period for this resource
	 * @param time the time the resource left the building/campus/etc.
	 */
	void leftSite(SimTime time);
	
	
	
	public boolean isMoveable();
	
}
