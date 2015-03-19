package io.asimov.db;

import io.asimov.agent.scenario.Replication;
import io.asimov.model.ASIMOVResourceDescriptor;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.Event;
import io.asimov.model.events.EventType;
import io.asimov.model.process.Process;
import io.asimov.model.resource.ResourceDescriptor;
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
	 * @param resourceDescriptor the {@link ASIMOVResourceDescriptor} to save
	 * @param replicationID the id of the replication this entity belongs to set to null to save 'globally'.
	 */
	void save(ASIMOVResourceDescriptor resourceDescriptor);

	/**
	 * @param replicationID the id of the replication this entity belongs to set to null to save 'globally'.
	 * @return the {@link ASIMOVResourceDescriptor}s
	 */
	Iterable<ASIMOVResourceDescriptor> findResourceDescriptors();
	

	/**
	 * @param resourceDescriptorID the identifier of the {@link ASIMOVResourceDescriptor} to load
	 * @param replicationID the id of the replication this entity belongs to set to null to save 'globally'.
	 * @return the {@link ASIMOVResourceDescriptor} or {@code null} if none found
	 */
	ASIMOVResourceDescriptor findResourceDescriptorByID(String resourceDescriptor);
	
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
	 * @param event the {@link Event} to save
	 */
	void save(Event event);
	
	/**
	 * @param replicationID the id of the replication this entity belongs to set to null to save 'globally'.
	 * @param event the {@link Process} to save
	 */
	void save(Process process);


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
	 * @param types the {@link EventType} to match
	 * @return an {@link Iterable} stream of events with specified {@code name}
	 */
	Iterable<ActivityEvent> findActivityEvents(EventType... types);

	
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
	 * Save a new replication in the database
	 * @param replication
	 */
	void save(Replication replication);


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
	void removeResourceDescriptors();
	
	
	/**
	 * @param replicationId the id {@link Replication} to match
	 */
	void removeProcesses();


	/**
	 * @param replicationID
	 * @return if this instance is for replication withID
	 */
	boolean hasReplication(String replicationID);

	
}
