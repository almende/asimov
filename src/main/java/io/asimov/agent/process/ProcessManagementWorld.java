package io.asimov.agent.process;

import io.arum.model.ARUMOrganizationWorld;
import io.asimov.agent.resource.Material;
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
	String getAssemblyLineTypeForAgentID(AgentID agent);

	/**
	 * @param resourceAgent
	 * @param resourceName
	 * @return
	 */
	List<Material> getMaterialsOfType(AgentID resourceAgent,
			String resourceName);

}
