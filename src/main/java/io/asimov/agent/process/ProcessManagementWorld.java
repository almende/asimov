package io.asimov.agent.process;

import io.asimov.model.ASIMOVOrganizationWorld;
import io.asimov.model.events.EventType;
import io.asimov.model.process.Process;
import io.coala.agent.AgentID;
import io.coala.capability.CapabilityFactory;
import io.coala.capability.embody.Percept;

import java.util.List;

import rx.Observable;

/**
 * {@link ProcessManagementWorld}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public interface ProcessManagementWorld extends ASIMOVOrganizationWorld
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
//
//	/**
//	 * @param agent
//	 * @return
//	 */
//	List<AssemblyLineType> getAssemblyLineTypesForAgentID(AgentID agent);
//
//	/**
//	 * @param resourceAgent
//	 * @param resourceName
//	 * @return
//	 */
//	List<Material> getMaterialsForSupplyType(AgentID resourceAgent,
//			SupplyType resourceName);
//	
	
}
