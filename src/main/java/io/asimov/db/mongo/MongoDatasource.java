package io.asimov.db.mongo;

import io.arum.model.events.MaterialEvent;
import io.arum.model.events.MovementEvent;
import io.arum.model.resource.ResourceSubtype;
import io.arum.model.resource.assemblyline.AssemblyLine;
import io.arum.model.resource.assemblyline.AssemblyLineType;
import io.arum.model.resource.person.Person;
import io.arum.model.resource.person.PersonRole;
import io.arum.model.resource.supply.Material;
import io.arum.model.resource.supply.SupplyType;
import io.asimov.agent.scenario.Replication;
import io.asimov.db.Datasource;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.Event;
import io.asimov.model.events.EventType;
import io.asimov.model.process.Process;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.bind.BinderFactory;
import io.coala.bind.BinderFactoryConfig;
import io.coala.capability.BasicCapability;
import io.coala.log.LogUtil;
import io.coala.model.ModelID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

import org.apache.log4j.Logger;
import org.joda.time.Interval;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.JacksonMapper;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

/**
 * {@link MongoDatasource}
 * 
 * @date $Date$
 * @version $Revision$
 * @author <a href="mailto:jos@almende.org">jos</a>
 * 
 */
@SuppressWarnings("rawtypes")
public class MongoDatasource extends BasicCapability implements Datasource
{
	/** */
	private static final long serialVersionUID = 1L;

	/** */
	public static final String EMBEDDED_PORT_PROPERTY = "eu.asimov.embedded-mongo-port";

	// TODO: store the database properties in a configuration file
	private static final String DB_HOST = "localhost";
	private static final int DB_PORT = 27017;
	private static final String DB_JPA_UNIT = "mongo";
	protected static String DB_NAME = "arum";

	private boolean eventsAreIndexed = false;

	// TODO get from @Entity(name=...) JPA annotations
	protected static String MONGO_SIMFILE_COLLECTION = "cim";
	protected static String MONGO_ASSEMBLY_LINES_COLLECTION = "assemblyLines";
	protected static String MONGO_MATERIALS_COLLECTION = "materials";
	protected static String MONGO_PROCESS_COLLECTION = "processes";
	protected static String MONGO_EVENTS_COLLECTION = "events";
	protected static String MONGO_KPI_COLLECTION = "kpi";
	protected static String MONGO_PERSONS_COLLECTION = "persons";
	protected static String MONGO_SIMULATOR_COLLECTION = "simulator";
	protected static String MONGO_REPLICATIONS_COLLECTION = "replications";
	protected static String MONGO_PERSON_EVENTS_COLLECTION = "measuredEvents";


	// TODO: remove hard-coded id field name, get from JPA annotations?
	protected static String REPLICATION_FOREIGN_ID_FIELDNAME = "replicationID";
	protected static String REPLICATION_ID_FIELDNAME = "id";
	protected static String CALIBRATION_ID_FIELDNAME = "id";
	protected static String CALIBRATION_FOREIGN_ID_FIELDNAME = "calibrationID";
	protected static String PROJECT_ID_FIELDNAME = "id";
	protected static String EVENT_REPLICATION_ID_FIELDNAME = "replicationID";
	protected static String EVENT_EXECUTION_TIME_FIELDNAME = "executionTime";
	protected static String EVENT_EXECUTION_TIME_VALUE_FIELDNAME = "value";
	protected static String EVENT_TYPE_FIELDNAME = "type";
	protected static String EVENT_TYPE_NAME_FIELDNAME = "name";
	protected static String CIM_ID_FIELDNAME = "id";
	protected static String EVENTS_COLLECTION_FOREIGN_ID = "eventCollectionId";
	protected static  String EVENT_PAIR_ID_FIELDNAME = "eventPairId";


	/** */
	private static final Logger LOG = LogUtil.getLogger(MongoDatasource.class);

	/** singleton instance */
	private static List<Datasource> INSTANCES = new ArrayList<Datasource>();

	/** */
	private final String replicationID;

	/** */
	private EntityManagerFactory emf;

	/** */
	private static MongoClient mongo;

	/** */
	private static Jongo jongo = null;

	/** */
	private final static Map<String, MongoCollection> collectionCache = Collections
			.synchronizedMap(new HashMap<String, MongoCollection>());



