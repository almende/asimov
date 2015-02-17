package io.arum.model.resource.person.impl;

import io.arum.model.events.MovementEvent;
import io.arum.model.resource.ARUMResourceType;
import io.arum.model.resource.AbstractResourceManagementWorld;
import io.arum.model.resource.person.Person;
import io.arum.model.resource.person.PersonResourceManagementWorld;
import io.asimov.agent.process.NonSkeletonActivityCapability;
import io.asimov.db.Datasource;
import io.asimov.model.Body;
import io.asimov.model.CoordinationUtil;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.EventType;
import io.coala.bind.Binder;
import io.coala.capability.configure.ConfiguringCapability;
import io.coala.log.InjectLogger;
import io.coala.model.ModelComponentIDFactory;
import io.coala.random.RandomDistribution;
import io.coala.time.SimDuration;
import io.coala.time.SimTime;
import io.coala.time.TimeUnit;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * {@link PersonResourceManagementWorldImpl}
 * 
 * @version $Revision: 1074 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public class PersonResourceManagementWorldImpl extends
		AbstractResourceManagementWorld<Person> implements
		PersonResourceManagementWorld
{

	/** */
	private static final long serialVersionUID = 1L;

	@InjectLogger
	private Logger LOG;

	/** */
	private Subject<MovementEvent, MovementEvent> movements = PublishSubject
			.create();

	/** */
	private Subject<ActivityEvent, ActivityEvent> activity = PublishSubject
			.create();

	
	
	private boolean onSite = false;

	public RandomDistribution<SimDuration> onSiteTimeOfDay;

	public RandomDistribution<SimDuration> offSiteTimeOfDay;
	
	private NonSkeletonActivityCapability nonSkeletonActivityCapability;

	public SimDuration enterDuration(final SimTime now)
	{
		if (getBinder().inject(ConfiguringCapability.class)
				.getProperty(DISABLE_NON_WORKING_HOURS_PROPERTY).getBoolean(false))
			return SimDuration.ZERO;
		final DateTime nowDT = new DateTime(now.getIsoTime());
		final int day = nowDT.getDayOfWeek();
		final SimDuration millisOfDay = new SimDuration(nowDT.getMillisOfDay(),
				TimeUnit.MILLIS);
		final SimTime startOfDay = now.minus(millisOfDay);
		final SimDuration onSiteTimeOfDay = this.onSiteTimeOfDay.draw();
		final SimTime onSiteTime = startOfDay.plus(onSiteTimeOfDay);
		final SimTime offSiteTime = startOfDay.plus(this.offSiteTimeOfDay.draw());
		SimDuration onSiteDelta = onSiteTimeOfDay.minus(millisOfDay);
		
		final DateTime offset = new DateTime(getBinder().inject(Date.class));
		boolean weekends = !getBinder().inject(ConfiguringCapability.class)
				.getProperty(DISABLE_WEEKENDS_PROPERTY).getBoolean(false);
		final SimDuration result;
		if (now.isBefore(onSiteTime))
		{
			if (weekends && day == DateTimeConstants.SATURDAY)
				result = onSiteDelta.plus(2, TimeUnit.DAYS);
			else if (weekends && day == DateTimeConstants.SUNDAY)
				result = onSiteDelta.plus(1, TimeUnit.DAYS);
			else result = onSiteDelta;
			LOG.info("delta [now: "+now.toDateTime(offset)+"] [BEFORE] [day: "+day+"] [startOfDay "+startOfDay.toDateTime(offset)+"] = "+result);
		} else if (now.isBefore(offSiteTime))
		{
			if (weekends && day == DateTimeConstants.SATURDAY)
				result = onSiteDelta.plus(2, TimeUnit.DAYS);
			else if (weekends && day == DateTimeConstants.SUNDAY)
				result = onSiteDelta.plus(1, TimeUnit.DAYS);
			else
				result = SimDuration.ZERO;
			LOG.info("delta [now: "+now.toDateTime(offset)+"] [DURING] [day: "+day+"] [startOfDay "+startOfDay.toDateTime(offset)+"] = "+result);
		} else
		// if(now.isAfter( this.offSiteTimeOfDay.draw()))
		{
			if (weekends && day == DateTimeConstants.FRIDAY)
				result = onSiteDelta.plus(3, TimeUnit.DAYS);
			else if (weekends && day == DateTimeConstants.SATURDAY)
				result = onSiteDelta.plus(2, TimeUnit.DAYS);
			else
			result = onSiteDelta.plus(1, TimeUnit.DAYS);
			LOG.info("delta [now: "+now.toDateTime(offset)+"] [AFTER] [day: "+day+"] [startOfDay "+startOfDay.toDateTime(offset)+"] = "+result);
		}
		return result;
	}

	private static final Map<String, Integer> assemblyLineOccupancy = new HashMap<String, Integer>();

	

	/**
	 * {@link PersonResourceManagementWorldImpl} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected PersonResourceManagementWorldImpl(final Binder binder)
	{
		super(binder);
	}
	

	@Override
	public void initialize() throws Exception
	{
		super.initialize();
		this.entityType = ARUMResourceType.PERSON;
		this.entity = getBinder().inject(Datasource.class).findPersonByID(
				getOwnerID().getValue());

		// this.onSiteDuration = CIMScenario.Util.getOccupancyOnSiteDist(
		// Collections.singleton(new TDistribution()), // FIXME Should use real
		// distribution here
		// getBinder().inject(RandomDistribution.Factory.class),
		// getBinder().inject(ReplicatingCapability.class).getRNG(),
		// new SimDuration(24, TimeUnit.HOURS)).draw();
		// this.offSiteDuration = CIMScenario.Util.getOccupancyOffSiteDist(
		// Collections.singleton(new TDistribution()), // FIXME Should use real
		// distribution here
		// getBinder().inject(RandomDistribution.Factory.class),
		// getBinder().inject(ReplicatingCapability.class).getRNG(),
		// new SimDuration(24, TimeUnit.HOURS)).draw();
		// final SimTimeFactory timeFact = getBinder()
		// .inject(SimTimeFactory.class);
		final RandomDistribution.Factory distFact = getBinder().inject(
				RandomDistribution.Factory.class);
		this.onSiteTimeOfDay = distFact.getConstant(new SimDuration(
				getBinder().inject(ConfiguringCapability.class)
				.getProperty(ON_SITE_TIME_OF_DAY_PROPERTY).getNumber(9),
				TimeUnit.HOURS));
		this.offSiteTimeOfDay = distFact.getConstant(new SimDuration(
				getBinder().inject(ConfiguringCapability.class)
				.getProperty(OFF_SITE_TIME_OF_DAY_PROPERTY).getNumber(18),
				TimeUnit.HOURS));
		setCurrentLocation(getBinder().inject(ModelComponentIDFactory.class)
				.createAgentID("world"));
		nonSkeletonActivityCapability = getBinder().inject(NonSkeletonActivityCapability.class);
	}

	@Override
	public SimDuration calculateTravelTimeToBody(SimTime timeOffset,
			Body targetBody)
	{
		return calculateTravelTime(timeOffset, new Body().withCoordinates(
				this.getCurrentCoordinates().get(
						CoordinationUtil.X_COORDINATE_INDEX),
				this.getCurrentCoordinates().get(
						CoordinationUtil.Y_COORDINATE_INDEX),
				this.getCurrentCoordinates().get(
						CoordinationUtil.Z_COORDINATE_INDEX), this.getEntity()
						.getBody().getLatitude(), this.getEntity().getBody()
						.getLongitude(), this.getEntity().getBody()
						.getAltitude()), targetBody,
				CoordinationUtil.defaultWalkingSpeedInMilliMeterPerSecond);
	}

	/**
	 * @see eu.a4ee.model.resource.ResourceManagementWorld#calculateTravelTime(io.coala.time.SimTime,
	 *      eu.a4ee.model.bean.Body, eu.a4ee.model.bean.Body)
	 */
	@Override
	public SimDuration calculateTravelTime(SimTime timeOffset, Body sourceBody,
			Body targetBody)
	{
		return CoordinationUtil.calculateTravelTime(timeOffset, sourceBody,
				targetBody);
	}

	/**
	 * @see eu.a4ee.model.resource.ResourceManagementWorld#calculateTravelTime(io.coala.time.SimTime,
	 *      eu.a4ee.model.bean.Body, eu.a4ee.model.bean.Body, java.lang.Long)
	 */
	@Override
	public SimDuration calculateTravelTime(SimTime timeOffset, Body sourceBody,
			Body targetBody, Long walkingSpeedInMilliMeterPerSecond)
	{
		return CoordinationUtil.calculateTravelTime(timeOffset, sourceBody,
				targetBody, walkingSpeedInMilliMeterPerSecond);
	}

	/**
	 * @see PersonResourceManagementWorld#performActivityChange(String,
	 *      String, EventType)
	 */
	@Override
	public void performActivityChange(final String processID,
			final String processInstanceID, final String activityName, final String activityInstanceId, 
			final String beName, final EventType eventType) throws Exception
	{
		LOG.info("fire 1!");
		fireAndForget(processID, processInstanceID, activityName, activityInstanceId, eventType,
				getOwnerID(), activityName, beName, this.activity);
	}

	/**
	 * @see eu.a4ee.model.resource.PersonResourceManagementWorld#performOccupancyChange(java.lang.String,
	 *      eu.a4ee.model.bean.EventType)
	 */
	@Override
	public void performOccupancyChange(final String processID,
			final String processInstanceID, final String activityName, final String activityInstanceId,
			final String beName, EventType eventType) throws Exception
	{
		LOG.info("fire 2!");
		fireAndForget(processID, processInstanceID, activityName, activityInstanceId, eventType,
				getOwnerID(), activityName, beName, this.movements);
		if (getBinder().inject(Datasource.class).findAssemblyLineByID(beName) != null)
			synchronized (assemblyLineOccupancy)
			{
				int occupancy = assemblyLineOccupancy.containsKey(beName) ? assemblyLineOccupancy
						.get(beName) : 0;
				if (eventType.equals(EventType.ARIVE_AT_ASSEMBLY))
				{
					occupancy++;
					assemblyLineOccupancy.put(beName, occupancy);
				} else if (eventType.equals(EventType.LEAVE_ASSEMBLY))
				{
					occupancy--;
					assemblyLineOccupancy.put(beName, occupancy);
				}
			}
	}

	/** @see eu.a4ee.model.resource.PersonResourceManagementWorld#onMovement() */
	@Override
	public Observable<MovementEvent> onMovement()
	{
		return this.movements.asObservable();
	}

	/** @see eu.a4ee.model.resource.PersonResourceManagementWorld#onActivity() */
	@Override
	public Observable<ActivityEvent> onActivity()
	{
		return this.activity.asObservable();
	}

	/** @see eu.a4ee.model.resource.PersonResourceManagementWorld#enteredSite(io.coala.time.SimTime) */
	@Override
	public void enteredSite(SimTime time)
	{
		if (onSite)
			throw new IllegalStateException(
					"entered time has been set while already on site!");
		onSite = true;
		LOG.info(getOwnerID() + " has entered the site");
	}

	/** @see eu.a4ee.model.resource.PersonResourceManagementWorld#leftSite(io.coala.time.SimTime) */
	@Override
	public void leftSite(SimTime time)
	{
		if (!onSite)
			throw new IllegalStateException(
					"leave time has been set while not on site!");
		onSite = false;
		getBinder().inject(NonSkeletonActivityCapability.class).onLeftSite();
		LOG.info(getOwnerID() + " has left the site");
	}

	// /** @see
	// eu.a4ee.model.resource.PersonResourceManagementWorld#desiredSiteLeaveTime()
	// */
	// @Override
	// public SimTime desiredSiteLeaveTime(final SimTime now)
	// {
	// SimTime result;
	// SimDuration totalDuration = onSiteDuration.plus(offSiteDuration);
	// long cycleTime = now.toMilliseconds().longValue() %
	// totalDuration.getMillis();
	// long cycleCount = (long)
	// Math.floor(now.dividedBy(totalDuration).doubleValue());
	// //if (cycleTime < this.onSiteDuration.toMilliseconds().longValue())
	// result = getBinder().inject(SimTimeFactory.class).create(
	// totalDuration.multipliedBy(cycleCount).plus(this.onSiteDuration).getMillis()
	// , TimeUnit.MILLIS);
	// // else
	// // result = getBinder().inject(SimTimeFactory.class).create(
	// //
	// totalDuration.multipliedBy(cycleCount+1).plus(this.onSiteDuration).getMillis()
	// // , TimeUnit.MILLIS);
	// //if (result.isBefore(now))
	// LOG.warn(getOwnerID()+" is going to leave the site after "+new
	// SimDuration(cycleTime,TimeUnit.MILLIS));
	// return result;
	// }
	//

//	protected SimDuration getOffsiteDurationFromNowOn(final SimTime now)
//	{
//		return this.offSiteDuration;
//	}

	/** @see eu.a4ee.model.resource.PersonResourceManagementWorld#desiredSiteLeaveTime() */
	@Override
	public SimDuration onSiteDelay(final SimTime now)
	{
		return enterDuration(now);
//		SimDuration offSiteDuration = this.getOffsiteDurationFromNowOn(now);
//		SimTime result;
//		SimDuration totalDuration = onSiteDuration.plus(offSiteDuration)
//				.toMilliseconds();
//		long cycleTime = now.toMilliseconds().longValue()
//				% totalDuration.toMilliseconds().getMillis();
//		long cycleCount = (long) Math.floor(now.dividedBy(totalDuration)
//				.doubleValue());
//		if (cycleTime < this.offSiteDuration.toMilliseconds().longValue())
//			result = getBinder().inject(SimTimeFactory.class).create(
//					(totalDuration.multipliedBy(cycleCount)).plus(
//							this.offSiteDuration.toMilliseconds()).getMillis(),
//					TimeUnit.MILLIS);
//		else
//			result = getBinder().inject(SimTimeFactory.class).create(
//					now.toMilliseconds().longValue(), TimeUnit.MILLIS);
//		LOG.warn(getOwnerID()
//				+ " will wait outside for "
//				+ getBinder()
//						.inject(SimTimeFactory.class)
//						.create(result.toMilliseconds().longValue()
//								- now.toMilliseconds().longValue(),
//								TimeUnit.MILLIS).toHours() + " until: "
//				+ result);
//		return new SimDuration(result.toMilliseconds().longValue()
//				- now.toMilliseconds().longValue(), TimeUnit.MILLIS);
	}

	

	

}
