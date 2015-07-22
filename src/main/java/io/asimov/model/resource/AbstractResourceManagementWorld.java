package io.asimov.model.resource;

import io.asimov.agent.resource.ResourceManagementWorld;
import io.asimov.db.Datasource;
import io.asimov.model.ASIMOVResourceDescriptor;
import io.asimov.model.AbstractASIMOVOrganizationtWorld;
import io.asimov.model.AbstractEmbodied;
import io.asimov.model.AbstractEmbodied.InitialASIMOVPercept;
import io.asimov.model.CoordinationUtil;
import io.asimov.model.Resource;
import io.asimov.model.ResourceRequirement;
import io.asimov.model.ResourceType;
import io.asimov.model.Time;
import io.asimov.model.TraceService;
import io.asimov.model.events.Event;
import io.asimov.model.events.EventType;
import io.asimov.model.sl.SL;
import io.asimov.model.sl.SLConvertible;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.capability.configure.ConfiguringCapability;
import io.coala.capability.embody.Percept;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.exception.CoalaException;
import io.coala.log.InjectLogger;
import io.coala.time.SimTime;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observable;
import rx.Observer;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

/**
 * {@link AbstractResourceManagementWorld}
 * 
 * @date $Date: 2014-09-18 20:49:54 +0200 (do, 18 sep 2014) $
 * @version $Revision: 1071 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 * 
 */
public abstract class AbstractResourceManagementWorld<E extends AbstractEmbodied<?>>
		extends AbstractASIMOVOrganizationtWorld implements
		ResourceManagementWorld<E>