	/**
	 * @return the {@link MongoDatasource} instance for this JVM
	 * @throws Exception
	 */
	public static synchronized Datasource getInstance(final Binder binder)
			throws Exception
	{

		for (Datasource i : INSTANCES)
		{
			if (i.hasReplication(binder.getID().getModelID().getValue()))
			{
				if (Thread.interrupted())
					LOG.info("Cleaned up thread interrupted status");
				return i;
			}
		}
		Datasource INSTANCE = new MongoDatasource(binder);
		INSTANCES.add(INSTANCE);

		if (Thread.interrupted())
			LOG.info("Cleaned up thread interrupted status");
		return INSTANCE;
	}

	/**
	 * @return the {@link MongoDatasource} instance for this JVM
	 * @throws Exception
	 */
	public static synchronized Datasource getInstance(final String replicationID)
			throws Exception
	{

		for (Datasource i : INSTANCES)
		{
			if (i.hasReplication(replicationID))
			{
				if (Thread.interrupted())
					LOG.info("Cleaned up thread interrupted status");
				return i;
			}
		}
		Datasource INSTANCE = new MongoDatasource(replicationID);
		INSTANCES.add(INSTANCE);

		if (Thread.interrupted())
			LOG.info("Cleaned up thread interrupted status");
		return INSTANCE;
	}

	/**
	 * {@link MongoDatasource} constructor
	 * 
	 * @throws Exception
	 */
	@Inject
	public MongoDatasource(final Binder binder) throws Exception
	{
		super(binder);
		if (mongo == null)
			synchronized (MongoDatasource.class)
			{
				try
				{
					mongo = MongoUtil.connectMongo(DB_HOST, DB_PORT);
				} catch (final Exception e)
				{
					final int randomPort = MongoUtil
							.startEmbeddedMongo(
									Integer.valueOf(System.getProperty(
											EMBEDDED_PORT_PROPERTY, "0")))
							.getConfig().net().getPort();
					System.setProperty(EMBEDDED_PORT_PROPERTY, "" + randomPort);
					mongo = MongoUtil.connectMongo("localhost", randomPort);
				}
			}
		this.replicationID = binder.getID().getModelID().getValue();
	}

	/**
	 * {@link MongoDatasource} constructor
	 * 
	 * @throws Exception
	 */
	public MongoDatasource(final String replicationID) throws Exception
	{
		this(BinderFactory.Builder
				.fromConfig(BinderFactoryConfig.Builder.fromFile("asimov.properties").build())
				.build()
				.create(new AgentID(new ModelID(replicationID),
						"datasourceAgent")));
	}

	// @Override
	// public Serializable getKey()
	// {
	// return getClass().getCanonicalName();
	// }

	@SuppressWarnings("serial")
	private static Jongo getJongo()
	{
		if (jongo == null)
		{
			final DB db = mongo.getDB(DB_NAME);
			jongo = new Jongo(db, new JacksonMapper.Builder()
			// .enable(MapperFeature.AUTO_DETECT_GETTERS)
					.registerModule(new SimpleModule()
					{
						{
							addSerializer(ResourceSubtype.class,
									new ResourceSubtype.JsonSerializer());
							addSerializer(Interval.class,
									new com.fasterxml.jackson.datatype.joda.ser.IntervalSerializer());
							addKeyDeserializer(ResourceSubtype.class,
									new ResourceSubtype.JsonKeyDeserializer());
							addDeserializer(ResourceSubtype.class,
									new ResourceSubtype.JsonDeserializer());
							addDeserializer(SupplyType.class,
									new SupplyType.JsonDeserializer());
							addDeserializer(PersonRole.class,
									new PersonRole.JsonDeserializer());
							addDeserializer(AssemblyLineType.class,
									new AssemblyLineType.JsonDeserializer());
							addDeserializer(Interval.class,
									new com.fasterxml.jackson.datatype.joda.deser.IntervalDeserializer());
							
						}
					}).build());
		}
		return jongo;
	}

	/** @see Datasource#close() */
	@Override
	public void close()
	{
		if (mongo != null)
		{
			LOG.trace("Closing Mongo client...");
			// close client
			mongo.close();

			LOG.trace("Closing embedded Mongo (if any)...");
			// just in case an embedded Mongo was started...
			MongoUtil.shutdownEmbeddedMongo(mongo.getAddress().getPort());
		}

		if (this.emf != null)
		{
			LOG.trace("Closing EntityManagerFactory...");
			this.emf.close();

			LOG.trace("Closing embedded Mongo (if any)...");
			// just in case an embedded Mongo was started...
			MongoUtil.shutdownEmbeddedMongo(Integer.valueOf((String) this.emf
					.getProperties().get(MongoUtil.JPA_PORT_PROPERTY)));
		}
		LOG.trace("Closed!");
	}

