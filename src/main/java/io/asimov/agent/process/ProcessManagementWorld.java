package io.asimov.agent.process;

import io.arum.model.ARUMOrganizationWorld;
import io.arum.model.resource.assemblyline.AssemblyLineType;
import io.arum.model.resource.supply.Material;
import io.arum.model.resource.supply.SupplyType;
import io.asimov.model.process.Process;
import io.coala.agent.AgentID;
import io.coala.capability.CapabilityFactory;
import io.coala.capability.embody.Percept;

import java.util.List;
import java.util.Set;

import rx.Observable;

/**
 * {@link ProcessManagementWorld}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public interface ProcessManagementWorld extends ARUMOrganizationWorld
{

	/**
	 * {@link Factory}
	 * 
	 * @version $Revision: 1048 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 *
	 */
	interface Factory extends CapabilityFactory<ProcessManagementWorld>
	{
		// empty
	}

	Process getProcess(String processTypeID);

	Observable<Percept> makeObservation(String processTypeID);

	/**
	 * @param agent
	 * @return
	 */
	List<AssemblyLineType> getAssemblyLineTypesForAgentID(AgentID agent);

	/**
	 * @param resourceAgent
	 * @param resourceName
	 * @return
	 */
	List<Material> getMaterialsForSupplyType(AgentID resourceAgent,
			SupplyType resourceName);

}
