package io.arum.model.process.impl;

import io.arum.model.AbstractARUMOrganizationtWorld;
import io.arum.model.resource.assemblyline.AssemblyLine;
import io.arum.model.resource.assemblyline.AssemblyLineType;
import io.arum.model.resource.supply.Material;
import io.arum.model.resource.supply.SupplyType;
import io.asimov.agent.process.ProcessManagementWorld;
import io.asimov.db.Datasource;
import io.asimov.model.process.Process;
import io.asimov.model.sl.LegacySLUtil;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.capability.embody.Percept;
import io.coala.log.InjectLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

public class ProcessManagementWorldImpl extends AbstractARUMOrganizationtWorld
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

	/** @see ProcessManagementWorld#getAssemblyLineTypeForAgentID(AgentID) */
	@Override
	public List<AssemblyLineType> getAssemblyLineTypesForAgentID(final AgentID agentID)
	{
		final AssemblyLine assemblyLine = getBinder().inject(Datasource.class).findAssemblyLineByID(
				agentID.getValue());
		if (assemblyLine == null)
		{
			throw new NullPointerException("No assemblyLine found for : "
					+ agentID.getValue());
		}
		if (assemblyLine.getTypes().isEmpty())
		{
			new NullPointerException("No type for assemblyLine: " + assemblyLine)
					.printStackTrace();
			return null;
		}
		return assemblyLine.getTypes();
	}

	@Override
	public List<Material> getMaterialsForSupplyType(AgentID resourceAgent,
			SupplyType materialType) {
		List<Material> result = new ArrayList<Material>();
		for (Material r : getBinder().inject(Datasource.class).findMaterials()) {
			for (SupplyType t : r.getTypes())
				if (t.getName().equalsIgnoreCase(materialType.getName())) {
					result.add(r);
				}
		}
		return result;
	}
}