	/** @see Datasource#getEntityManagerFactory() */
	@Override
	public EntityManagerFactory getEntityManagerFactory() throws Exception
	{
		LOG.trace("Constructing EntityManagerFactory...");
		this.emf = MongoUtil.getEntityManagerFactory(DB_JPA_UNIT, mongo
				.getAddress().getHost(), mongo.getAddress().getPort(), DB_NAME);
		LOG.trace("EntityManagerFactory ready!");
		return this.emf;
	}

	/**
	 * Helper method
	 * 
	 * @param object
	 */
	protected WriteResult save(final MongoCollection collection,
			final Object object)
	{
		WriteResult result = collection.save(object);

		if (result == null)
			throw new IllegalStateException(
					"Unexpected NULL WriteResult while saving " + object
							+ " to collection " + collection.getName());
		if (result.getError() != null)
			throw new IllegalStateException("NOT saved, error: "
					+ result.getError());

		return result;
	}

	/**
	 * Helper method
	 * 
	 * @param query
	 * @return
	 */
	protected Iterable<AssemblyLine> queryAssemblyLines(final String query)
	{
		LOG.trace("query assemblyLines: " + query);
		return getAssemblyLinesCollection().find(query).as(AssemblyLine.class);
	}

	/**
	 * Helper method
	 * 
	 * @param query
	 * @return
	 */
	protected Iterable<Process> querProcesses(final String query)
	{
		LOG.trace("query processes: " + query);
		return getProcesCollection().find(query).as(Process.class);
	}

	/**
	 * Helper method
	 * 
	 * @param query
	 * @return
	 */
	protected Iterable<Material> queryMaterials(final String query)
	{
		LOG.trace("query materials: " + query);
		return getMaterialsCollection().find(query).as(Material.class);
	}

	/**
	 * Helper method
	 * 
	 * @param query
	 * @return
	 */
	protected Iterable<Event> queryEvents(final String query)
	{
		LOG.trace("query events: " + query);
		return getEventsCollection().find(query).as(Event.class);
	}

	private static MongoCollection getCollection(final String key)
	{
		if (!collectionCache.containsKey(key))
			collectionCache.put(key, getJongo().getCollection(key));

		if (Thread.interrupted())
			LOG.info("Cleaned up thread interrupted status");
		return collectionCache.get(key);
	}

	/**
	 * Get the assemblyLines collection
	 * 
	 * @return collection
	 */
	protected MongoCollection getAssemblyLinesCollection()
	{
		return getCollection(MONGO_ASSEMBLY_LINES_COLLECTION);
	}
	
	/**
	 * Get the person events collection
	 * 
	 * @return collection
	 */
	protected MongoCollection getPersonUsageEventsCollection()
	{
		return getCollection(MONGO_PERSON_EVENTS_COLLECTION);
	}
	

	protected MongoCollection getPersonsCollection()
	{
		return getCollection(MONGO_PERSONS_COLLECTION);
	}

	/**
	 * Get the assemblyLines collection
	 * 
	 * @return collection
	 */
	protected MongoCollection getMaterialsCollection()
	{
		return getCollection(MONGO_MATERIALS_COLLECTION);
	}
	
	
	/**
	 * Get the processes collection
	 * 
	 * @return collection
	 */
	protected MongoCollection getProcesCollection()
	{
		return getCollection(MONGO_PROCESS_COLLECTION);
	}

	/**
	 * Get the gbXML collection
	 * 
	 * @return collection
	 */
	protected MongoCollection getSimulationFileCollection()
	{
		return getCollection(MONGO_SIMFILE_COLLECTION);
	}

	/**
	 * Get the events collection
	 * 
	 * @return collection
	 */
	protected MongoCollection getEventsCollection()
	{
		MongoCollection result = getCollection(MONGO_EVENTS_COLLECTION);
		if (!eventsAreIndexed)
		{
			result.ensureIndex(String.format("{\"%s.%s\":1}",
					EVENT_EXECUTION_TIME_FIELDNAME,
					EVENT_EXECUTION_TIME_VALUE_FIELDNAME));
			eventsAreIndexed = true;
		}
		return result;
	}

