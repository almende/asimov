package io.asimov.model.usecase;

import io.asimov.agent.resource.impl.GenericResourceManagementOrganizationImpl;
import io.asimov.agent.scenario.Replication;
import io.asimov.agent.scenario.ScenarioManagementWorld;
import io.asimov.agent.scenario.SimStatus;
import io.asimov.db.Datasource;
import io.asimov.model.ASIMOVResourceDescriptor;
import io.asimov.model.AbstractASIMOVOrganizationtWorld;
import io.asimov.model.TraceService;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.Event;
import io.asimov.model.events.EventType;
import io.asimov.run.RunUseCase;
import io.asimov.util.extrapolator.EventExtrapolator;
import io.asimov.xml.SimulationFile;
import io.asimov.xml.TUseCase;
import io.coala.agent.Agent;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.capability.configure.ConfiguringCapability;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.capability.replicate.ReplicationConfig;
import io.coala.exception.CoalaException;
import io.coala.invoke.ProcedureCall;
import io.coala.invoke.Schedulable;
import io.coala.log.InjectLogger;
import io.coala.model.ModelComponentIDFactory;
import io.coala.random.RandomDistribution;
import io.coala.resource.FileUtil;
import io.coala.time.SimDuration;
import io.coala.time.SimTime;
import io.coala.time.SimTimeFactory;
import io.coala.time.TimeUnit;
import io.coala.time.Trigger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.measure.unit.SI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.jscience.physics.amount.Amount;

import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

