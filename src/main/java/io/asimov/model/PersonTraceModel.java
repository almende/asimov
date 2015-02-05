package io.asimov.model;

import io.arum.model.events.MaterialEvent;
import io.arum.model.events.MovementEvent;
import io.asimov.agent.scenario.Context;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.EventType;
import io.asimov.xml.SimulationFile.Simulations.SimulationCase;
import io.asimov.xml.SimulationFile.Simulations.SimulationCase.Roles;
import io.asimov.xml.TRole;
import io.asimov.xml.TTrainedTemplate;
import io.asimov.xml.TTrainedTemplate.RoleTemplate;
import io.coala.dsol.util.AbstractDsolModel;
import io.coala.dsol.util.DsolModel;
import io.coala.log.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

/**
 * {@link PersonTraceModel}
 * 
 * @date $Date: 2014-11-04 11:22:27 +0100 (di, 04 nov 2014) $
 * @version $Revision: 1107 $
 * @author <a href="mailto:rick@almende.org">Rick van Krevelen</a>
 */
public class PersonTraceModel extends
		AbstractDsolModel<DEVSSimulatorInterface, PersonTraceModel> implements
		PersonTraceEventListener {

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private static final Logger LOG = LogUtil
			.getLogger(PersonTraceModel.class);

	static {
		DateTimeZone.setDefault(DateTimeZone.UTC);
	}

	/** */
	private final Map<String, String> personLocations = Collections
			.synchronizedMap(new HashMap<String, String>());

	/** */
	private final Map<String, String> personActivities = Collections
			.synchronizedMap(new HashMap<String, String>());

	/** */
	private final Map<String, String> personGroups = Collections
			.synchronizedMap(new HashMap<String, String>());

	/** */
	private final Roles roles;

	/** */
	private final Context context;

	/** */
	private final TTrainedTemplate simParams;


	/** internal assumptions regarding emissions */
	private DistContinuous emissionKgCO2PerKWhDist = null;

	/** */
	private final Duration emissionUpdateRepeat = Duration.standardHours(3);

	/** */
	private boolean initialized = false;

	/** */
	private PersonTraceEventProducer eventSource = null;

	/** */
	private final List<PersonTraceEventListener> occEventListeners = new ArrayList<PersonTraceEventListener>();

	/** */
	private final Map<String, String> materialTypes = new TreeMap<>();

	/**
	 * @throws Exception
	 */
	public PersonTraceModel(final String simulatorName,
			final Context context, final Roles roles,
			final TTrainedTemplate simParams)
			throws Exception {
		super(simulatorName);

		this.context = context;
		this.roles = roles;
		this.simParams = simParams;
	}

	public Context getContext() {
		return this.context;
	}

	/**
	 * @return
	 */
	public Roles getRoles() {
		return this.roles;
	}

	/** @see PersonTraceEventListener#onMovement(MovementEvent) */
	@Override
	public void onMovement(final MovementEvent event) {
		this.personGroups.put(event.getPerson().getName(), event
				.getPerson().getType().getName());
		this.personLocations.put(event.getPerson().getName(), event
				.getType().getName().equals(EventType.ARIVE_AT_ASSEMBLY) ? event.getAssemblyLine()
				.getName() : null);
	}

	@Override
	public void onUsage(final MaterialEvent event) {
		// nothing to do
	}

	
	@Override
	public void onActivity(final ActivityEvent event) {
		this.personActivities
				.put(event.getPerson().getName(), event.getType().getName()
						.equals(EventType.START_ACTIVITY) ? event.getActivity()
						: null);
	}

	@Override
	public void onTraceComplete() {
		// nothing to do
	}

	public String getPersonLocation(final String personID) {
		if (!this.personLocations.containsKey(personID))
			throw new NullPointerException("No current location for person: "
					+ personID);
		return this.personLocations.get(personID);
	}

	public String getPersonActivity(final String personID) {
		if (!this.personActivities.containsKey(personID))
			throw new NullPointerException("No current activity for person: "
					+ personID);
		return this.personActivities.get(personID);
	}

	public String getPersonGroup(final String personID) {
		if (!this.personGroups.containsKey(personID))
			throw new NullPointerException("No group defined for person: "
					+ personID);
		return this.personGroups.get(personID);
	}

	/** @see DsolModel#onInitialize() */
	@Override
	public synchronized void onInitialize() throws Exception {
		if (this.initialized) {
			LOG.error("Already initialized!", new IllegalStateException());
			return;
		}

		LOG.trace("Initializing model...");

		this.emissionKgCO2PerKWhDist = new DistTriangular(getRNG(), 1.66, 5.0,
				8.33);

	

		// initiate emission rate update cycle
		getSimulator().scheduleEvent(getSimTime().doubleValue(), this, this,
				UPDATE_EMISSION_RATE_METHOD_ID, NO_ARGS);


		this.initialized = true;

		LOG.trace("Model initialized!");
	}

	public void registerListener(final PersonTraceEventListener listener) {
		synchronized (this.occEventListeners) {
			this.occEventListeners.add(listener);
			if (this.eventSource != null)
				this.eventSource.addListener(listener);
		}
	}

	public void setSource(final PersonTraceEventProducer source) {
		if (source == null)
			throw new NullPointerException("Can't set an empty event source");
		synchronized (this.occEventListeners) {
			this.eventSource = source;
			this.eventSource.addListener(this);
			for (PersonTraceEventListener listener : this.occEventListeners)
				this.eventSource.addListener(listener);
		}
	}

	/** */
	protected static final String UPDATE_EMISSION_RATE_METHOD_ID = "updateEmissionsRate";

	
	/** @return the event generator */
	public PersonTraceEventProducer getEventSource() {
		return this.eventSource;
	}

	/** @return the processes */
	public TTrainedTemplate getSimulationParameters() {
		return this.simParams;
	}


	private String roleIDMapping(final String groupID) {
		// [Project Manager, Junior Financial Assistant, Controller, Unknown,
		// Solution Designer, Trainee, Financial Assistant, Innovation Director,
		// Financail Director, Accountant]

		if (groupID.equals("Board Member"))
			return "Financial Assistant";
		if (groupID.equals("Procurement Specialist"))
			return "Solution Designer";
		if (groupID.equals("Treasury Controller"))
			return "Controller";
		if (groupID.equals("ISA Employee"))
			return "Unknown";
		if (groupID.equals("Director"))
			return "Financail Director"; // or "Innovation Director" ?
		return groupID;
	}

	private RoleTemplate getRoleTemplate(final String groupID) {
		final Set<String> roleNames = new HashSet<>();
		final String roleName = roleIDMapping(groupID);
		if (!roleName.equals(groupID))
			LOG.warn("Applied group name mapping: " + groupID + " -> "
					+ roleName);
		for (TRole role : this.roles.getRole()) {
			if (role.getRoleName().equalsIgnoreCase(roleName))
				for (RoleTemplate roleTpl : this.simParams.getRoleTemplate())
					if (roleTpl.getRoleRef().equalsIgnoreCase(role.getId()))
						return roleTpl;
			roleNames.add(role.getRoleName());
		}
		throw new NullPointerException(
				"Role template not defined in simulation training params: "
						+ groupID + " (" + roleName + "), mismatched: "
						+ roleNames);
	}

	/**
	 * @return
	 * @throws JAXBException
	 */
	public static PersonTraceModel fromXML(final String replicationID,
			final SimulationCase simCase, final TTrainedTemplate simParams)
			throws Exception {
		final Context context;
		if (simCase.getContext() == null) {
			context = new ARUMContext();
			simCase.setContext(context);
		} else
			context = (Context) simCase.getContext();

		return new PersonTraceModel(replicationID, context,
				simCase.getRoles(), simParams);
	}

}