	/**
	 * Get the replications collection
	 * 
	 * @return
	 */
	protected MongoCollection getReplicationsCollection()
	{
		return getCollection(MONGO_REPLICATIONS_COLLECTION);
	}


	/** @see Datasource#save(AssemblyLine) */
	@Override
	public void save(final AssemblyLine assemblyLine)
	{
		// FIXME : Must be a REAL update instead of a remove/create
		MongoCollection collection = getAssemblyLinesCollection();
		collection.remove("{name: '" + assemblyLine.getName() + "', "
				+ REPLICATION_FOREIGN_ID_FIELDNAME + ": '" + replicationID
				+ "'}");
		assemblyLine.setReplicationID(replicationID);
		save(getAssemblyLinesCollection(), assemblyLine);
	}

	/** @see Datasource#findAssemblyLines() */
	@Override
	public Iterable<AssemblyLine> findAssemblyLines()
	{
		final String query = "{" + REPLICATION_FOREIGN_ID_FIELDNAME + ": '"
				+ replicationID + "'}";
		return queryAssemblyLines(query);
	}

	/** @see Datasource#findAssemblyLineByID(String) */
	@Override
	public AssemblyLine findAssemblyLineByID(final String assemblyLineID)
	{
		final String query = "{name: '" + assemblyLineID + "', "
				+ REPLICATION_FOREIGN_ID_FIELDNAME + ": '" + replicationID
				+ "'}";
		return getAssemblyLinesCollection().findOne(query).as(AssemblyLine.class);
	}

	@Override
	public Iterable<Person> findPersons()
	{
		final String query = "{" + REPLICATION_FOREIGN_ID_FIELDNAME + ": '"
				+ replicationID + "'}";
		return queryPersons(query);
	}

	private Iterable<Person> queryPersons(String query)
	{
		LOG.trace("query persons: " + query);
		return getPersonsCollection().find(query).as(Person.class);
	}

	@Override
	public Person findPersonByID(String personID)
	{
		final String query = "{name: '" + personID + "', "
				+ REPLICATION_FOREIGN_ID_FIELDNAME + ": '" + replicationID
				+ "'}";
		return getPersonsCollection().findOne(query).as(Person.class);
	}

	// @Override
	public void removePersons()
	{
		removeReplicationFromCollection(replicationID, getPersonsCollection());
	}

	// @Override
	public void removeAssemblyLines()
	{
		removeReplicationFromCollection(replicationID, getAssemblyLinesCollection());
	}

	// @Override
	public void removeMaterials()
	{
		removeReplicationFromCollection(replicationID, getMaterialsCollection());
	}

	// @Override
	public void removeProcesses()
	{
		removeReplicationFromCollection(replicationID, getProcesCollection());
	}

	// @Override
	public void removeEvents()
	{
		final MongoCollection collection = getEventsCollection();

		final String query = "{" + EVENT_REPLICATION_ID_FIELDNAME + ": '"
				+ replicationID + "'}";
		LOG.trace("events quering (remove): " + query);
		final WriteResult result = collection.remove(query);

		if (result == null)
			LOG.warn("Unexpected NULL WriteResult while removing events from replication "
					+ replicationID
					+ " from collection "
					+ collection.getName());
		else if (result.getError() != null)
			LOG.warn("NOT removed, error: " + result.getError());
	}

	private void removeReplicationFromCollection(final String replicationID,
			final MongoCollection collection)
	{
		final String query = "{" + REPLICATION_FOREIGN_ID_FIELDNAME + ": '"
				+ replicationID + "'}";
		LOG.trace(collection.getName() + " quering (remove): " + query);
		final WriteResult result = collection.remove(query);

		if (result == null)
			LOG.warn("Unexpected NULL WriteResult while removing "
					+ collection.getName() + " from replication "
					+ replicationID + " from collection "
					+ collection.getName());
		else if (result.getError() != null)
			LOG.warn("NOT removed, error: " + result.getError());
	}

