package io.arum.model.resource;

import io.arum.model.AbstractARUMOrganizationtWorld;
import io.arum.model.events.PersonEvent;
import io.arum.model.resource.assemblyline.AssemblyLine;
import io.arum.model.resource.assemblyline.AssemblyLineType;
import io.arum.model.resource.person.Person;
import io.arum.model.resource.person.PersonRole;
import io.arum.model.resource.supply.Material;
import io.arum.model.resource.supply.SupplyType;
import io.asimov.agent.resource.ResourceManagementWorld;
import io.asimov.db.Datasource;
import io.asimov.model.AbstractEmbodied;
import io.asimov.model.AbstractEmbodied.InitialASIMOVPercept;
import io.asimov.model.CoordinationUtil;
import io.asimov.model.Resource;
import io.asimov.model.ResourceRequirement;
import io.asimov.model.Time;
import io.asimov.model.TraceService;
import io.asimov.model.events.EventType;
import io.asimov.model.sl.SLConvertible;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.capability.embody.Percept;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.factory.ClassUtil;
import io.coala.log.InjectLogger;
import io.coala.model.ModelComponentIDFactory;
import io.coala.time.SimTime;
import jade.semantics.lang.sl.tools.SL;

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
		extends AbstractARUMOrganizationtWorld implements
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
	protected ARUMResourceType entityType = null;

	/** */
	protected E entity = null;

	/**
	 * {@link AbstractResourceManagementWorld} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected AbstractResourceManagementWorld(final Binder binder)
	{
		super(binder);
	}

	@SuppressWarnings("unchecked")
	public void initialize() throws Exception
	{
		final Class<E> clazz = (Class<E>) ClassUtil.getTypeArguments(
				AbstractResourceManagementWorld.class, getClass()).get(0);
		final String entityID = getBinder().getID().getValue();
		if (Material.class.isAssignableFrom(clazz))
		{
			this.entityType = ARUMResourceType.MATERIAL;
			this.entity = (E) getBinder().inject(Datasource.class)
					.findMaterialByID(entityID);
			setCurrentLocation(getOwnerID());
		} else if (AssemblyLine.class.isAssignableFrom(clazz))
		{
			this.entityType = ARUMResourceType.ASSEMBLY_LINE;
			this.entity = (E) getBinder().inject(Datasource.class)
					.findAssemblyLineByID(entityID);
			setCurrentLocation(getOwnerID());
		} else if (Person.class.isAssignableFrom(clazz))
		{
			this.entityType = ARUMResourceType.PERSON;
			this.entity = (E) getBinder().inject(Datasource.class)
					.findPersonByID(entityID);
			setCurrentLocation(getBinder()
					.inject(ModelComponentIDFactory.class).createAgentID(
							"world"));
		} else
			throw new Exception("Unsupported entity type " + clazz.getName());
	}

	/** @see ResourceManagementWorld#getEntity() */
	@Override
	public E getEntity()
	{
		if (this.entity == null)
		{
			// throw new NullPointerException(getID() +
			// " entity not initialized");
			// FIXME!!! Should be ensured by lifecycle!
			try
			{
				initialize();
			} catch (Exception e)
			{
				LOG.error("Entity was not properly initialized ", e);
			}
		}
		return this.entity;
	}

	/** @see ARUMOrganizationWorldView#getEntityType() */
	@Override
	public ARUMResourceType getResourceType()
	{
		return this.entityType;
	}

	/** @see ResourceManagementWorld#perceive() */
	@Override
	public Observable<Percept> perceive()
	{
		for (InitialASIMOVPercept b : getInitialAgentKBBeliefSet(getEntity(),
				getBinder().getID()))
			subject.onNext(b);
		subject.onCompleted();
		return subject.asObservable();
	}

	/*
	 * Obtains the initial KnowledgeBase entries as a formula set for the agents that have a physical body.
	 * @param agentID The String that contains the agent ID and model ID information to generate the agentID references from.
	 * @return A set of Formula's to instantiate an agents believe base with.
	 */
	public Set<InitialASIMOVPercept> getInitialAgentKBBeliefSet(final E entity,
			final AgentID agentID)
	{
		Set<InitialASIMOVPercept> result = new LinkedHashSet<InitialASIMOVPercept>();
		Set<ResourceRequirement> resourceMatchPatterns = new HashSet<ResourceRequirement>();
		if (entity instanceof AssemblyLine)
		{
			for (AssemblyLineType type : ((AssemblyLine) entity).getTypes())
				resourceMatchPatterns.add(new ResourceRequirement().withResource(
					new Resource().withName(agentID.toString())
							.withSubTypeID(type)
							.withTypeID(AssemblyLine.class), 1,
					new Time().withMillisecond(0)));
		} else if (entity instanceof Material)
		{
			for (SupplyType type : ((Material) entity).getTypes())
				resourceMatchPatterns.add(new ResourceRequirement().withResource(
					new Resource().withName(agentID.toString())
							.withSubTypeID(type)
							.withTypeID(Material.class), 1,
					new Time().withMillisecond(0)));
		} else if (entity instanceof Person)
		{
			for (PersonRole type : ((Person) entity).getTypes())
				resourceMatchPatterns.add(new ResourceRequirement().withResource(
					new Resource().withName(agentID.toString())
							.withSubTypeID(type)
							.withTypeID(Person.class), 1,
					new Time().withMillisecond(0)));
		}
		for (ResourceRequirement resourceMatchPattern : resourceMatchPatterns)
			result.add(InitialASIMOVPercept.toBelief(SLConvertible.ASIMOV_PROPERTY_SET_FORMULA
					.instantiate(SLConvertible.sASIMOV_PROPERTY,
							SL.string(ResourceRequirement.TASK_RESOURCE))
					.instantiate(
							SLConvertible.sASIMOV_KEY,
							SL.string(ResourceRequirement.TASK_RESOURCE_TERM_NAME))
					.instantiate(SLConvertible.sASIMOV_VALUE,
							resourceMatchPattern.toSL())));
		
		// LOG.trace("Init "+baseAID.getLocalName()+" with: "+result);
		return result;
	}

	/** @see eu.a4ee.model.resource.CurrentLocationStateService#getCurrentLocation() */
	@Override
	public AgentID getCurrentLocation()
	{
		return this.currentLocationRepr;
	}

	/** @see eu.a4ee.model.resource.CurrentLocationStateService#setCurrentLocation(io.coala.agent.AgentID) */
	protected void setCurrentLocation(AgentID locationAgentID)
	{
		this.currentLocationRepr = locationAgentID;
		this.coordinates = null;
	}

	/**
	 * @see eu.a4ee.model.resource.CurrentLocationStateService#setCurrentLocation(io.coala.agent.AgentID,
	 *      java.util.List)
	 */
	protected void setCurrentLocation(AgentID locationAgentID,
			List<Number> coordinates)
	{
		this.currentLocationRepr = locationAgentID;
		this.coordinates = coordinates;
	}

	@SuppressWarnings("unchecked")
	protected <T extends PersonEvent<?>> void fireAndForget(
			final String processID, final String processInstanceID,
			final String activityName, final EventType eventType,
			final AgentID personID, final String ref1, final String beName,
			final Observer<T> publisher)
	{
		if (eventType.equals(EventType.ARIVE_AT_ASSEMBLY))
			this.setCurrentLocation(getBinder().inject(
					ModelComponentIDFactory.class).createAgentID(beName));
		final SimTime now = getBinder().inject(ReplicatingCapability.class)
				.getTime();
		publisher.onNext((T) TraceService.getInstance(
				personID.getModelID().getValue()).saveEvent(
				getBinder().inject(Datasource.class), processID,
				processInstanceID, activityName, personID, eventType, now,
				ref1, beName));
	}

	/** @see eu.a4ee.model.resource.CurrentLocationStateService#getCurrentCoordinates() */
	@Override
	public List<Number> getCurrentCoordinates()
	{
		if (this.coordinates == null)
			return this.coordinates;
		return CoordinationUtil.getCoordinatesForNonMovingElement(getBinder()
				.inject(Datasource.class), getCurrentLocation());
	}

}
