package io.asimov.agent.resource.impl;

import java.util.Collections;

import io.asimov.agent.resource.GenericResourceManagementWorld;
import io.asimov.db.Datasource;
import io.asimov.model.ASIMOVResourceDescriptor;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.EventType;
import io.asimov.model.resource.AbstractResourceManagementWorld;
import io.coala.bind.Binder;
import io.coala.log.InjectLogger;

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
		setCurrentLocation(getOwnerID());
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
			final String activityInstanceId, final String resourceName,
			final EventType eventType) throws Exception {
		LOG.info("fire ASIMOV activity event!");
		fireAndForget(processID, processInstanceID, activityName,
				activityInstanceId, eventType,
				Collections.singletonList(resourceName), this.activity);
	}

	@Override
	public boolean isMoveable() {
		return this.entity.isMoveable();
	}

}