	/** @see Datasource#save(Material) */
	@Override
	public void save(final Material material)
	{
		// FIXME : Must be a REAL update instead of a remove/create
		MongoCollection collection = getMaterialsCollection();
		collection.remove("{name: '" + material.getName() + "', "
				+ REPLICATION_FOREIGN_ID_FIELDNAME + ": '" + replicationID
				+ "'}");
		material.setReplicationID(replicationID);
		save(collection, material);
	}

	/** @see Datasource#findMaterials() */
	@Override
	public Iterable<Material> findMaterials()
	{
		final String query = String.format("{%s:'%s'}",
				REPLICATION_FOREIGN_ID_FIELDNAME, replicationID);
		return queryMaterials(query);
	}

	/** @see Datasource#findMaterialByID(String) */
	@Override
	public Material findMaterialByID(final String materialID)
	{
		final String query = String.format("{name:'%s', %s:'%s'}", materialID,
				REPLICATION_FOREIGN_ID_FIELDNAME, replicationID);
		return getMaterialsCollection().findOne(query).as(Material.class);
	}

	/** @see Datasource#save(Event) */
	@Override
	public void save(final Event event)
	{
		save(getEventsCollection(), event);
	}

	@Override
	public Iterable<MovementEvent> findMovementEvents()
	{
		final StringBuilder or = new StringBuilder();
		or.append(String.format("{%s:{%s:'%s'}}", EVENT_TYPE_FIELDNAME,
				EVENT_TYPE_NAME_FIELDNAME, EventType.ARIVE_AT_ASSEMBLY.getName()));
		or.append(',');
		or.append(String.format("{%s:{%s:'%s'}}", EVENT_TYPE_FIELDNAME,
				EVENT_TYPE_NAME_FIELDNAME, EventType.LEAVE_ASSEMBLY.getName()));
		final String query = String.format("{%s:'%s', $or:[%s]}",
				EVENT_REPLICATION_ID_FIELDNAME, replicationID, or.toString());
		LOG.info("Query: " + query);
		return getEventsCollection()
				.find(query)
				.sort(String.format("{%s.%s:1}",
						EVENT_EXECUTION_TIME_FIELDNAME,
						EVENT_EXECUTION_TIME_VALUE_FIELDNAME))
				.as(MovementEvent.class);
	}

	@Override
	public Iterable<MaterialEvent> findMaterialEvents()
	{
		final StringBuilder or = new StringBuilder();
		or.append(String.format("{%s:{%s:'%s'}}", EVENT_TYPE_FIELDNAME,
				EVENT_TYPE_NAME_FIELDNAME,
				EventType.START_USE_MATERIAL.getName()));
		or.append(',');
		or.append(String.format("{%s:{%s:'%s'}}", EVENT_TYPE_FIELDNAME,
				EVENT_TYPE_NAME_FIELDNAME,
				EventType.STOP_USE_MATERIAL.getName()));
		final String query = String.format("{%s:'%s', $or:[%s]}",
				EVENT_REPLICATION_ID_FIELDNAME, replicationID, or.toString());
		return getEventsCollection()
				.find(query)
				.sort(String.format("{%s.%s:1}",
						EVENT_EXECUTION_TIME_FIELDNAME,
						EVENT_EXECUTION_TIME_VALUE_FIELDNAME))
				.as(MaterialEvent.class);
	}

	@Override
	public Iterable<ActivityEvent> findActivityEvents()
	{
		final StringBuilder or = new StringBuilder();
		or.append(String.format("{%s:{%s:'%s'}}", EVENT_TYPE_FIELDNAME,
				EVENT_TYPE_NAME_FIELDNAME, EventType.START_ACTIVITY.getName()));
		or.append(',');
		or.append(String.format("{%s:{%s:'%s'}}", EVENT_TYPE_FIELDNAME,
				EVENT_TYPE_NAME_FIELDNAME, EventType.STOP_ACTIVITY.getName()));
		final String query = String.format("{%s:'%s', $or:[%s]}",
				EVENT_REPLICATION_ID_FIELDNAME, replicationID, or.toString());
		return getEventsCollection()
				.find(query)
				.sort(String.format("{%s.%s:1}",
						EVENT_EXECUTION_TIME_FIELDNAME,
						EVENT_EXECUTION_TIME_VALUE_FIELDNAME))
				.as(ActivityEvent.class);
	}

	/*-
	 * @see Datasource#findEvents(EventType)
	 * @Override 
	 * public Iterable<MovementEvent> findEvents(final EventType type)
	 *           { 
	 *           final String query = "{type: {name: '" + type.getValue() +
	 *           "'}}"; 
	 *           return queryEvents(query); 
	 *           }
	 */

