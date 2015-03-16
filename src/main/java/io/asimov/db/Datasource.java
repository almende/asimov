package io.asimov.db;

import io.arum.model.events.MaterialEvent;
import io.arum.model.events.MovementEvent;
import io.arum.model.resource.assemblyline.AssemblyLine;
import io.arum.model.resource.person.Person;
import io.arum.model.resource.supply.Material;
import io.asimov.agent.scenario.Replication;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.Event;
import io.asimov.model.events.EventType;
import io.asimov.model.process.Process;
import io.coala.capability.BasicCapabilityStatus;
import io.coala.capability.Capability;
import io.coala.capability.CapabilityFactory;

import javax.persistence.EntityManagerFactory;

/**
 * {@link Datasource}
 * 
 * @date $Date: 2014-11-24 12:10:07 +0100 (ma, 24 nov 2014) $
 * @version $Revision: 1122 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@SuppressWarnings("rawtypes")
public interface Datasource extends Capability<BasicCapabilityStatus>
{

	/**
	 * {@link Factory}
	 * 
	 * @version $Revision: 1122 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 *
	 */
	interface Factory extends CapabilityFactory<Datasource>
	{
		// empty
	}

	/**
	 * @return the JPA {@link EntityManagerFactory}
	 * @throws Exception
	 */
	EntityManagerFactory getEntityManagerFactory()
			throws Exception;

	/**
	 * should clean up all the {@link Datasource}'s resources
	 */
	void close();

	/**
	 * @param assemblyLine the {@link AssemblyLine} to save
	 * @param replicationID the id of the replication this entity belongs to set to null to save 'globally'.
	 */
	void save(AssemblyLine assemblyLine);

	/**
	 * @param replicationID the id of the replication this entity belongs to set to null to save 'globally'.
	 * @return the {@link AssemblyLine}s
	 */
	Iterable<AssemblyLine> findAssemblyLines();
	
	/**
	 * @param replicationID the id of the replication this entity belongs to set to null to save 'globally'.
	 * @return the {@link Person}s
	 */
	Iterable<Person> findPersons();
	
	
	/**
	 * @param replicationID the id of the replication this entity belongs to set to null to save 'globally'.
	 * @return the {@link Material}s
	 */
	Iterable<Material> findMaterials();

	/**
	 * @param assemblyLineID the identifier of the {@link AssemblyLine} to load
	 * @param replicationID the id of the replication this entity belongs to set to null to save 'globally'.
	 * @return the {@link AssemblyLine} or {@code null} if none found
	 */
	AssemblyLine findAssemblyLineByID(String AssemblyLine);
	
	/**
	 * @param replicationID the id of the replication this entity belongs to set to null to save 'globally'.
	 * @return the {@link Material}s
	 */
	Iterable<Process> findProcesses();

	/**
	 * @param replicationID the id of the replication this entity belongs to set to null to save 'globally'.
	 * @param processID the identifier of the {@link Process} to load
	 * @return the {@link Process} or {@code null} if none found
	 */
	Process findProcessByID(String processID);
	
	/**
	 * @param replicationID the id of the replication this entity belongs to set to null to save 'globally'.
	 * @param personID the identifier of the {@link Person} to load
	 * @return the {@link AssemblyLine} or {@code null} if none found
	 */
	Person findPersonByID(String personID);
	
	
	/**
	 * @param replicationID the id of the replication this entity belongs to set to null to save 'globally'.
	 * @param materialID the identifier of the {@link Material} to load
	 * @return the {@link Material} or {@code null} if none found
	 */
	Material findMaterialByID(String materialID);

	/**
	 * @param event the {@link Event} to save
	 */
	void save(Event event);
	
	/**
	 * @param replicationID the id of the replication this entity belongs to set to null to save 'globally'.
	 * @param event the {@link Process} to save
	 */
	void save(Process process);

	/**
	 * @return an {@link Iterable} stream of all {@link MovementEvent} for specified replication
	 */
	Iterable<MovementEvent> findMovementEvents();

	/**
	 * @return an {@link Iterable} stream of all {@link MaterialEvent} for specified replication
	 */
	Iterable<MaterialEvent> findMaterialEvents();

	/**
	 * @return an {@link Iterable} stream of all {@link ActivityEvent} for specified replication
	 */
	Iterable<ActivityEvent> findActivityEvents();
	
