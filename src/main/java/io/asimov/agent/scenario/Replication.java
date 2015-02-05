package io.asimov.agent.scenario;

import io.asimov.model.AbstractEntity;
import io.coala.log.LogUtil;
import io.coala.time.TimeUnit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@link Replication}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:jos@almende.org">Jos</a>
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * @author <a href="mailto:suki@almende.org">Suki</a>
 * @author <a href="mailto:ludo@almende.org">Ludo</a>
 */
public class Replication extends AbstractEntity<Replication>
{
	private static final Logger LOG = LogUtil.getLogger(Replication.class);
	private static final long serialVersionUID = 1L;

	protected String id;
	protected String useCaseUri;
	protected String contextUri = null;
	protected Number startDate = 0;
	protected Number durationMS;
	protected Number occupancyNr;
	protected String calibrationId;
	protected SimStatus status = SimStatus.CREATED;
	protected Number progress = 0.0;

	public static final TimeUnit BASE_UNIT = TimeUnit.MILLIS;

	public Replication()
	{
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getUseCaseUri()
	{
		return useCaseUri;
	}

	public void setUseCaseUri(String useCaseUri)
	{
		this.useCaseUri = useCaseUri;
	}

	public String getContextUri()
	{
		return contextUri;
	}

	public void setContextUri(String contextUri)
	{
		this.contextUri = contextUri;
	}

	public Number getStartDate()
	{
		return startDate;
	}

	public void setStartDate(Number startDate)
	{
		this.startDate = startDate;
	}

	public Number getDurationMS()
	{
		return durationMS;
	}

	public void setDurationMS(Number durationMS)
	{
		this.durationMS = durationMS;
	}

	@Deprecated
	public Number getOccupancyNr()
	{
		return occupancyNr;
	}

	@Deprecated
	public void setOccupancyNr(Number occupancyNr)
	{
		this.occupancyNr = occupancyNr;
	}

	public String getCalibrationId()
	{
		return calibrationId;
	}

	public void setCalibrationId(String calibrationId)
	{
		this.calibrationId = calibrationId;
	}

	public SimStatus getStatus()
	{
		return status;
	}

	public void setStatus(SimStatus status)
	{
		this.status = status;
	}

	public Number getProgress()
	{
		return progress;
	}

	public void setProgress(Number progress)
	{
		this.progress = progress;
	}

	@JsonIgnore
	public Context getContext() throws JAXBException, FileNotFoundException
	{
		if (this.contextUri != null)
		{
			LOG.warn("Get resources from:'" + this.contextUri + "'");
			String path = null;
			if (this.contextUri.startsWith("file:"))
			{
				path = this.contextUri.replaceFirst("file:(//)?", "");
			}
			if (path != null)
			{
				// Local file
				final File file = new File(path);
				InputStream is = null;
				try
				{
					is = new FileInputStream(file);
					return (Context) ContextUtil.getContextUnmarshaller().unmarshal(is);
				} finally
				{
					if (is != null)
						try
						{
							is.close();
						} catch (final IOException ignore)
						{
							// ignore
						}
				}
			}
		}
		return null;
	}

	private static Integer seqNo = 0;

	@JsonIgnore
	public int getNewSeqNo()
	{
		synchronized (seqNo)
		{
			seqNo += 1;
			if (seqNo > 999)
			{
				seqNo = 0;
			}
			return seqNo;
		}
	}

	private Replication(Builder builder)
	{
		super();
		this.id = builder.id;
		this.useCaseUri = builder.projectId;
		this.contextUri = builder.contextUri;
		this.startDate = builder.startDate;
		this.durationMS = builder.durationMS;
		this.occupancyNr = builder.occupancyNr;
		this.calibrationId = builder.calibrationId;
		this.status = builder.status;
		this.progress = builder.progress;
	}

	public static class Builder
	{
		private String id;
		private String projectId;
		private String contextUri;
		private Number startDate;
		private Number durationMS;
		private Number occupancyNr;
		private String calibrationId;
		private SimStatus status;
		private Number progress;

		public Builder withId(String id)
		{
			this.id = id;
			return this;
		}

		public Builder withProjectId(String projectId)
		{
			this.projectId = projectId;
			return this;
		}

		public Builder withContextUri(String contextUri)
		{
			this.contextUri = contextUri;
			return this;
		}

		public Builder withStartDate(Number startDate)
		{
			this.startDate = startDate;
			return this;
		}

		public Builder withDurationMS(Number durationMS)
		{
			this.durationMS = durationMS;
			return this;
		}

		public Builder withOccupancyNr(Number occupancyNr)
		{
			this.occupancyNr = occupancyNr;
			return this;
		}

		public Builder withCalibrationId(String calibrationId)
		{
			this.calibrationId = calibrationId;
			return this;
		}

		public Builder withStatus(SimStatus status)
		{
			this.status = status;
			return this;
		}

		public Builder withProgress(Number progress)
		{
			this.progress = progress;
			return this;
		}

		public Replication build()
		{
			return new Replication(this);
		}
	}
}