	@Override
	public Iterable<Event> findEvents()
	{
		final String query = String.format("{%s:'%s'}",
				EVENT_REPLICATION_ID_FIELDNAME, replicationID);
		LOG.info("Getting events for " + query);
		return queryEvents(query);
	}

	/** @see Datasource#findEvents(EventType[]) */
	@Override
	public Iterable<Event> findEvents(final EventType... types)
	{
		final StringBuilder or = new StringBuilder();
		for (EventType _type : types)
		{
			if (or.length() != 0)
				or.append(',');
			or.append(String.format("{%s:{%s:'%s'}}", EVENT_TYPE_FIELDNAME,
					EVENT_TYPE_NAME_FIELDNAME, _type.getName()));
		}
		final String query = String.format("{%s:'%s', $or:[%s]}",
				EVENT_REPLICATION_ID_FIELDNAME, replicationID, or.toString());
		return getEventsCollection()
				.find(query)
				.sort(String.format("{%s:{%s:1}}",
						EVENT_EXECUTION_TIME_FIELDNAME,
						EVENT_EXECUTION_TIME_VALUE_FIELDNAME)).as(Event.class);
	}

	/** @see Datasource#findEvents(EventType[]) */
	@Override
	public Iterable<MovementEvent> findMovementEvents(final EventType... types)
	{
		final StringBuilder or = new StringBuilder();
		for (EventType _type : types)
		{
			if (or.length() != 0)
				or.append(',');
			or.append(String.format("{%s:{%s:'%s'}}", EVENT_TYPE_FIELDNAME,
					EVENT_TYPE_NAME_FIELDNAME, _type.getName()));
		}
		final String query = String.format("{%s:'%s', $or:[%s]}",
				EVENT_REPLICATION_ID_FIELDNAME, replicationID, or.toString());
		return getEventsCollection()
				.find(query)
				.sort(String.format("{%s.%s:1}",
						EVENT_EXECUTION_TIME_FIELDNAME,
						EVENT_EXECUTION_TIME_VALUE_FIELDNAME))
				.as(MovementEvent.class);
	}

	/** @see Datasource#findEvents(EventType[]) */
	@Override
	public Iterable<MaterialEvent> findMaterialEvents(
			final EventType... types)
	{
		final StringBuilder or = new StringBuilder();
		for (EventType _type : types)
		{
			if (or.length() != 0)
				or.append(',');
			or.append(String.format("{%s:{%s:'%s'}}", EVENT_TYPE_FIELDNAME,
					EVENT_TYPE_NAME_FIELDNAME, _type.getName()));
		}
		final String query = String.format("{%s:'%s', $or:[%s]}",
				EVENT_REPLICATION_ID_FIELDNAME, replicationID, or.toString());
		return getEventsCollection()
				.find(query)
				.sort(String.format("{%s.%s:1}",
						EVENT_EXECUTION_TIME_FIELDNAME,
						EVENT_EXECUTION_TIME_VALUE_FIELDNAME))
				.as(MaterialEvent.class);
	}

	
	/** @see Datasource#findEvents(EventType[]) */
	@Override
	public Iterable<ActivityEvent> findActivityEvents(final EventType... types)
	{
		final StringBuilder or = new StringBuilder();
		for (EventType _type : types)
		{
			if (or.length() != 0)
				or.append(',');
			or.append(String.format("{%s:{%s:'%s'}}", EVENT_TYPE_FIELDNAME,
					EVENT_TYPE_NAME_FIELDNAME, _type.getName()));
		}
		final String query = String.format("{%s:'%s', $or:[%s]}",
				EVENT_REPLICATION_ID_FIELDNAME, replicationID, or.toString());
		return getEventsCollection()
				.find(query)
				.sort(String.format("{%s.%s:1}",
						EVENT_EXECUTION_TIME_FIELDNAME,
						EVENT_EXECUTION_TIME_VALUE_FIELDNAME))
				.as(ActivityEvent.class);
	}

	/** @see Datasource#findReplications() */
	@Override
	public synchronized Iterable<Replication> findReplications()
	{
		LOG.trace("query replications: {}");
		return getReplicationsCollection().find().as(Replication.class);
	}