//	/**
//	 * @return an {@link Iterable} stream of all {@link MaterialEvent} for specified replication
//	 */
//	Iterable<LightingEvent> findLightingEvents();

	/**
	 * @param type the {@link EventType} to match
	 * @return an {@link Iterable} stream of events with specified {@code type}
	 */
	//Iterable<Event> findEvents(EventType type);

	/**
	 * @return an {@link Iterable} stream of events with specified {@code name}
	 */
	Iterable<Event> findEvents();

	/**
	 * @param types the {@link EventType} to match
	 * @return an {@link Iterable} stream of events with specified {@code name}
	 */
	Iterable<Event> findEvents(EventType... types);
	
	/**
	 * @param replicationId the id {@link Replication} to match
	 * @param types the {@link EventType} to match
	 * @return an {@link Iterable} stream of events with specified {@code name}
	 */
	Iterable<MovementEvent> findMovementEvents(EventType... types);

	/**
	 * @param types the {@link EventType} to match
	 * @return an {@link Iterable} stream of events with specified {@code name}
	 */
	Iterable<ActivityEvent> findActivityEvents(EventType... types);
	
	/**
	 * @param types the {@link EventType} to match
	 * @return an {@link Iterable} stream of events with specified {@code name}
	 */
	Iterable<MaterialEvent> findMaterialEvents(EventType... types);

	
	/**
	 * Get all replications stored in the database
	 * @return replications
	 */
	Iterable<Replication> findReplications();

	/**
	 * Find a replication by id
	 * @param id
	 * @return replication or {@code  null} if not found
	 */
	Replication findReplication();
	
	
	
	/**
	 * Find a gbXMLDocument by id.
	 * Returns null if replication is not found.
	 * @param id
	 * @return gbXMLDocument
	 */
	//SimulationFileDocument findSimulationFileDocument(String id);

	/**
	 * Save a new replication in the database
	 * @param replication
	 */
	void save(Replication replication);
	
	
	/**
	 * Save a gbXML file in the database
	 * @param replication
	 */
	//void save(SimulationFileDocument simulationFileDocument);
	
	/**
	 * Save a Material bean in the database
	 * @param material the Material to save
	 * @param replicationID the id of the replication this entity belongs to set to null to save 'globally'.
	 */
	void save(Material material);
	
	/**
	 * Save a Person bean in the database
	 * @param person the occpant to save
	 * @param replicationID the id of the replication this entity belongs to set to null to save 'globally'.
	 */
	void save(Person person);


	/**
	 * Update an existing replication in the database
	 * @param replication
	 */
	void update(Replication replication);
	
	/**
	 * Remove a replication from the database
	 * @param replication
	 */
	void remove(Replication replication);

	/**
	 * Remove a replication from the database by its id
	 * @param id
	 */
	void removeReplication();

	/**
	 * @param replicationId the id {@link Replication} to match
	 */
	void removeEvents();
	
	/**
	 * @param replicationId the id {@link Replication} to match
	 */
	void removePersons();
	
	/**
	 * @param replicationId the id {@link Replication} to match
	 */
	void removeAssemblyLines();
	
	/**
	 * @param replicationId the id {@link Replication} to match
	 */
	void removeMaterials();
	
	/**
	 * @param replicationId the id {@link Replication} to match
	 */
	void removeProcesses();


//	/**
//	 * Remove a calibration from the database by its id
//	 * @param id
//	 */
//	void removeCalibration(String id);

	/**
	 * @param replicationID
	 * @return if this instance is for replication withID
	 */
	boolean hasReplication(String replicationID);

	
}
