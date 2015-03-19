package io.asimov.agent.resource;

import rx.Observable;
import io.asimov.model.ASIMOVResourceDescriptor;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.EventType;
import io.coala.capability.CapabilityFactory;

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
	void performActivityChange(String processID, String processInstanceID,  String activityName,  String activityInstanceId, String reourceName, EventType eventType) throws Exception;

	public boolean isMoveable();
	
}
