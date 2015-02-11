package io.arum.model.resource.supply;

import io.arum.model.events.MaterialEvent;
import io.asimov.agent.resource.ResourceManagementWorld;
import io.asimov.model.events.EventType;
import io.coala.capability.CapabilityFactory;
import rx.Observable;

/**
 * {@link MaterialResourceManagementWorld}
 * 
 * TODO Move to even more generic worldView in sim-common
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public interface MaterialResourceManagementWorld extends
		ResourceManagementWorld<Material>
{

	interface Factory extends CapabilityFactory<MaterialResourceManagementWorld>
	{
		// empty
	}

	/**
	 * @param equipmentName
	 * @param beName
	 * @param eventType
	 * @return equipmentInstanceID
	 */
	void performUsageChange(String processID, String processInstanceID, String activityName, String equipmentName, String occupantName, final String assemblyLineRef, EventType eventType) throws Exception;

//	/** @param event the newly triggered {@link EquipmentEvent} */
	Observable<MaterialEvent> onUsage();

	/**
	 * @param equipmentType the type of equipment
	 * @return a List of all available equipments for the specified type in the space
	 */
//	List<Equipment> getEquipmentsOfType(String equipmentType);
	

}
