package io.asimov.model.process.impl;

import io.asimov.agent.process.ProcessManagementWorld;
import io.asimov.db.Datasource;
import io.asimov.model.AbstractASIMOVOrganizationtWorld;
import io.asimov.model.process.Process;
import io.asimov.model.sl.LegacySLUtil;
import io.coala.bind.Binder;
import io.coala.capability.embody.Percept;
import io.coala.log.InjectLogger;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observable;
import rx.subjects.ReplaySubject;

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

	
}
