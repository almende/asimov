package io.asimov.model.process;

import io.asimov.agent.process.NotYetInitializedException;
import io.asimov.db.Datasource;
import io.asimov.model.CoordinationUtil;
import io.asimov.model.resource.ResourceDescriptor;
import io.asimov.model.resource.RouteLookup;
import io.asimov.model.resource.RouteLookup.Result;
import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.capability.interact.ReceivingCapability;
import io.coala.enterprise.role.AbstractInitiator;
import io.coala.log.InjectLogger;
import io.coala.model.ModelComponentIDFactory;
import io.coala.model.ModelID;
import io.coala.time.SimDuration;
import io.coala.time.SimTime;
import io.coala.time.SimTimeFactory;
import io.coala.time.TimeUnit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observable;
import rx.Observer;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

/**
 * {@link DistanceMatrixServiceImpl}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public class DistanceMatrixServiceImpl extends AbstractInitiator<RouteLookup> implements
	DistanceMatrixService
{

	/** */
	private static final long serialVersionUID = 1L;
	
	private static final DistanceMatrix distanceMatrix = new DistanceMatrix();
	
	private static final Map<Entry<AgentID,AgentID>, Subject<SimDuration,SimDuration>> subjects = new HashMap<Entry<AgentID,AgentID>,Subject<SimDuration,SimDuration>>();

	public static final SimDuration DEFAULT_WALKING_DURATION = new SimDuration(10, TimeUnit.SECONDS);
	
	@InjectLogger
	private static Logger LOG;
	
	/**
	 * {@link DistanceMatrixServiceImpl} constructor
	 * @param binder
	 */
	@Inject
	protected DistanceMatrixServiceImpl(Binder binder)
	{
		super(binder);
	}
	
	public void generateDistanceMatrix(final AgentID scenarioAgentID) {
		getBinder().inject(ReceivingCapability.class).getIncoming().ofType(RouteLookup.class).subscribe(new Observer<RouteLookup>()
				{

					@Override
					public void onCompleted()
					{
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onError(Throwable e)
					{
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onNext(RouteLookup t)
					{
						// TODO Auto-generated method stub
						onStated(t);
					}
				});
				if (distanceMatrix.isEmpty()) {
					for (ResourceDescriptor<?> r1 : getBinder().inject(Datasource.class).findResourceDescriptors()) {
						final AgentID sourceAgentID = getBinder()
								.inject(ModelComponentIDFactory.class)
								.createAgentID(r1.getName());
						for (ResourceDescriptor<?> r2 : getBinder().inject(Datasource.class).findResourceDescriptors()) {
							final AgentID targetAgentID = getBinder()
									.inject(ModelComponentIDFactory.class)
									.createAgentID(r2.getName());
							try
							{
								findWalkingTimeDistanceBetween(scenarioAgentID, sourceAgentID, targetAgentID);
							} catch (Exception e)
							{
								LOG.error("Failed to fill distance matrix");
							}
						}
					}
				}
	}
	
	
	@Override
	public SimDuration findWalkingTimeDistanceBetween(final AgentID sourceAgentID, final AgentID targetAgentID) throws NotYetInitializedException {
		if (distanceMatrix.isEmpty())
			throw new NotYetInitializedException();
//		SimDuration result = RxUtil.awaitFirst(observeWalkingTimeDistanceBetween(sourceAgentID, targetAgentID));
		Entry<AgentID,AgentID> entry = distanceMatrix.getEntriesForAgentIDs(sourceAgentID, targetAgentID).get(0);
		SimDuration result = distanceMatrix.get(entry);
		return result;
	}
	
	private Observable<SimDuration> observeWalkingTimeDistanceBetween(final AgentID sourceAgentID, final AgentID targetAgentID) {
		Entry<AgentID,AgentID> entry = distanceMatrix.getEntriesForAgentIDs(sourceAgentID, targetAgentID).get(0);
		return subjects.get(entry).asObservable();
	}
	
	private Observable<SimDuration> findWalkingTimeDistanceBetween(final AgentID scenarioAgentID, final AgentID sourceAgentID, final AgentID targetAgentID) throws Exception{
		Entry<AgentID,AgentID> entry = distanceMatrix.getEntriesForAgentIDs(sourceAgentID, targetAgentID).get(0);
		if (distanceMatrix.containsKey(entry)) {
			return observeWalkingTimeDistanceBetween(sourceAgentID, targetAgentID);
		} else {
			Subject<SimDuration,SimDuration> subject = ReplaySubject.create();
			subjects.put(entry,subject);
			LOG.warn("Requested route: from "+sourceAgentID+"  to "+targetAgentID);
			if (sourceAgentID.getValue().equals(targetAgentID.getValue()))
				subject.onNext(new SimDuration(0, TimeUnit.MILLIS));
			else
				subject.onNext(DEFAULT_WALKING_DURATION);
			subject.onCompleted();
			//send(RouteLookup.Request.Builder.forProducer(this, scenarioAgentID, sourceAgentID, targetAgentID).build());
			return subject;
		}
		
	}

	/** @see io.coala.enterprise.role.AbstractInitiator#onStated(io.coala.enterprise.fact.CoordinationFact) */
	@Override
	public void onStated(RouteLookup state)
	{
		AgentID currentBE = null;
		AgentID firstBE = null;
		SimDuration totalDuration = new SimDuration(0, TimeUnit.MILLIS);
		for (AgentID routeEntry : ((Result)state)
				.getRouteOfRepresentativesWithCoordidnates().keySet())
		{
			if (currentBE == null)
			{
				firstBE = routeEntry;
				currentBE = routeEntry;
				continue;
			}
			totalDuration = totalDuration.plus(CoordinationUtil.calculateTravelTime(SimTime.ZERO,
					CoordinationUtil
							.getCoordinatesForNonMovingElement(getBinder().inject(Datasource.class), currentBE),
					CoordinationUtil
							.getCoordinatesForNonMovingElement(getBinder().inject(Datasource.class), routeEntry)));
		}
		Entry<AgentID,AgentID> entry = distanceMatrix.getEntriesForAgentIDs(firstBE, currentBE).get(0);
		LOG.warn("Got route with travel time: "+totalDuration);
		if (!distanceMatrix.containsKey(entry)) {
			distanceMatrix.put(entry, totalDuration);
			subjects.get(entry).onNext(distanceMatrix.get(entry));
			subjects.get(entry).onCompleted();
		}
	}
	
	private static class DistanceMatrix implements IDistanceMatrix
	{
		
		private HashMap<String, SimDuration> map = new HashMap<String, SimDuration>();

		private static final String delimiter = "~~~~";
		
		
		public List<Entry<AgentID, AgentID>> getEntriesForAgentIDs(
				final AgentID sourceAgentID, final AgentID targetAgentID)
		{
			List<String> strList = new ArrayList<String>();
			strList.add(sourceAgentID.toString());
			strList.add(sourceAgentID.getValue().toString());
			Collections.sort(strList);
			String result = strList.get(0) + delimiter + strList.get(1);
			return getEntriesForString(result);
		}
		
		private String getStringForEntry(Entry<AgentID, AgentID> entry)
		{
			List<String> strList = new ArrayList<String>();
			strList.add(entry.getKey().toString());
			strList.add(entry.getValue().toString());
			Collections.sort(strList);
			String result = strList.get(0) + delimiter + strList.get(1);
			return result;
		}

		private List<Entry<AgentID, AgentID>> getEntriesForString(
				final String entryStr)
		{
			List<Entry<AgentID, AgentID>> result = new ArrayList<Entry<AgentID, AgentID>>();
			final String[] agentIDStrings = entryStr.split(delimiter);
			final AgentID key = new AgentID(new ModelID(
					agentIDStrings[0].split("|")[0]),
					agentIDStrings[0].split("|")[1]);
			final AgentID value = new AgentID(new ModelID(
					agentIDStrings[1].split("|")[0]),
					agentIDStrings[1].split("|")[1]);
			result.add(new Entry<AgentID, AgentID>()
			{

				@Override
				public AgentID setValue(AgentID value)
				{
					return null;
				}

				@Override
				public AgentID getValue()
				{
					return value;
				}

				@Override
				public AgentID getKey()
				{
					return key;
				}

				@Override
				public int hashCode()
				{
					return entryStr.hashCode();
				}

				@Override
				public boolean equals(Object other)
				{
					return this.hashCode() == other.hashCode();
				}

			});
			result.add(new Entry<AgentID, AgentID>()
			{

				@Override
				public AgentID setValue(AgentID value)
				{
					return null;
				}

				@Override
				public AgentID getValue()
				{
					return key;
				}

				@Override
				public AgentID getKey()
				{
					return value;
				}

				@Override
				public int hashCode()
				{
					return entryStr.hashCode();
				}

				@Override
				public boolean equals(Object other)
				{
					return this.hashCode() == other.hashCode();
				}

			});
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public SimDuration get(Object key)
		{
			return map
					.get((key instanceof Entry<?, ?>) ? getStringForEntry((Entry<AgentID, AgentID>) key)
							: key);
		}

		/** @see java.util.Map#size() */
		@Override
		public int size()
		{
			return map.size();
		}

		/** @see java.util.Map#isEmpty() */
		@Override
		public boolean isEmpty()
		{
			return map.isEmpty();
		}

		/** @see java.util.Map#containsKey(java.lang.Object) */
		@SuppressWarnings("unchecked")
		@Override
		public boolean containsKey(Object key)
		{
			return map.containsKey((key instanceof Entry<?, ?>) ? getStringForEntry((Entry<AgentID, AgentID>) key)
					: key);
		}

		/** @see java.util.Map#containsValue(java.lang.Object) */
		@Override
		public boolean containsValue(Object value)
		{
			return map.containsValue(value);
		}

		/** @see java.util.Map#put(java.lang.Object, java.lang.Object) */
		@Override
		public SimDuration put(java.util.Map.Entry<AgentID, AgentID> key,
				SimDuration value)
		{
			return map.put(getStringForEntry((Entry<AgentID, AgentID>) key), value);
		}

		/** @see java.util.Map#remove(java.lang.Object) */
		@SuppressWarnings("unchecked")
		@Override
		public SimDuration remove(Object key)
		{
			return map.remove((key instanceof Entry<?, ?>) ? getStringForEntry((Entry<AgentID, AgentID>) key)
					: key);
		}

		/** @see java.util.Map#putAll(java.util.Map) */
		@SuppressWarnings("unchecked")
		@Override
		public void putAll(
				Map<? extends java.util.Map.Entry<AgentID, AgentID>, ? extends SimDuration> m)
		{
			for (Entry<?,?> entry : m.entrySet())
				map.put(getStringForEntry((Entry<AgentID, AgentID>)entry),(SimDuration)entry.getValue());
		}

		/** @see java.util.Map#clear() */
		@Override
		public void clear()
		{
			map.clear();
		}

		/** @see java.util.Map#keySet() */
		@Override
		public Set<java.util.Map.Entry<AgentID, AgentID>> keySet()
		{
			Set<java.util.Map.Entry<AgentID, AgentID>> result = new HashSet<java.util.Map.Entry<AgentID, AgentID>>();
			for (String key : map.keySet())
				result.addAll(this.getEntriesForString(key));
			return result;
		}

		/** @see java.util.Map#values() */
		@Override
		public Collection<SimDuration> values()
		{
			return map.values();
		}

		/** @see java.util.Map#entrySet() */
		@Override
		public Set<java.util.Map.Entry<java.util.Map.Entry<AgentID, AgentID>, SimDuration>> entrySet()
		{
			Set<Entry<java.util.Map.Entry<AgentID, AgentID>, SimDuration>> result = new HashSet<Entry<java.util.Map.Entry<AgentID, AgentID>, SimDuration>>();
			for (final java.util.Map.Entry<String, SimDuration> r : map.entrySet()) {
				result.add(new Entry<Map.Entry<AgentID,AgentID>, SimDuration>()
				{
					@Override
					public SimDuration setValue(SimDuration value)
					{
						return r.setValue(value);	
					}
					
					@Override
					public SimDuration getValue()
					{
					return r.getValue();
					}
					
					@Override
					public Entry<AgentID, AgentID> getKey()
					{
						return getEntriesForString(r.getKey()).get(0);
					}
				});
				result.add(new Entry<Map.Entry<AgentID,AgentID>, SimDuration>()
						{
							@Override
							public SimDuration setValue(SimDuration value)
							{
								return r.setValue(value);	
							}
							
							@Override
							public SimDuration getValue()
							{
							return r.getValue();
							}
							
							@Override
							public Entry<AgentID, AgentID> getKey()
							{
								return getEntriesForString(r.getKey()).get(1);
							}
						});
			}
			return result;
		}
	}
}
