package io.asimov.model;

import io.asimov.agent.scenario.Context;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.EventType;
import io.asimov.xml.SimulationFile.Simulations.SimulationCase;
import io.asimov.xml.TUseCase;
import io.coala.dsol.util.AbstractDsolModel;
import io.coala.dsol.util.DsolModel;
import io.coala.log.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;

/**
 * {@link ResourceTraceModel}
 * 
 * @date $Date: 2014-11-04 11:22:27 +0100 (di, 04 nov 2014) $
 * @version $Revision: 1107 $
 * @author <a href="mailto:rick@almende.org">Rick van Krevelen</a>
 */
public class ResourceTraceModel extends
		AbstractDsolModel<DEVSSimulatorInterface, ResourceTraceModel> implements
		ResourceTraceEventListener {

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private static final Logger LOG = LogUtil
			.getLogger(ResourceTraceModel.class);

	static {
		DateTimeZone.setDefault(DateTimeZone.UTC);
	}

	
	/** */
	private final Map<String, String> resourceActivities = Collections
			.synchronizedMap(new HashMap<String, String>());


	/** */
	private final Context context;

	/** */
	private final TUseCase simParams;


	/** */
	private boolean initialized = false;

	/** */
	private ResourceTraceEventProducer eventSource = null;

	/** */
	private final List<ResourceTraceEventListener> occEventListeners = new ArrayList<ResourceTraceEventListener>();


	/**
	 * @throws Exception
	 */
	public ResourceTraceModel(final String simulatorName,
			final Context context,
			final TUseCase simParams)
			throws Exception {
		super(simulatorName);

		this.context = context;
		this.simParams = simParams;
	}

	public Context getContext() {
		return this.context;
	}



	
	@Override
	public void onActivity(final ActivityEvent event) {
		for (String resource : event.getInvolvedResources())
		this.resourceActivities
				.put(resource, event.getType().getName()
						.equals(EventType.START_ACTIVITY) ? event.getActivity()
						: null);
	}

	@Override
	public void onTraceComplete() {
		// nothing to do
	}



	public String getResourceActivity(final String resourceID) {
		if (!this.resourceActivities.containsKey(resourceID))
			throw new NullPointerException("No current activity for person: "
					+ resourceID);
		return this.resourceActivities.get(resourceID);
	}


	/** @see DsolModel#onInitialize() */
	@Override
	public synchronized void onInitialize() throws Exception {
		if (this.initialized) {
			LOG.error("Already initialized!", new IllegalStateException());
			return;
		}

		LOG.trace("Initializing model...");


		this.initialized = true;

		LOG.trace("Model initialized!");
	}

	public void registerListener(final ResourceTraceEventListener listener) {
		synchronized (this.occEventListeners) {
			this.occEventListeners.add(listener);
			if (this.eventSource != null)
				this.eventSource.addListener(listener);
		}
	}

	public void setSource(final ResourceTraceEventProducer source) {
		if (source == null)
			throw new NullPointerException("Can't set an empty event source");
		synchronized (this.occEventListeners) {
			this.eventSource = source;
			this.eventSource.addListener(this);
			for (ResourceTraceEventListener listener : this.occEventListeners)
				this.eventSource.addListener(listener);
		}
	}

	/** */
	protected static final String UPDATE_EMISSION_RATE_METHOD_ID = "updateEmissionsRate";

	
	/** @return the event generator */
	public ResourceTraceEventProducer getEventSource() {
		return this.eventSource;
	}

	/** @return the processes */
	public TUseCase getSimulationParameters() {
		return this.simParams;
	}


	
	/**
	 * @return
	 * @throws JAXBException
	 */
	public static ResourceTraceModel fromXML(final String replicationID,
			final SimulationCase simCase, final TUseCase simParams)
			throws Exception {
		final Context context;
		if (simCase.getUsecase().getContext() == null) {
			context = (Context) new ASIMOVContext().toXML();
			simCase.getUsecase().setContext(context.toXML());
		} else
			context = (Context) simCase.getUsecase().getContext();

		return new ResourceTraceModel(replicationID, context, simParams);
	}

}
