package io.asimov.model;

import io.asimov.messaging.ASIMOVMessage;
import io.coala.agent.AgentID;
import io.coala.agent.BasicAgent;
import io.coala.agent.BasicAgentStatus;
import io.coala.bind.Binder;
import io.coala.capability.configure.ConfiguringCapability;
import io.coala.capability.know.ReasoningCapability;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.factory.ClassUtil;
import io.coala.invoke.ProcedureCall;
import io.coala.invoke.Schedulable;
import io.coala.json.JsonUtil;
import io.coala.log.InjectLogger;
import io.coala.message.Message;
import io.coala.model.ModelComponent;
import io.coala.resource.FileUtil;
import io.coala.time.Instant;
import io.coala.time.TimeUnit;
import io.coala.time.Trigger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observer;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * {@link ASIMOVOrganization}
 * 
 * @date $Date: 2014-09-28 14:42:17 +0200 (zo, 28 sep 2014) $
 * @version $Revision: 1083 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 * 
 * @param <W>
 */
public class ASIMOVOrganization<W extends ASIMOVOrganizationWorld> extends
		BasicAgent implements ModelComponent<AgentID>
{

	/** */
	private static final long serialVersionUID = -8697203058486189448L;

	/** */
	@InjectLogger
	private Logger LOG;

	/** */
	private W world;
	
	public static long agentCount = 0L;
	public static long messageCount = 0L;
	public static long reasoningCount = 0L;

	/**
	 * {@link ASIMOVOrganization} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected ASIMOVOrganization(final Binder binder)
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

	private static OutputStream jsonSniffer = null;

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
	
	public static final String REQUEST_DESTROY = "REQUEST_DESTROY";
	
	@Schedulable(REQUEST_DESTROY)
	public void destroy(){
		try {
			getFinalizer().destroy();
		} catch (Exception e) {
			LOG.error("Failed to destroy asimov agent",e);
		}
	}

	/** @see BasicAgent#initialize() */
	@Override
	public void initialize() throws Exception
	{
//		getTime(); // FIXME Workaround for bug in coala
		agentCount++;
		if (jsonSniffer == null && Boolean.parseBoolean(getBinder().inject(ConfiguringCapability.class).getProperty("enableSniffer").get("false"))) {
			final File file = new File("sniffer.json");
			file.delete();
			
			jsonSniffer = FileUtil.getFileAsOutputStream(file, true);
		}
		// if (LOG == null)
		// {
		// LOG = LogUtil.getLogger(this);
		// // new NullPointerException("No LOG??").printStackTrace();
		// }
		final boolean useSniffer = Boolean.parseBoolean(getBinder().inject(ConfiguringCapability.class).getProperty("enableSniffer").get("false"));
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
					messageCount++;
					if (useSniffer)
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
						// LOG.trace("Got ASIMOV msg: " + msg);
						if (msg.content.equals(REQUEST_DESTROY))
							getScheduler().schedule(
									ProcedureCall.create(ASIMOVOrganization.this, ASIMOVOrganization.this, REQUEST_DESTROY),
									Trigger.createAbsolute(getBinder().inject(ReplicatingCapability.class).getTime().plus(1,TimeUnit.MINUTES)));
						else	
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
							ASIMOVOrganization.class, getClass()).get(0));

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
