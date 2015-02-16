package io.arum.model.resource.assemblyline;

import io.asimov.agent.resource.ResourceManagementWorld;
import io.coala.capability.CapabilityFactory;

/**
 * {@link SpaceResourceManagementWorld}
 * 
 * TODO Move to even more generic worldView in sim-common
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public interface AssemblyLineResourceManagementWorld extends
		ResourceManagementWorld<AssemblyLine>
{

	interface Factory extends CapabilityFactory<AssemblyLineResourceManagementWorld>
	{
		// empty
	}

//	/**
//	 * @param equipmentName
//	 * @param beName
//	 * @param eventType
//	 * @return equipmentInstanceID
//	 */
//	void performUsageChange(String processID, String processInstanceID, String activityName, String equipmentName, String occupantName, EventType eventType) throws Exception;

//	/** @param event the newly triggered {@link EquipmentEvent} */
//	Observable<EquipmentEvent> onUsage();
//
//	/**
//	 * @param equipmentType the type of equipment
//	 * @return a List of all available equipments for the specified type in the space
//	 */
//	List<Equipment> getEquipmentsOfType(String equipmentType);
	

}
