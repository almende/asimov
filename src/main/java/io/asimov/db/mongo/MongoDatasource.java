package io.asimov.db.mongo;

import io.asimov.agent.scenario.Replication;
import io.asimov.db.Datasource;
import io.asimov.model.ASIMOVResourceDescriptor;
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

	private static boolean eventsAreIndexed = false;

	// TODO get from @Entity(name=...) JPA annotations
	protected static String MONGO_SIMFILE_COLLECTION = "cim";
	protected static String MONGO_RESOURCE_DESCRIPTOR_COLLECTION = "resourceDescriptors";
	protected static String MONGO_PROCESS_COLLECTION = "processes";
	protected static String MONGO_EVENTS_COLLECTION = "events";
	protected static String MONGO_KPI_COLLECTION = "kpi";
	protected static String MONGO_SIMULATOR_COLLECTION = "simulator";
	protected static String MONGO_REPLICATIONS_COLLECTION = "replications";


	// TODO: remove hard-coded id field name, get from JPA annotations?
	protected static String REPLICATION_FOREIGN_ID_FIELDNAME = "replicationID";
	protected static String REPLICATION_ID_FIELDNAME = "id";
	protected static String CALIBRATION_ID_FIELDNAME = "id";
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
							addSerializer(Interval.class,
									new com.fasterxml.jackson.datatype.joda.ser.IntervalSerializer());
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
	protected Iterable<ASIMOVResourceDescriptor> queryResourceDescriptors(final String query)
	{
		LOG.trace("query resource descriptors: " + query);
		return getResourceDescriptorCollection().find(query).as(ASIMOVResourceDescriptor.class);
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
	protected MongoCollection getResourceDescriptorCollection()
	{
		return getCollection(MONGO_RESOURCE_DESCRIPTOR_COLLECTION);
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
	public void save(final ASIMOVResourceDescriptor resourceDescriptor)
	{
		// FIXME : Must be a REAL update instead of a remove/create
		MongoCollection collection = getResourceDescriptorCollection();
		collection.remove("{name: '" + resourceDescriptor.getName() + "', "
				+ REPLICATION_FOREIGN_ID_FIELDNAME + ": '" + replicationID
				+ "'}");
		resourceDescriptor.setReplicationID(replicationID);
		save(getResourceDescriptorCollection(), resourceDescriptor);
	}

	/** @see Datasource#findAssemblyLines() */
	@Override
	public Iterable<ASIMOVResourceDescriptor> findResourceDescriptors()
	{
		final String query = "{" + REPLICATION_FOREIGN_ID_FIELDNAME + ": '"
				+ replicationID + "'}";
		return queryResourceDescriptors(query);
	}

	/** @see Datasource#findAssemblyLineByID(String) */
	@Override
	public ASIMOVResourceDescriptor findResourceDescriptorByID(final String resourceDescriptorID)
	{
		final String query = "{name: '" + resourceDescriptorID + "', "
				+ REPLICATION_FOREIGN_ID_FIELDNAME + ": '" + replicationID
				+ "'}";
		return getResourceDescriptorCollection().findOne(query).as(ASIMOVResourceDescriptor.class);
	}

	@Override
	public void removeResourceDescriptors()
	{
		removeReplicationFromCollection(replicationID, getResourceDescriptorCollection());
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


	/** @see Datasource#save(Event) */
	@Override
	public void save(final Event event)
	{
		save(getEventsCollection(), event);
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
		removeResourceDescriptors();
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

	

	/** @see io.asimov.db.Datasource#hasReplication(java.lang.String) */
	@Override
	public boolean hasReplication(String replicationID)
	{
		return this.replicationID != null
				&& this.replicationID.equals(replicationID);
	}

	@Override
	public Iterable<ActivityEvent> findActivityEvents() {
		final String query = String.format("{%s:'%s'}",
				EVENT_REPLICATION_ID_FIELDNAME, replicationID);
		return getEventsCollection()
				.find(query)
				.sort(String.format("{%s.%s:1}",
						EVENT_EXECUTION_TIME_FIELDNAME,
						EVENT_EXECUTION_TIME_VALUE_FIELDNAME))
				.as(ActivityEvent.class);
	}

	
	

}
