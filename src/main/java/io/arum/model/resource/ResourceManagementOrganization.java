package io.arum.model.resource;

import io.arum.model.ARUMOrganization;
import io.arum.model.resource.assemblyline.AssemblyLineResourceManagementOrganization;
import io.arum.model.resource.person.PersonResourceManagementOrganization;
import io.arum.model.resource.supply.MaterialResourceManagementOrganization;
import io.asimov.agent.gui.SemanticAgentGui;
import io.asimov.agent.resource.ResourceManagementWorld;
import io.asimov.messaging.ASIMOVMessage;
import io.asimov.microservice.negotiation.ResourceAllocationResponder;
import io.asimov.microservice.negotiation.ResourceReadyListeningResource;
import io.asimov.microservice.negotiation.ResourceReadyNotification.ResourceReadyListener;
import io.asimov.model.ActivityParticipation.ActivityParticipant;
import io.asimov.reasoning.sl.SLParsableSerializable;
import io.coala.bind.Binder;
import io.coala.capability.embody.Percept;
import io.coala.capability.interact.ReceivingCapability;
import io.coala.capability.know.ReasoningCapability;
import io.coala.log.InjectLogger;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observer;

/**
 * {@link ResourceManagementOrganization}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class ResourceManagementOrganization<T extends ResourceManagementWorld<?>>
		extends ARUMOrganization<T> implements ResourceReadyListeningResource
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private SemanticAgentGui myGui;

	/** */
	@SuppressWarnings("unused")
	private ResourceAllocationResponder responder;

	/** */
	private ActivityParticipant participator;

	/** */
	private ResourceReadyListener resourceReadyListner;

	/** */
	@InjectLogger
	private Logger LOG;

	/**
	 * {@link ResourceManagementOrganization} constructor
	 * 
	 * @param binder
	 * @throws Exception
	 */
	@Inject
	public ResourceManagementOrganization(final Binder binder) throws Exception
	{
		super(binder);
		if (getProperty("gui").getBoolean(false)
				|| getProperty("resource.gui").getBoolean(false)
				|| getProperty(getID().getValue() + ".gui").getBoolean(false))
			try
			{
				if (!GraphicsEnvironment.isHeadless())
					this.myGui = new SemanticAgentGui(getBinder(), null, true,
							true);
			} catch (final HeadlessException e)
			{
				LOG.warn("No screen/keyboard/mouse available", e);
			}
	}

	@Override
	public void initialize() throws Exception
	{
		super.initialize();
		resourceReadyListner = getBinder().inject(ResourceReadyListener.class);
		final Observer<Percept> beliefObserver = new Observer<Percept>()
		{

			@Override
			public void onCompleted()
			{
			}

			@Override
			public void onError(final Throwable t)
			{
				t.printStackTrace();
				die();
			}

			@Override
			public void onNext(final Percept args)
			{
				final ReasoningCapability reasonerService = getBinder().inject(
						ReasoningCapability.class);

				reasonerService
						.addBeliefToKBase(reasonerService.toBelief(args));
			}
		};

		getBinder().inject(ReceivingCapability.class).getIncoming()
				.ofType(ASIMOVMessage.class)
				.subscribe(new Observer<ASIMOVMessage>()
				{

					@Override
					public void onCompleted()
					{
						LOG.info("No more messages to receive");
					}

					@Override
					public void onError(final Throwable t)
					{
						t.printStackTrace();
					}

					@Override
					public void onNext(ASIMOVMessage args)
					{
						if (args.content instanceof SLParsableSerializable)
						{
							getBinder().inject(ReasoningCapability.class)
									.addBeliefToKBase(
											getBinder().inject(
													ReasoningCapability.class)
													.toBelief(args.content));
						}
					}
				});

		if (this instanceof AssemblyLineResourceManagementOrganization
				|| this instanceof PersonResourceManagementOrganization
				|| this instanceof MaterialResourceManagementOrganization)
			responder = getBinder().inject(ResourceAllocationResponder.class);

		getWorld().perceive().subscribe(beliefObserver);

		participator = getBinder().inject(ActivityParticipant.class);

	}

	@Override
	public void finish() throws Exception
	{
		LOG.info("Exit with participator status:_" + participator.getStatus());
		if (this.myGui != null && this.myGui.isVisible())
			this.myGui.dispose();
	}

	/**
	 * @return the resourceReadyListner
	 */
	@Override
	public ResourceReadyListener getResourceReadyListener()
	{
		return this.resourceReadyListner;
	}
}
