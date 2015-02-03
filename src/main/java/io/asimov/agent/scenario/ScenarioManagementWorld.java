package io.asimov.agent.scenario;

import io.arum.model.ARUMOrganizationWorld;
import io.asimov.agent.resource.AssemblyLine;
import io.asimov.agent.resource.Material;
import io.asimov.agent.resource.Person;
import io.coala.agent.Agent;
import io.coala.agent.AgentID;
import io.coala.capability.CapabilityFactory;
import io.coala.random.RandomDistribution;
import io.coala.time.SimDuration;

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

	/**
	 * @param processTypeID
	 * @return
	 */
	RandomDistribution<SimDuration> getProcessStartTimeOfDayDist(
			final String processTypeID);

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
	public interface ResourceEvent
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
	}

}
