package io.asimov.model.process.impl;

import io.asimov.agent.process.ProcessManagementWorld;
import io.asimov.db.Datasource;
import io.asimov.model.AbstractASIMOVOrganizationtWorld;
import io.asimov.model.TraceService;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.Event;
import io.asimov.model.events.EventType;
import io.asimov.model.process.Process;
import io.asimov.model.sl.LegacySLUtil;
import io.coala.bind.Binder;
import io.coala.capability.embody.Percept;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.log.InjectLogger;
import io.coala.time.SimTime;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

/**
 * {@link ProcessManagementWorldImpl}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 * 
 */

public class ProcessManagementWorldImpl extends AbstractASIMOVOrganizationtWorld
		implements ProcessManagementWorld
{

	/** */
	private static final long serialVersionUID = 1L;

	@InjectLogger
	private Logger LOG;

	private final Map<String, Process> processCache = new HashMap<>();
	
	/** */
	private Subject<ActivityEvent, ActivityEvent> process = PublishSubject
			.create();


	/**
	 * {@link ProcessManagementWorldImpl} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected ProcessManagementWorldImpl(final Binder binder)
	{
		super(binder);
	}

	/** @see ProcessManagementWorld#getProcess(String) */
	@Override
	public Process getProcess(final String processTypeID)
	{
		Process result = this.processCache.get(processTypeID);
		if (result != null)
			return result;

		result = getBinder().inject(Datasource.class).findProcessByID(
				processTypeID);
		if (result == null)
			throw new NullPointerException("Problem retrieving process type: "
					+ processTypeID);
		LOG.info("Retrieved process with type ID: " + processTypeID
				+ " from datasource: " + result);
		this.processCache.put(processTypeID, result);
		return result;
	}

	/** @see ProcessManagementWorld#makeObservation(String) */
	@Override
	public Observable<Percept> makeObservation(final String processTypeID)
	{
		final ReplaySubject<Percept> result = ReplaySubject.create();
		// FIXME deprecate SL
		try {
			final Process process = getProcess(processTypeID);
			LegacySLUtil.makeObservation(result, process, getBinder().getID());
		} catch (NullPointerException ne) {
			result.onError(ne);
		}
		return result.asObservable();
	}
	
	/**
	 * @see ProcessManagementWorld#performProcessChange(String, String,
	 *      EventType)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void performProcessChange(final String processID,
			final String processInstanceID, 
			final EventType eventType) throws Exception {
		LOG.info("fire ASIMOV activity participation event!");
		if (eventType.equals(EventType.START_PROCESS)
				|| eventType.equals(EventType.STOP_PROCESS)) {
			fireAndForget(processID, processInstanceID, null,
					null, eventType, Collections.EMPTY_LIST, this.process);
		}
		
	}

	@SuppressWarnings("unchecked")
	protected <T extends Event<?>> void fireAndForget(final String processID,
			final String processInstanceID, final String activityName,
			final String activityInstanceId, final EventType eventType,
			final List<String> involvedResources, final Observer<T> publisher) {
		final SimTime now = getBinder().inject(ReplicatingCapability.class)
				.getTime();
		publisher.onNext((T) TraceService.getInstance(
				getOwnerID().getModelID().getValue()).saveEvent(
				getBinder().inject(Datasource.class), processID,
				processInstanceID, activityName, activityInstanceId,
				involvedResources, eventType, now));
	}

}
