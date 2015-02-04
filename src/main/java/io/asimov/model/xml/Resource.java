package io.asimov.model.xml;

import io.coala.exception.CoalaException;
import io.coala.exception.CoalaExceptionFactory;
import io.coala.log.LogUtil;
import io.coala.resource.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

public class Resource
{

	/** */
	private static final Logger LOG = LogUtil.getLogger(Resource.class);

	/** */
	private final ResourceType type;

	/** */
	private final String path;

	protected Resource(final ResourceType type, final String path)
	{
		this.type = type;
		this.path = path;
		LOG.info("New resource type " + type + " with path: "
				+ (path == null ? "n/a" : Arrays.asList(path)));
	}

	public static Resource of(final String file)
	{
		return of(new File(file));
	}

	public static Resource of(final File file)
	{
		if (file.exists())
			return new Resource(ResourceType.FILE, file.toURI().toASCIIString());
		final String classPath = file.getPath();
		final URL classPathURL = Thread.currentThread().getContextClassLoader()
				.getResource(classPath);
		if (classPathURL != null)
			try
			{
				return new Resource(ResourceType.FILE, classPathURL.toURI()
						.toASCIIString());
			} catch (final URISyntaxException e)
			{
				throw new IllegalStateException("File not found: "
						+ classPathURL, e);
			}
		throw new IllegalStateException("File not found: " + file);
	}

	public static Resource of(final URI uri)
	{
		return new Resource(ResourceType.URI, uri.toASCIIString());
	}

	// "SELECT * FROM nation WHERE n_name >= 'C';"
	public static Resource of(final Class<?> driverType, final URI dbURL,
			final String sql)
	{
		// driverType just had to be loaded by {@link ClassLoader}
		/* FIXME
		try
		{
			final String[] userInfo = dbURL.getUserInfo().split(":");
			final Connection con = DriverManager.getConnection(
					dbURL.toASCIIString(), userInfo[0], userInfo[1]);
			final Statement stmt = con.createStatement();
			final ResultSet rst = stmt.executeQuery(sql);
			
		} catch (final SQLException e)
		{
			e.printStackTrace();
		}*/
		return new Resource(ResourceType.JDBC, dbURL.toASCIIString());
	}

	public InputStream asInputStream() throws CoalaException
	{
		switch (this.type)
		{
		case FILE:
			return FileUtil
					.getFileAsInputStream(new File(URI.create(this.path)));
		case URI:
			if (this.path == null)
				throw CoalaExceptionFactory.VALUE_NOT_SET.create("path");

			if (this.path.isEmpty())
				throw CoalaExceptionFactory.VALUE_NOT_ALLOWED.create("path",
						path);

			final List<Object> results = new ArrayList<>(1);
			final ResponseHandler<Void> rh = new ResponseHandler<Void>()
			{
				@Override
				public Void handleResponse(final HttpResponse response)
						throws ClientProtocolException, IOException
				{
					final HttpEntity entity = response.getEntity();
					if (entity == null)
						results.add(new ClientProtocolException(
								"Response contains no content"));
					else
						results.add(entity.getContent());
					synchronized (results)
					{
						results.notifyAll();
					}
					return null;
				}
			};
			try
			{
				HttpClients.createDefault().execute(new HttpGet(this.path), rh);
				while (results.isEmpty())
					LOG.info("Awaiting response from " + this.path);
				synchronized (results)
				{
					try
					{
						results.wait(1000L);
					} catch (final InterruptedException ignore)
					{
						// empty
					}
				}
				if (results.get(0) instanceof Throwable)
					throw (Throwable) results.get(0);
				else if (results.get(0) instanceof InputStream)
					return (InputStream) results.get(0);
				throw new IllegalStateException("Unknown response type: "
						+ results.get(0).getClass());
			} catch (final Throwable e)
			{
				throw CoalaExceptionFactory.UNMARSHAL_FAILED.create(e);
			}

		default:
		case JDBC:
			throw CoalaExceptionFactory.VALUE_NOT_ALLOWED.create("type",
					ResourceType.JDBC);
		}
	}

	/**
	 * @return
	 */
	public URI getURI()
	{
		return URI.create(this.path);
	}

	/**
	 * @return
	 */
	public boolean isFile()
	{
		return this.type == ResourceType.FILE;
	}
}