/**
 * {@link ScenarioManagementWorldImpl}
 * 
 * @version $Revision: 1084 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class ScenarioManagementWorldImpl extends
		AbstractASIMOVOrganizationtWorld implements ScenarioManagementWorld {

	/** */
	private static final long serialVersionUID = 1L;

	public static final String ON_SITE_TIME_OF_DAY_PROPERTY = "onSiteTimeOfDay";

	public static final String OFF_SITE_TIME_OF_DAY_PROPERTY = "offSiteTimeOfDay";

	public static final String DISABLE_WEEKENDS_PROPERTY = "disableWeekends";

	public static final String DISABLE_NON_WORKING_HOURS_PROPERTY = "disableNonWorkingHours";

	public RandomDistribution<SimDuration> onSiteTimeOfDay;

	public RandomDistribution<SimDuration> offSiteTimeOfDay;
	

	private Subject<ActivityEvent, ActivityEvent> operational = PublishSubject
			.create();


	@InjectLogger
	private Logger LOG;

	/** */
	private final Set<AgentID> resourceIDs = Collections
			.synchronizedSet(new HashSet<AgentID>());

	/** */
	private final Set<ASIMOVResourceDescriptor> resourceDescriptors = Collections
			.synchronizedSet(new HashSet<ASIMOVResourceDescriptor>());

	/** */
	private final List<ResourceEvent> agents = new ArrayList<ResourceEvent>();

	/** */
	private Replication replication = null;

	/** */
	private UseCaseScenario scenario = null;

	private Subject<Integer, Integer> resourceHash = BehaviorSubject.create(0);

	/**
	 * {@link ScenarioManagementWorldImpl} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected ScenarioManagementWorldImpl(final Binder binder) {
		super(binder);
	}

	/** @see io.asimov.model.resource.PersonResourceManagementWorld#desiredSiteLeaveTime() */
	@Override
	public SimDuration onSiteDelay(final SimTime now) {
		return enterDuration(now);
	}

	public SimDuration enterDuration(final SimTime now) {
		if (getBinder().inject(ConfiguringCapability.class)
				.getProperty(DISABLE_NON_WORKING_HOURS_PROPERTY)
				.getBoolean(false))
			return SimDuration.ZERO;
		final DateTime nowDT = new DateTime(now.getIsoTime());
		final int day = nowDT.getDayOfWeek();
		final SimDuration millisOfDay = new SimDuration(nowDT.getMillisOfDay(),
				TimeUnit.MILLIS);
		final SimTime startOfDay = now.minus(millisOfDay);
		final SimDuration onSiteTimeOfDay = this.onSiteTimeOfDay.draw();
		final SimTime onSiteTime = startOfDay.plus(onSiteTimeOfDay);
		final SimTime offSiteTime = startOfDay.plus(this.offSiteTimeOfDay
				.draw());
		SimDuration onSiteDelta = onSiteTimeOfDay.minus(millisOfDay);

		final DateTime offset = new DateTime(getBinder().inject(Date.class));
		boolean weekends = !getBinder().inject(ConfiguringCapability.class)
				.getProperty(DISABLE_WEEKENDS_PROPERTY).getBoolean(false);
		final SimDuration result;
		if (now.isBefore(onSiteTime)) {
			if (weekends && day == DateTimeConstants.SATURDAY)
				result = onSiteDelta.plus(2, TimeUnit.DAYS);
			else if (weekends && day == DateTimeConstants.SUNDAY)
				result = onSiteDelta.plus(1, TimeUnit.DAYS);
			else
				result = onSiteDelta;
			LOG.info("delta [now: " + now.toDateTime(offset)
					+ "] [BEFORE] [day: " + day + "] [startOfDay "
					+ startOfDay.toDateTime(offset) + "] = " + result);
		} else if (now.isBefore(offSiteTime)) {
			if (weekends && day == DateTimeConstants.SATURDAY)
				result = onSiteDelta.plus(2, TimeUnit.DAYS);
			else if (weekends && day == DateTimeConstants.SUNDAY)
				result = onSiteDelta.plus(1, TimeUnit.DAYS);
			else
				result = SimDuration.ZERO;
			LOG.info("delta [now: " + now.toDateTime(offset)
					+ "] [DURING] [day: " + day + "] [startOfDay "
					+ startOfDay.toDateTime(offset) + "] = " + result);
		} else
		// if(now.isAfter( this.offSiteTimeOfDay.draw()))
		{
			if (weekends && day == DateTimeConstants.FRIDAY)
				result = onSiteDelta.plus(3, TimeUnit.DAYS);
			else if (weekends && day == DateTimeConstants.SATURDAY)
				result = onSiteDelta.plus(2, TimeUnit.DAYS);
			else
				result = onSiteDelta.plus(1, TimeUnit.DAYS);
			LOG.info("delta [now: " + now.toDateTime(offset)
					+ "] [AFTER] [day: " + day + "] [startOfDay "
					+ startOfDay.toDateTime(offset) + "] = " + result);
		}
		return result;
	}

	public <T extends Agent> ResourceEvent getResourceEventForNewAgent(
			final AgentID agentID, final Class<T> agentType,
			final SimTime eventTime) {
		return new ResourceEvent() {
			@Override
			public AgentID getResourceID() {
				return agentID;
			}

			@Override
			public Class<T> getResourceType() {
				return agentType;
			}

			@Override
			public ResourceEventType getEventType() {
				return ResourceEventType.ADDED;
			}

			@Override
			public SimTime getEventTime() {
				return eventTime;
			}

			@Override
			public int compareTo(ResourceEvent o) {
				if (getEventTime() == null)
					return -1;
				else if (o.getEventTime() != null)
					return getEventTime().toDate().compareTo(
							o.getEventTime().toDate());
				else
					return 0;
			}
		};
	}

	@Override
	public void initialize() throws Exception {
		final Datasource ds = getBinder().inject(Datasource.class);
		final ReplicationConfig cfg = getBinder().inject(
				ReplicationConfig.class);

		this.replication = ds.findReplication();

		File simFileURI = (RunUseCase.sourceFile != null) ? new File(
				RunUseCase.sourceFile.getAbsolutePath()) : new File(
				getProperty(SCENARIO_FILE_KEY).get());
		SimulationFile simFile = null;
		InputStream is = FileUtil.getFileAsInputStream(simFileURI);

		if (is == null)
			throw new NullPointerException("Simulationfile not found at URI: "
					+ simFileURI);
		try {
			JAXBContext jc = JAXBContext.newInstance(SimulationFile.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();

			simFile = (SimulationFile) unmarshaller.unmarshal(is);

		} finally {
			try {
				is.close();
			} catch (final IOException ignore) {
			}
		}

		if (simFile == null) {
			LOG.error("Was not able to read input, aborting inference.");
			return;
		}
		long offsetMS;
		if (this.replication == null) {
			LOG.warn("Replication not found in DB: "
					+ getID().getModelID().getValue()
					+ ", starting default scenario...");
			this.replication = UseCaseScenario.Util.saveDefaultReplication(
					getBinder(),
					simFileURI,
					Amount.valueOf(cfg.getDuration().toStandardSeconds()
							.getSeconds(), SI.SECOND));
		} else {
			offsetMS = this.replication.getStartDate().longValue();
			LOG.info("Got replication from DB: " + this.replication.toJSON());
			// update the ReplicationConfig settings from DB values
			final long durationMS = this.replication.getDurationMS()
					.longValue();
			if (durationMS > 0)
				cfg.setProperty(ReplicationConfig.DURATION_KEY, new Duration(
						durationMS).toPeriod().toString());
			else
				LOG.warn("Illegal duration specified in DB or GUI: "
						+ this.replication.getDurationMS());

			if (offsetMS > 0) {
				cfg.setProperty(ReplicationConfig.OFFSET_KEY, new DateTime(
						offsetMS).toString());
			} else
				LOG.warn("Illegal offset specified in DB or GUI: "
						+ this.replication.getStartDate());
		}
		offsetMS = cfg.getOffset().getMillis();
		setReplication(SimStatus.PREPARING, 0);

		TUseCase simCase = simFile.getSimulations().getSimulationCase().get(0)
				.getUsecase();

		try {
			this.scenario = UseCaseScenario.Builder.fromXML(this.replication,
					simCase,
					getBinder().inject(RandomDistribution.Factory.class),
					getBinder().inject(ReplicatingCapability.class).getRNG())
					.build();
		} catch (final Exception e) {
			LOG.error("Problem loading scenario", e);
		}
		if (this.scenario == null)
			throw new NullPointerException("No Scenario loaded!");

		final ModelComponentIDFactory agentIDFactory = getBinder().inject(
				ModelComponentIDFactory.class);
		// persist processes for process mgmt agents
		for (String processTypeID : this.scenario.getProcessTypeIDs()) {
			LOG.warn("Persisting process type: " + processTypeID);
			ds.save(this.scenario.getProcess(processTypeID));
		}

		if (replication.getStartDate() == null)
			replication.setStartDate(offsetMS);

		for (ASIMOVResourceDescriptor resourceDescriptor : this.scenario
				.getResourceDescriptors()) {
			this.resourceIDs.add(agentIDFactory
					.createAgentID(resourceDescriptor.getName()));

			this.resourceDescriptors.add(resourceDescriptor);
			if (resourceDescriptor.getAvailableFromTime() != null
					&& resourceDescriptor.getAvailableFromTime() > 0)
				resourceDescriptor.setUnAvailable(true);
			ds.save(resourceDescriptor);
			final ResourceEvent createAgentAction = getResourceEventForNewAgent(
					agentIDFactory.createAgentID(resourceDescriptor.getName()),
					GenericResourceManagementOrganizationImpl.class,
					getBinder()
							.inject(SimTimeFactory.class)
							.create((resourceDescriptor.getAvailableFromTime() == null) ? 0
									: resourceDescriptor.getAvailableFromTime(),
									TimeUnit.MILLIS));
			this.agents.add(createAgentAction);
		}

		final SimTime duration = getBinder().inject(SimTimeFactory.class)
				.create(replication.getDurationMS(), TimeUnit.MILLIS);
		LOG.info("Sim repl duration: " + getBinder().inject(Period.class));

		getBinder().inject(ReplicatingCapability.class).getTimeUpdates()
				.subscribe(new Observer<SimTime>() {
					@Override
					public void onNext(final SimTime time) {
						if (replication.getStatus().equals(SimStatus.PREPARING))
							setReplication(SimStatus.RUNNING, 0);
						// HOLD the simulation for debugging purposes
						Double autoPause;
						try {
							autoPause = getBinder()
									.inject(ConfiguringCapability.class)
									.getProperty("autopause").getDouble();
							if (autoPause != null && autoPause > 0)
								if (time.isAfter(getBinder().inject(
										SimTimeFactory.class).create(autoPause,
										TimeUnit.DAYS))) {
									LOG.info("Paused replication for debugging puposes");
									getBinder().inject(
											ReplicatingCapability.class)
											.pause();
								}
						} catch (CoalaException e1) {
							LOG.trace("Simulation may continue because no auto-pause property could be retrieved: "
									+ e1.getMessage());
						}
						// ------------------------------------------
						LOG.trace("Time changed: " + time);
						final Number progressPerc =
						// (time.getValue().doubleValue() == 0.0) ? 0 :
						time.dividedBy(duration).doubleValue() * 100;
						if (replication.getProgress().doubleValue() != progressPerc
								.doubleValue())
							setReplication(null, progressPerc);

						if (replication.getStatus().equals(SimStatus.RUNNING)
								&& (progressPerc.doubleValue() >= 100 || time
										.isOnOrAfter(SimTime.ZERO.plus(8,
												TimeUnit.DAYS)))) {
							setReplication(SimStatus.FINISHED, 100);
							try {
								if (time.isOnOrAfter(SimTime.ZERO.plus(8,
										TimeUnit.DAYS)))
									EventExtrapolator.extrapolator(replication
											.getId(), SimTime.ZERO.plus(1,
											TimeUnit.DAYS), new SimDuration(7,
											TimeUnit.DAYS), new SimDuration(
											replication.getDurationMS(),
											TimeUnit.MILLIS));
							} catch (Exception e) {
								LOG.error(
										"failed to extrapolate results: "
												+ e.getMessage(), e);
							}
							System.exit(0); // FIXME Must be more elegant
											// eventualy
						}
					}

					@Override
					public void onCompleted() {
					}

					@Override
					public void onError(final Throwable t) {
						t.printStackTrace();
					}
				});
		final RandomDistribution.Factory distFact = getBinder().inject(
				RandomDistribution.Factory.class);
		this.onSiteTimeOfDay = distFact.getConstant(new SimDuration(getBinder()
				.inject(ConfiguringCapability.class)
				.getProperty(ON_SITE_TIME_OF_DAY_PROPERTY).getNumber(9),
				TimeUnit.HOURS));
		this.offSiteTimeOfDay = distFact.getConstant(new SimDuration(
				getBinder().inject(ConfiguringCapability.class)
						.getProperty(OFF_SITE_TIME_OF_DAY_PROPERTY)
						.getNumber(18), TimeUnit.HOURS));
	}

	/** @see ScenarioManagementWorld#getReplication() */
	@Override
	public synchronized Replication getReplication() {
		return this.replication;
	}

	/** @see ScenarioManagementWorld#getCurrentAssemblyLineIDs() */
	@Override
	public Set<AgentID> getCurrentResourceIDs() {
		return Collections.unmodifiableSet(this.resourceIDs);
	}

	ReplaySubject<ScenarioManagementWorld.ResourceEvent> resourceEvents;

	private int currentResourceHash;

	/** @see ScenarioManagementWorld#onResources() */
	@Override
	public Observable<ResourceEvent> onResources() {
		if (resourceEvents == null)
			resourceEvents = ReplaySubject.create();
		for (ResourceEvent a : this.agents) {
			getBinder().inject(ReplicatingCapability.class).schedule(
					ProcedureCall.create(this, this, RESOURCE_AVAILABLE, a),
					Trigger.createAbsolute(a.getEventTime()));
			LOG.error("SCHEDULING: " + a.getResourceID() + " at "
					+ a.getEventTime());
		}
		return resourceEvents.asObservable();
	}

	public static final String RESOURCE_AVAILABLE = "RESOURCE_AVAILABLE";

	@Schedulable(RESOURCE_AVAILABLE)
	public void resourceAvailable(ResourceEvent e) {
		LOG.error("AVAILABLE: " + e.getResourceID() + " at " + e.getEventTime());
		resourceEvents.onNext(e);
		updateResourceStatusHash(getOwnerID().toString());
	}

	@Override
	public Iterable<ASIMOVResourceDescriptor> getResourceDescriptors() {
		return this.resourceDescriptors;
	}

	private static long processInstanceCount = 0;

	private static String PROCESS_INSTANCE_PREFIX = "processInstance";

	/** @see ScenarioManagementWorld#onProcessEvent() */
	@Override
	public Observable<ProcessEvent> onProcessEvent() {
		// TODO make dynamic using a ReplaySubject
		// FIXME: Added take(n) for testing purposes, needs to be removed for
		// production
		return Observable.from(this.scenario.getProcessTypeIDs()).map(
				new Func1<String, ProcessEvent>() {
					@Override
					public ProcessEvent call(final String processTypeID) {
						return new ProcessEvent() {
							@Override
							public String getProcessTypeID() {
								return processTypeID;
							}

							@Override
							public AgentID getProcessInstanceID() {
								return getBinder()
										.inject(ModelComponentIDFactory.class)
										.createAgentID(
												PROCESS_INSTANCE_PREFIX
														+ (processInstanceCount++));
							}

							@Override
							public ProcessEventType getEventType() {
								return ProcessEventType.REQUESTED;
							}
						};
					}
				});
	}
	

	public void performOperationChange(
			final EventType eventType) throws Exception {
		LOG.info("fire ASIMOV resource unavailability event!");
		if (eventType.equals(EventType.START_GLOBAL_OPERATIONAL_PERIOD) || eventType.equals(EventType.STOP_GLOBAL_OPERATIONAL_PERIOD))
			fireAndForget(eventType,
				this.operational);
		else
			LOG.error("Unsupported event type for availablility");
	}

	@SuppressWarnings("unchecked")
	protected <T extends Event<?>> void fireAndForget(
			final EventType eventType,
			final Observer<T> publisher) {
		final SimTime now = getBinder().inject(ReplicatingCapability.class)
				.getTime();
		publisher.onNext((T) TraceService.getInstance(
				getOwnerID().getModelID().getValue()).saveEvent(
				getBinder().inject(Datasource.class), null, null, null, null,
				Collections.EMPTY_LIST, eventType, now));
	}


	/** @see ScenarioManagementWorld#getProcessStartTimeOfDayDist(String) */
	@Override
	public RandomDistribution<SimDuration> getProcessStartTimeOfDayDist(
			final String processTypeID) {
		return this.scenario.getProcessStartDelayDistribution(processTypeID);
	}

	/** @see ScenarioManagementWorld#getResourceUnavailabilityDist(String) */
	@Override
	public RandomDistribution<SimDuration> getResourceUnavailabilityDist(
			final String resourceId) {
		return this.scenario.getResourceUnavailabilityDistribution(resourceId);
	}

	public synchronized void setReplication(final SimStatus status,
			final Number progressPerc) {
		if (status != null)
			this.replication.setStatus(status);
		if (progressPerc != null)
			this.replication.setProgress(progressPerc);
		getBinder().inject(Datasource.class).update(this.replication);
	}

	@Override
	public Observable<Integer> resourceStatusHash() {
		return this.resourceHash.asObservable();
	}

	@Override
	public void updateResourceStatusHash(String base) {
		final int hash = (base.hashCode() + "" + getTime().hashCode())
				.hashCode();
		if (this.currentResourceHash != hash) {
			this.resourceHash.onNext(hash);
			this.currentResourceHash = hash;
		}
	}

	@Override
	public int getCurrentResourceStatusHash() {
		return currentResourceHash;
	}

}
