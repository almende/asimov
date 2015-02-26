package io.asimov.agent.scenario;

import io.arum.model.ARUMOrganizationWorld;
import io.arum.model.resource.assemblyline.AssemblyLine;
import io.arum.model.resource.person.Person;
import io.arum.model.resource.supply.Material;
import io.coala.agent.Agent;
import io.coala.agent.AgentID;
import io.coala.capability.CapabilityFactory;
import io.coala.random.RandomDistribution;
import io.coala.time.SimDuration;
import io.coala.time.SimTime;

import java.util.Set;

import rx.Observable;

/**
 * {@link ScenarioManagementWorld}
 * 
 * @version $Revision: 1072 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public interface ScenarioManagementWorld extends ARUMOrganizationWorld
{

	/**
	 * {@link Factory}
	 * 
	 * @version $Revision: 1072 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 */
	interface Factory extends CapabilityFactory<ScenarioManagementWorld>
	{
		// empty
	}

	String NUMBER_OF_PERSONS_KEY = "nofPersons";

	String SCENARIO_FILE_KEY = "scenarioFile";

	Replication getReplication();

	Observable<ProcessEvent> onProcessEvent();

	Observable<ResourceEvent> onResources();

	Set<AgentID> getCurrentAssemblyLineIDs();

	Set<AgentID> getCurrentPersonIDs();

	Set<AgentID> getCurrentMaterialIDs();
	
	int getCurrentResourceStatusHash();
	
	Observable<Integer> resourceStatusHash();
	
	void updateResourceStatusHash(String base);

	/**
	 * @param processTypeID
	 * @return
	 */
	RandomDistribution<SimDuration> getProcessStartTimeOfDayDist(
			final String processTypeID);
	
	/**
	 * @param resourceId
	 * @return
	 */
	RandomDistribution<SimDuration> getResourceUnavailabilityDist(
			final String resourceId);


	/**
	 * @return
	 */
	Iterable<AssemblyLine> getAssemblyLines();

	/**
	 * @return
	 */
	Iterable<Material> getMaterials();
	
	/**
	 * @return
	 */
	Iterable<Person> getPersons();

	/**
	 * {@link ResourceEventType}
	 * 
	 * @version $Revision: 1072 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public enum ResourceEventType
	{
		/** */
		ADDED,

		/** */
		REMOVED,

		;
	}

	/**
	 * 
	 * {@link ProcessEventType}
	 * 
	 * @date $Date: 2014-09-19 09:38:53 +0200 (vr, 19 sep 2014) $
	 * @version $Revision: 1072 $
	 * @author <a href="mailto:suki@almende.org">suki</a>
	 * 
	 */
	public enum ProcessEventType
	{
		/** */
		REQUESTED,

		/**
		 * 
		 */
		INSTANTIATED,

		/**
		 * 
		 */
		ALLOCATED,

		/**
		 * 
		 */
		NOT_ALLOCATED,

		/**
		 * 
		 */
		STARTED,

		/**
		 *
		 */
		FAILED,

		/**
		 *
		 */
		COMPLETED,

		;
	}

	/**
	 * 
	 * {@link ProcessEvent}
	 * 
	 * @date $Date: 2014-09-19 09:38:53 +0200 (vr, 19 sep 2014) $
	 * @version $Revision: 1072 $
	 * @author <a href="mailto:suki@almende.org">suki</a>
	 * 
	 */
	public interface ProcessEvent
	{
		/**
		 * @return
		 */
		String getProcessTypeID();

		/**
		 * @return
		 */
		AgentID getProcessInstanceID();

		/**
		 * @return
		 */
		ProcessEventType getEventType();
	}

	/**
	 * {@link ResourceEvent}
	 * 
	 * @version $Revision: 1072 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public interface ResourceEvent extends Comparable<ResourceEvent>
	{
		/**
		 * @return
		 */
		AgentID getResourceID();

		/**
		 * 
		 * @return
		 */
		Class<? extends Agent> getResourceType();

		/**
		 * @return
		 */
		ResourceEventType getEventType();
		
		/**
		 * the time to fire the event
		 */
		
		SimTime getEventTime();
	}

}