	/** @see Datasource#findReplication() */
	@Override
	public synchronized Replication findReplication()
	{
		final String query = "{" + REPLICATION_ID_FIELDNAME + ": '"
				+ this.replicationID + "'}";
		LOG.trace("query: " + query + " at "
				+ getReplicationsCollection().getDBCollection().getFullName());

		final Replication replication = getReplicationsCollection().findOne(
				query).as(Replication.class);

		return replication;
	}


	/** @see Datasource#findReplication(String) */
	// @Override
	// public SimulationFileDocument findSimulationFileDocument(String id)
	// {
	// MongoCollection collection = getSimulationFileCollection();
	//
	// final String query = "{" + CIM_ID_FIELDNAME + ": '" + id + "'}";
	// LOG.trace("query replications: " + query);
	//
	// SimulationFileDocument simFile = collection.findOne(query).as(
	// SimulationFileDocument.class);
	//
	// return simFile;
	// }

	/** @see Datasource#save(Replication) */
	@Override
	public synchronized void save(final Replication replication)
	{
		save(getReplicationsCollection(), replication);
	}

	// /** @see Datasource#save(Replication) */
	// @Override
	// public void save(final SimulationFileDocument simulationFileDocument)
	// {
	// save(getSimulationFileCollection(), simulationFileDocument);
	// }

	@Override
	public void save(Person person)
	{

		// FIXME : Must be a REAL update instead of a remove/create
		MongoCollection collection = getPersonsCollection();
		collection.remove("{name: '" + person.getName() + "', "
				+ REPLICATION_FOREIGN_ID_FIELDNAME + ": '" + replicationID
				+ "'}");
		person.setReplicationID(replicationID);
		save(collection, person);

	}

	/** @see Datasource#update(Replication) */
	@Override
	public synchronized void update(Replication replication)
	{
		MongoCollection collection = getReplicationsCollection();

		WriteResult result = collection.update(
				"{" + REPLICATION_ID_FIELDNAME + ": '" + replication.getId()
						+ "'}").with("{$set: #}", replication);

		if (result == null)
			LOG.warn("Unexpected NULL WriteResult while saving " + replication
					+ " to collection " + collection.getName());
		else if (result.getError() != null)
			LOG.warn("NOT updated, error: " + result.getError());
	}

	/** @see Datasource#remove(Replication) */
	@Override
	public void remove(final Replication replication)
	{
		removeReplication(replication.getId());
	}

	/** @see Datasource#remove() */
	@Override
	public void removeReplication()
	{
		removeReplication(this.replicationID);
		removeEvents();
		removeAssemblyLines();
		removeMaterials();
		removePersons();
		removeProcesses();
	}

	protected void removeReplication(final String id)
	{
		final String query = "{" + REPLICATION_ID_FIELDNAME + ": '" + id + "'}";
		LOG.trace("replications quering (remove): " + query);
		final WriteResult result = getReplicationsCollection().remove(query);

		if (result == null)
			LOG.warn("Unexpected NULL WriteResult while removing replication "
					+ id + " from collection "
					+ getReplicationsCollection().getName());
		else if (result.getError() != null)
			LOG.warn("NOT removed, error: " + result.getError());
	}


	/** @see Datasource#toString() */
	@Override
	public String toString()
	{
		return String.format("%s[]", getClass().getSimpleName());
	}

	@Override
	public Iterable<Process> findProcesses()
	{
		final String query = "{" + REPLICATION_FOREIGN_ID_FIELDNAME + ": '"
				+ replicationID + "'}";
		return querProcesses(query);
	}

	@Override
	public Process findProcessByID(String processID)
	{
		final String query = "{name: '" + processID + "', "
				+ REPLICATION_FOREIGN_ID_FIELDNAME + ": '" + replicationID
				+ "'}";
		return getProcesCollection().findOne(query).as(Process.class);
	}

	@Override
	public void save(Process process)
	{
		// FIXME : Must be a REAL update instead of a remove/create
		getProcesCollection().remove(
				"{name: '" + process.getName() + "', "
						+ REPLICATION_FOREIGN_ID_FIELDNAME + ": '"
						+ replicationID + "'}");
		process.setReplicationID(replicationID);
		save(getProcesCollection(), process);
	}

	

