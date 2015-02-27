package io.arum.model;

import io.asimov.messaging.ASIMOVMessage;
import io.coala.agent.AgentID;
import io.coala.agent.AgentStatusUpdate;
import io.coala.agent.BasicAgent;
import io.coala.agent.BasicAgentStatus;
import io.coala.bind.Binder;
import io.coala.capability.know.ReasoningCapability;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.factory.ClassUtil;
import io.coala.json.JsonUtil;
import io.coala.lifecycle.LifeCycleHooks;
import io.coala.lifecycle.MachineUtil;
import io.coala.log.InjectLogger;
import io.coala.message.Message;
import io.coala.model.ModelComponent;
import io.coala.resource.FileUtil;
import io.coala.time.Instant;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observer;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * {@link ARUMOrganization}
 * 
 * @date $Date: 2014-09-28 14:42:17 +0200 (zo, 28 sep 2014) $
 * @version $Revision: 1083 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 * 
 * @param <W>
 */
public class ARUMOrganization<W extends ARUMOrganizationWorld> extends
		BasicAgent implements ModelComponent<AgentID>
{

	/** */
	private static final long serialVersionUID = -8697203058486189448L;

	/** */
	@InjectLogger
	private Logger LOG;

	/** */
	private W world;

	/**
	 * {@link ARUMOrganization} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected ARUMOrganization(final Binder binder)
	{
		super(binder);
		super.getStatusHistory().subscribe(new Observer<BasicAgentStatus>(){

			@Override
			public void onCompleted() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onError(Throwable e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNext(BasicAgentStatus t) {
				System.err.println("STATUSUPDATE: "+getID()+" - "+t);
				System.err.flush();
			}});
	}

	private OutputStream jsonSniffer;

	// @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.PROPERTY,
	// property = "class")
	public static class Snif // implements Serializable
	{
		@JsonInclude
		protected Number timestamp;
		@JsonInclude
		protected String from;
		@JsonInclude
		protected String to;
		@JsonInclude
		protected String name;
		@JsonInclude
		protected String content;

		// protected Snif()
		// {
		// }

		public Snif(final Number timestamp, final String from, final String to,
				final String name, final String content)
		{
			this.timestamp = timestamp;
			this.from = from;
			this.to = to;
			this.name = name;
			this.content = content;
		}
	}

	/** @see BasicAgent#initialize() */
	@Override
	public void initialize() throws Exception
	{
//		getTime(); // FIXME Workaround for bug in coala
		final File file = new File("sniffer.json");
		file.delete();
		this.jsonSniffer = FileUtil.getFileAsOutputStream(file, true);
		// if (LOG == null)
		// {
		// LOG = LogUtil.getLogger(this);
		// // new NullPointerException("No LOG??").printStackTrace();
		// }
		getReceiver().getIncoming().subscribe(new Observer<Message<?>>()
		{
			@Override
			public void onCompleted()
			{
			}

			@Override
			public void onError(final Throwable e)
			{
				e.printStackTrace();
			}

			@Override
			public void onNext(final Message<?> t)
			{
				try
				{
					jsonSniffer.write((JsonUtil.toJSONString(t) + "\r\n")
							.getBytes());
				} catch (final IOException e)
				{
					onError(e);
				}
			}
		});

		getReceiver().getIncoming().ofType(ASIMOVMessage.class)
				.subscribe(new Observer<ASIMOVMessage>()
				{

					@Override
					public void onCompleted()
					{
						; // How do we now it is?
					}

					@Override
					public void onError(final Throwable t)
					{
						t.printStackTrace();
					}

					@Override
					public void onNext(final ASIMOVMessage msg)
					{
						final ReasoningCapability reasoner = getBinder()
								.inject(ReasoningCapability.class);
						// LOG.trace("Got a4ee msg: " + msg);
						reasoner.addBeliefToKBase(reasoner
								.toBelief(msg.content));
					}
				});
//		MachineUtil.setStatus(this, BasicAgentStatus., false);
//		((LifeCycleHooks)getReceiver()).activate();// FIXME Yet another workaround for yet another bug in coala
	}

	/** @return the organization's world */
	@SuppressWarnings("unchecked")
	public synchronized W getWorld()
	{
		if (this.world == null)
			this.world = getBinder().inject(
					(Class<? extends W>) ClassUtil.getTypeArguments(
							ARUMOrganization.class, getClass()).get(0));

		return this.world;
	}

	/** @see ModelComponent#getOwnerID() */
	@Override
	public AgentID getOwnerID()
	{
		return getBinder().getID();
	}

	/** @see ModelComponent#getTime() */
	@Override
	public Instant<?> getTime()
	{
		return getBinder().inject(ReplicatingCapability.class).getTime();
	}

}