{
	/** */
	private static final long serialVersionUID = 1L;

	private AgentID currentLocationRepr;

	private List<Number> coordinates;

	public static final String ON_SITE_TIME_OF_DAY_PROPERTY = "onSiteTimeOfDay";

	public static final String OFF_SITE_TIME_OF_DAY_PROPERTY = "offSiteTimeOfDay";

	public static final String DISABLE_WEEKENDS_PROPERTY = "disableWeekends";

	public static final String DISABLE_NON_WORKING_HOURS_PROPERTY = "disableNonWorkingHours";

	@InjectLogger
	private Logger LOG;

	/** FIXME memory-leak: replay everything for everyone, or publish only? */
	private Subject<Percept, Percept> subject = ReplaySubject.create();

	/** */
	protected String entityType = null;

	/** */
	protected E entity = null;

	/**
	 * {@link AbstractResourceManagementWorld} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected AbstractResourceManagementWorld(final Binder binder) {
		super(binder);
	}

	@SuppressWarnings("unchecked")
	public void initialize() throws Exception {
		final String entityID = getBinder().getID().getValue();
		
		this.entity = (E) getBinder().inject(Datasource.class)
				.findResourceDescriptorByID(entityID);
		this.entityType = ((ResourceDescriptor<?>)this.entity).getType();
		setCurrentLocation(getOwnerID());
		this.entity.setUnAvailable(false);
		getBinder().inject(Datasource.class).save((ASIMOVResourceDescriptor)this.entity);
	}

	/** @see ResourceManagementWorld#getEntity() */
	@Override
	public E getEntity() {
		if (this.entity == null) {
			// throw new NullPointerException(getID() +
			// " entity not initialized");
			// FIXME!!! Should be ensured by lifecycle!
			try {
				initialize();
			} catch (Exception e) {
				LOG.error("Entity was not properly initialized ", e);
			}
		}
		return this.entity;
	}

	/** @see ARUMOrganizationWorldView#getEntityType() */
	@Override
	public String getResourceType() {
		return this.entityType;
	}

	/** @see ResourceManagementWorld#perceive() */
	@Override
	public Observable<Percept> perceive() {
		for (InitialASIMOVPercept b : getInitialAgentKBBeliefSet(getEntity(),
				getBinder().getID()))
			subject.onNext(b);
		subject.onCompleted();
		return subject.asObservable();
	}

	/*
	 * Obtains the initial KnowledgeBase entries as a formula set for the agents
	 * that have a physical body.
	 * 
	 * @param agentID The String that contains the agent ID and model ID
	 * information to generate the agentID references from.
	 * 
	 * @return A set of Formula's to instantiate an agents believe base with.
	 */
	public Set<InitialASIMOVPercept> getInitialAgentKBBeliefSet(final E entity,
			final AgentID agentID) {
		Set<InitialASIMOVPercept> result = new LinkedHashSet<InitialASIMOVPercept>();
		Set<ResourceRequirement> resourceMatchPatterns = new HashSet<ResourceRequirement>();
		if (entity instanceof ASIMOVResourceDescriptor) {
			for (ResourceSubtype subType : ((ASIMOVResourceDescriptor) entity)
					.getTypes())
				resourceMatchPatterns
						.add(new ResourceRequirement().withResource(
								new Resource()
										.withName(agentID.toString())
										.withSubTypeID(subType)
										.withTypeID(
												new ResourceType()
														.withName(((ASIMOVResourceDescriptor) entity)
																.getType())),
								1, new Time().withMillisecond(0)));
		} else {
			LOG.error("Unsuported entity type: "
					+ entity.getClass().getCanonicalName());
		}
		for (ResourceRequirement resourceMatchPattern : resourceMatchPatterns)
			result.add(InitialASIMOVPercept.toBelief(SLConvertible.ASIMOV_PROPERTY_SET_FORMULA
					.instantiate().add(SLConvertible.sASIMOV_PROPERTY,
							SL.string(ResourceRequirement.TASK_RESOURCE))
					.add(
							SLConvertible.sASIMOV_KEY,
							SL.string(ResourceRequirement.TASK_RESOURCE_TERM_NAME))
					.add(SLConvertible.sASIMOV_VALUE,
							resourceMatchPattern.toSL())));

		// LOG.trace("Init "+baseAID.getLocalName()+" with: "+result);
		return result;
	}
	
	@Override
	public void debug(final String... line) {
		debug(true, line);
	}
	
	@Override
	public void debug(boolean includeStackTrace, final String... line) {
		try {
			if (getBinder().inject(ConfiguringCapability.class).getProperty("debugResources").getJSON(new ArrayList<String>()).contains(getOwnerID().getValue())) {
				String message = getBinder().inject(ReplicatingCapability.class).getTime().toString()+" for "+getOwnerID()+" DEBUG: ";
				for (String part : line) {
					message += part;
				}
				PrintWriter pw = new PrintWriter(System.out);
				pw.flush();
				pw.println(message);
				if (includeStackTrace) {
					final IllegalStateException e 
						= new IllegalStateException("NO ERROR but DEBUG INFO");
					e.printStackTrace(pw);
				}
				pw.flush();
			}
		} catch (CoalaException e) {
			LOG.error("Failed to read property",e);
		}
		
	}

	/** @see io.asimov.model.resource.CurrentLocationStateService#getCurrentLocation() */
	@Override
	public AgentID getCurrentLocation() {
		return this.currentLocationRepr;
	}

	/** @see io.asimov.model.resource.CurrentLocationStateService#setCurrentLocation(io.coala.agent.AgentID) */
	protected void setCurrentLocation(AgentID locationAgentID) {
		this.currentLocationRepr = locationAgentID;
		this.coordinates = null;
	}

	/**
	 * @see io.asimov.model.resource.CurrentLocationStateService#setCurrentLocation(io.coala.agent.AgentID,
	 *      java.util.List)
	 */
	protected void setCurrentLocation(AgentID locationAgentID,
			List<Number> coordinates) {
		this.currentLocationRepr = locationAgentID;
		this.coordinates = coordinates;
	}

	@SuppressWarnings("unchecked")
	protected <T extends Event<?>> void fireAndForget(final String processID,
			final String processInstanceID, final String activityName,
			final String activityInstanceId, final EventType eventType,
			final List<String> involvedResources, final Observer<T> publisher) {
		final SimTime now = getBinder().inject(ReplicatingCapability.class)
				.getTime();
		publisher.onNext((T) TraceService.getInstance(
				getOwnerID().getModelID().getValue()).saveEvent(
				getBinder().inject(Datasource.class), processID,
				processInstanceID, activityName, activityInstanceId,
				involvedResources, eventType, now));
	}

	/** @see io.asimov.model.resource.CurrentLocationStateService#getCurrentCoordinates() */
	@Override
	public List<Number> getCurrentCoordinates() {
		if (this.coordinates == null)
			return this.coordinates;
		return CoordinationUtil.getCoordinatesForNonMovingElement(getBinder()
				.inject(Datasource.class), getCurrentLocation());
	}

	@Override
	public boolean isAvailable() {
		return !getEntity().isUnAvailable();
	}

	@Override
	public void setAvailable() {
		getEntity().setUnAvailable(false);
		if (getEntity() instanceof ASIMOVResourceDescriptor) {
			getBinder().inject(Datasource.class).save(
					(ASIMOVResourceDescriptor) getEntity());
		} else {
			LOG.error("Unknown entity type: "
					+ getEntity().getClass().getCanonicalName());
		}

	}

	@Override
	public void setUnavailable() {
		getEntity().setUnAvailable(true);
		if (getEntity() instanceof ASIMOVResourceDescriptor) {
			getBinder().inject(Datasource.class).save(
					(ASIMOVResourceDescriptor) getEntity());
		} else {
			LOG.error("Unknown entity type: "
					+ getEntity().getClass().getCanonicalName());
		}
	}

}