	/** @see eu.a4ee.db.Datasource#hasReplication(java.lang.String) */
	@Override
	public boolean hasReplication(String replicationID)
	{
		return this.replicationID != null
				&& this.replicationID.equals(replicationID);
	}
	
//
//	/** @see eu.a4ee.db.Datasource#save(eu.a4ee.model.bean.events.PersonUsageEvent) */
//	@Override
//	public void save(AssemblyLineUsageInterval<?,?> personUsageEvent)
//	{
//		save(getPersonUsageEventsCollection(), personUsageEvent);
//	}
//
//	/** @see eu.a4ee.db.Datasource#findPopulationGenerationResults() */
//	@Override
//	public Iterable<AssemblyLineUsageInterval> findPersonUsageEvents()
//	{
//		LOG.trace("query generations: {}");
//		return getPersonUsageEventsCollection().find().as(AssemblyLineUsageInterval.class);
//	}
//
//	/** @see eu.a4ee.db.Datasource#findPopulationGenerationResultsByCalibrationId(java.lang.String) */
//	@Override
//	public Iterable<AssemblyLineUsageInterval> findPersonUsageEventsByEventCollectionId(
//			String eventCollectionId)
//	{
//		String query = "{\""+EVENTS_COLLECTION_FOREIGN_ID+"\":\""+eventCollectionId+"\"}";
//		LOG.trace("query person usage events: "+query);
//		return getPersonUsageEventsCollection().find(query).as(AssemblyLineUsageInterval.class);
//	}
//	
//	/** @see eu.a4ee.db.Datasource#findTimelineTrainerEventsByEventCollectionId(java.lang.String) */
//	@Override
//	public Iterable<TimeLineTrainerEvent> findTimelineTrainerEventsByEventCollectionId(
//			String eventCollectionId)
//	{
////		String query = "{\""+EVENTS_COLLECTION_FOREIGN_ID+"\":\""+eventCollectionId+"\"}";
//		BasicDBObject query = 
//				new BasicDBObject(EVENTS_COLLECTION_FOREIGN_ID, eventCollectionId);
//		BasicDBObject limit = new BasicDBObject("notTrained", true)
//		.append("calibrations", true)
//		.append("eventPairId", true)
//		.append("eventCollectionId", true)
//		.append("interval",true)
//		.append("startEvent.executionTime.isoTime", true)
//		.append("@class", true);
//		LOG.trace("query usage events: "+query+" with projection: "+limit);
//		return getPersonUsageEventsCollection().find(query.toString())
//				.projection(limit.toString()).as(TimeLineTrainerEvent.class);
//	}
//	
//	/** @see Datasource#update(Replication) */
//	@Override
//	public synchronized void update(AssemblyLineUsageInterval personUsageEvent)
//	{
//		MongoCollection collection = getPersonUsageEventsCollection();
//
//		WriteResult result = collection.update(
//				"{" + EVENT_PAIR_ID_FIELDNAME + ": '" + personUsageEvent.getEventPairId()
//						+ "'}").with("{$set: #}", personUsageEvent);
//
//		if (result == null)
//			LOG.warn("Unexpected NULL WriteResult while saving " + personUsageEvent
//					+ " to collection " + collection.getName());
//		else if (result.getError() != null)
//			LOG.warn("NOT updated, error: " + result.getError());
//	}
//
//
//
//	/** @see eu.a4ee.db.Datasource#save(eu.a4ee.model.bean.PopulationGenerationResult) */
//	@Override
//	public void save(PopulationGenerationResult populationGenerationResult)
//	{
//		save(getGenerationsCollection(), populationGenerationResult);
//		
//	}
//
//	/** @see eu.a4ee.db.Datasource#findPopulationGenerationResults() */
//	@Override
//	public Iterable<PopulationGenerationResult> findPopulationGenerationResults()
//	{
//		LOG.trace("query generations: {}");
//		return getGenerationsCollection().find().as(PopulationGenerationResult.class);
//	}
//
//	/** @see eu.a4ee.db.Datasource#findPopulationGenerationResultsByCalibrationId(java.lang.String) */
//	@Override
//	public Iterable<PopulationGenerationResult> findPopulationGenerationResultsByCalibrationId(
//			String calibrationId)
//	{
//		String query = "{\""+CALIBRATION_FOREIGN_ID_FIELDNAME+"\":\""+calibrationId+"\"}";
//		LOG.trace("query generations: "+query);
//		return getGenerationsCollection().find(query).as(PopulationGenerationResult.class);
//	}


}
