package io.asimov.agent.resource.impl;

import io.asimov.agent.process.NonSkeletonActivityCapability;
import io.asimov.agent.resource.GenericResourceManagementWorld;
import io.asimov.db.Datasource;
import io.asimov.model.ASIMOVResourceDescriptor;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.EventType;
import io.asimov.model.resource.AbstractResourceManagementWorld;
import io.asimov.model.resource.ResourceDescriptor;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.log.InjectLogger;
import io.coala.time.SimTime;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * {@link GenericResourceManagementWorldImpl}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public class GenericResourceManagementWorldImpl extends
		AbstractResourceManagementWorld<ASIMOVResourceDescriptor> implements
		GenericResourceManagementWorld {

	/** */
	private static final long serialVersionUID = 1L;

	private boolean onSite = false;

	private static final Map<String, Integer> resourcesOnSite = new HashMap<String, Integer>();

	/** */
	private Subject<ActivityEvent, ActivityEvent> activity = PublishSubject
			.create();

	@InjectLogger
	private Logger LOG;

	/**
	 * {@link GenericResourceManagementWorldImpl} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected GenericResourceManagementWorldImpl(final Binder binder) {
		super(binder);
	}

	@Override
	public void initialize() throws Exception {
		super.initialize();
		this.entity = getBinder().inject(Datasource.class)
				.findResourceDescriptorByID(getOwnerID().getValue());
		this.entityType = this.entity.getType();
		if (this.entity.isMoveable()
				&& this.entity.getContainerResource() != null
				&& this.entity.getContainerResource().getConnectedResourceId() != null)
			setCurrentLocation(new AgentID(this.entity.getAgentID()
					.getModelID(), this.entity.getContainerResource()
					.getConnectedResourceId()));
		else if (this.entity.isInfrastructural())
			setCurrentLocation(this.entity.getAgentID());
		else
			setCurrentLocation(getOwnerID());
		if (this.entity.isUnAvailable())
			performActivityChange(null, null, null, null, Collections.singletonList(getOwnerID().getValue()), EventType.START_GLOBAL_UNAVAILABILITY);
		else
			performActivityChange(null, null, null, null, Collections.singletonList(getOwnerID().getValue()), EventType.STOP_GLOBAL_UNAVAILABILITY);
	}

	/** @see eu.a4ee.model.resource.PersonResourceManagementWorld#onActivity() */
	@Override
	public Observable<ActivityEvent> onActivityEvent() {
		return this.activity.asObservable();
	}

	/**
	 * @see PersonResourceManagementWorld#performActivityChange(String, String,
	 *      EventType)
	 */
	@Override
	public void performActivityChange(final String processID,
			final String processInstanceID, final String activityName,
			final String activityInstanceId, final List<String> resourceNames,
			final EventType eventType) throws Exception {
		LOG.info("fire ASIMOV activity participation event!");
		if (eventType.equals(EventType.TRANSIT_FROM_RESOURCE)
				|| eventType.equals(EventType.TRANSIT_TO_RESOURCE)) {
			if (resourceNames.size() != 2)
				throw new IllegalStateException(
						"Expected 2 resources: actor and target, but received "
								+ resourceNames.size() + " resources instead.");
			final String targetName = resourceNames.get(1);
			ASIMOVResourceDescriptor rd = getBinder().inject(Datasource.class)
					.findResourceDescriptorByID(targetName);
			if (rd != null)
				synchronized (resourcesOnSite) {
					int occupancy = resourcesOnSite
							.containsKey(targetName) ? resourcesOnSite
							.get(targetName) : 0;
					if (eventType.equals(EventType.TRANSIT_TO_RESOURCE)) {
						occupancy++;
						resourcesOnSite.put(targetName, occupancy);
						setCurrentLocation(rd.getAgentID());
					} else if (eventType
							.equals(EventType.TRANSIT_FROM_RESOURCE)) {
						occupancy--;
						resourcesOnSite.put(targetName, occupancy);
					}
				}
		}
		fireAndForget(processID, processInstanceID, activityName,
				activityInstanceId, eventType, resourceNames, this.activity);
	}

	@Override
	public boolean isMoveable() {
		return this.entity.isMoveable();
	}

	@Override
	public void enteredSite(SimTime time) {
		if (onSite)
			throw new IllegalStateException(
					"entered time has been set while already on site!");
		onSite = true;
		LOG.info(getOwnerID() + " has entered the site");
	}

	@Override
	public void leftSite(SimTime time) {
		if (!onSite)
			throw new IllegalStateException(
					"leave time has been set while not on site!");
		onSite = false;
		getBinder().inject(NonSkeletonActivityCapability.class).onLeftSite();
		LOG.info(getOwnerID() + " has left the site");
	}

}
