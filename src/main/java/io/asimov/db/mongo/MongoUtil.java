/* $Id: MongoUtil.java 1048 2014-09-01 09:53:05Z krevelen $
 * $URL: https://redmine.almende.com/svn/a4eesim/trunk/adapt4ee-db/src/main/java/eu/a4ee/db/mongo/MongoUtil.java $
 * 
 * Part of the EU project Adapt4EE, see http://www.adapt4ee.eu/
 * 
 * @license
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright (c) 2010-2013 Almende B.V. 
 */
package io.asimov.db.mongo;

import io.coala.log.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;

import org.apache.log4j.Logger;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.nosql.adapters.mongo.MongoConnectionSpec;
import org.eclipse.persistence.nosql.adapters.mongo.MongoPlatform;
import org.joda.time.DateTime;

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.ArtifactStoreBuilder;
import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.directories.IDirectory;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * {@link MongoUtil}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class MongoUtil
{
	/** */
	private static final Logger LOG = LogUtil.getLogger(MongoUtil.class);

	/** */
	private static final class MongodEntry
	{

		/** The start time */
		private final DateTime time = DateTime.now();

		/** The Mongo process executable */
		private final MongodExecutable executable;

		/** the Mongo process daemon */
		private final MongodProcess process;

		public MongodEntry(final MongodExecutable executable,
				final MongodProcess process)
		{
			this.executable = executable;
			this.process = process;
		}

	}

	/** */
	private static final Map<Integer, MongodEntry> DAEMONS = Collections
			.synchronizedMap(new HashMap<Integer, MongodEntry>());

	/**
	 * 
	 * @param port the port number on which to start an embedded mongo
	 * @throws IOException
	 */
	public static MongodProcess startEmbeddedMongo(final int port)
			throws Exception
	{
		LOG.trace("Starting embedded mongo...");

		final java.util.logging.Logger mdLog = LogUtil
				.getJavaLogger("de.flapdoodle.embed.mongo");
		mdLog.setLevel(Level.ALL);
		// configure Mongo logging
		final Level output = Level.FINEST, error = Level.SEVERE, console = Level.INFO;

		final ProcessOutput processOutput = new ProcessOutput(
				Processors.logTo(mdLog, output),
				Processors.logTo(mdLog, error),
				Processors.named("[console>]", Processors.logTo(mdLog, console)));

		final IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
				.defaultsWithLogger(Command.MongoD, mdLog)
				.processOutput(processOutput)
				.artifactStore(
						new ArtifactStoreBuilder()
								.defaults(Command.MongoD)
								.download(
										new DownloadConfigBuilder()
												.defaultsForCommand(Command.MongoD))
								.tempDir(new IDirectory()
								{

									@Override
									public File asFile()
									{
										final File dir = new File(
												"target/mongod");
										dir.mkdir();
										return dir;
									}
								})).build();

		final MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
		// MongodStarter runtime = MongodStarter.getDefaultInstance();

		final int actualPort = port == 0 ? Network.getFreeServerPort() : port;

		final MongodExecutable executable = runtime
				.prepare(new MongodConfig(Version.Main.PRODUCTION, actualPort,
						Network.localhostIsIPv6()));

		LOG.trace("Starting embedded Mongo daemon...");
		final MongodProcess process = executable.start();

		DAEMONS.put(Integer.valueOf(actualPort), new MongodEntry(executable,
				process));

		LOG.trace("Mongo daemon ready, running on port: "
				+ process.getConfig().net().getPort() + " (= " + actualPort
				+ ")");
		return process;
	}

	public static void shutdownEmbeddedMongo(final int port)
	{
		final MongodEntry daemon = DAEMONS.remove(Integer.valueOf(port));
		if (daemon == null)
		{
			LOG.trace("No (more) Mongo running on port " + port);
			return;
		}
		LOG.trace("Shut down embedded Mongo on port " + port + "...");
		if (daemon.process != null)
			daemon.process.stop();
		if (daemon.executable != null)
			daemon.executable.stop();
		LOG.trace("Mongo running on port " + port + " since " + daemon.time
				+ " now terminated!");
	}

	/** */
	private static MongoClient testMongo(final String host, final int port)
	{
		LOG.trace("Connecting to (embedded) Mongo at " + host + ":" + port);
		MongoClient client = null;
		try
		{
			client = new MongoClient(host, port);

			// authorize if in secure mode
			// boolean auth = db.authenticate("username",
			// "password".toCharArray());

			client.getDatabaseNames();
			return client;
		} catch (final Exception e)
		{
			LOG.warn("Could not connect to Mongo DB at " + host + ":" + port);
			if (client != null)
				client.close();
			return null;
		}
	}

	/**
	 * @param host
	 * @param port
	 * @return
	 * @throws Exception
	 */
	public static MongoClient connectMongo(final String host, final int port)
			throws Exception
	{
		// start database client
		MongoClient client = null;
		String actualHost = host;
		int actualPort = port;
		if (port == 0)
		{
			actualPort = startEmbeddedMongo(0).getConfig().net().getPort();
			actualHost = "localhost";
			LOG.trace("Embedded Mongo running on available port: " + actualPort);
		}
		client = testMongo(actualHost, actualPort);

		if (client == null)
			throw new Exception("Problem connecting to (embedded) Mongo at "
					+ actualHost + ":" + actualPort);

		LOG.trace("Connected to (embedded) Mongo at " + client.getAddress());

		return client;
	}

	public static EntityManagerFactory getEntityManagerFactory(
			final String jpaUnit, final Map<String, String> props)
	{
		LOG.trace("Loading JPA unit " + jpaUnit + " with properties: " + props);
		final PersistenceProviderResolver resolver = PersistenceProviderResolverHolder
				.getPersistenceProviderResolver();
		final List<PersistenceProvider> providers = resolver
				.getPersistenceProviders();
		LOG.trace("Resolved providers: " + providers);
		final EntityManagerFactory result = Persistence
				.createEntityManagerFactory(jpaUnit, props);

		LOG.trace("EclipseLink entity manager factory ready!");

		return result;
	}

	/** */
	private static final String JPA_PROPERTY_PREFIX = "eclipselink.nosql.property.";

	/** */
	public static final String JPA_HOST_PROPERTY = JPA_PROPERTY_PREFIX
			+ MongoConnectionSpec.HOST;

	/** */
	public static final String JPA_PORT_PROPERTY = JPA_PROPERTY_PREFIX
			+ MongoConnectionSpec.PORT;

	/** */
	public static final String JPA_DB_PROPERTY = JPA_PROPERTY_PREFIX
			+ MongoConnectionSpec.DB;

	/**
	 * @param jpaUnit
	 * @param host
	 * @param port
	 * @param db
	 * @return
	 * @throws Exception
	 */
	public static EntityManagerFactory getEntityManagerFactory(
			final String jpaUnit, final String host, final int port,
			final String db) throws Exception
	{
		// first test the specified location (or start embedded mongo instead)
		final MongoClient client = connectMongo(host, port);
		client.close();

		final Map<String, String> props = new HashMap<String, String>();

		// props.put(PersistenceUnitProperties.DDL_GENERATION,
		// PersistenceUnitProperties.DROP_AND_CREATE);
		props.put(PersistenceUnitProperties.LOGGING_LEVEL,
				Level.FINEST.getName());
		props.put(PersistenceUnitProperties.TARGET_DATABASE,
				MongoPlatform.class.getName());
		props.put(PersistenceUnitProperties.NOSQL_CONNECTION_SPEC,
				MongoConnectionSpec.class.getName());
		props.put(JPA_HOST_PROPERTY, client.getAddress().getHost());
		props.put(JPA_PORT_PROPERTY,
				Integer.toString(client.getAddress().getPort()));
		props.put(JPA_DB_PROPERTY, db);

		return getEntityManagerFactory(jpaUnit, props);
	}
}
