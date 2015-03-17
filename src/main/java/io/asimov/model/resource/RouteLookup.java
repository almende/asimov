package io.asimov.model.resource;

import io.asimov.agent.process.NonSkeletonActivityCapability;
import io.asimov.agent.scenario.ScenarioManagementWorld;
import io.asimov.model.ActivityParticipation;
import io.asimov.model.ActivityParticipation.ActivityParticipant;
import io.asimov.model.ActivityParticipationResourceInformation;
import io.coala.agent.AgentID;
import io.coala.capability.CapabilityFactory;
import io.coala.enterprise.fact.AbstractCoordinationFact;
import io.coala.enterprise.fact.AbstractCoordinationFactBuilder;
import io.coala.enterprise.fact.CoordinationFact;
import io.coala.enterprise.fact.CoordinationFactType;
import io.coala.enterprise.fact.FactID;
import io.coala.enterprise.role.AbstractInitiator;
import io.coala.enterprise.role.Executor;
import io.coala.enterprise.role.Initiator;
import io.coala.model.ModelComponentID;
import io.coala.time.SimTime;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * {@link RouteLookup}
 * 
 * @version $Revision: 1074 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public interface RouteLookup extends CoordinationFact
{

	//@JsonIgnore
	//public static Logger LOG = LogUtil.getLogger(RouteLookup.class);
	
	/**
	 * {@link RouteInitiator}
	 * 
	 * @version $Revision: 1074 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public interface RouteInitiator extends Initiator<Result>
	{
		public interface Factory extends CapabilityFactory<RouteInitiator> {
			
		}
		RouteInitiator initiate(final ActivityParticipant producer, final ActivityParticipation.Request observer, final boolean tomorrow);
		/**
		 * @param nonSkeletonActivityCapabilityImpl
		 */
		RouteInitiator initiate(final NonSkeletonActivityCapability initiator, final AgentID currentLocation,  final AgentID scenarioAgentID);
	}

	/**
	 * {@link RouteProvider}
	 * 
	 * @version $Revision: 1074 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public interface RouteProvider extends Executor<Request>
	{
		public interface Factory extends CapabilityFactory<RouteProvider> {
			
		}

		
	}

	/**
	 * {@link Request}
	 * 
	 * @version $Revision: 1074 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 *
	 */
	public class Request extends AbstractCoordinationFact
	{

		/** */
		private static final long serialVersionUID = 1L;

		private AgentID destinationResource = null;

		private AgentID currentlyOccpupiedResource = null;

		/**
		 * {@link DirectoryLookup} zero-arg bean constructor
		 */
		protected Request()
		{
			super();
		}

		/**
		 * {@link Request} constructor
		 * 
		 * @param id
		 * @param producerID
		 * @param senderID
		 * @param receiverID
		 * @param expiration
		 */
		protected Request(final FactID id,
				final ModelComponentID<?> producerID, final AgentID senderID,
				final AgentID receiverID, final SimTime expiration)
		{
			super(id, producerID, senderID, receiverID, expiration);
		}

		/**
		 * {@link Builder}
		 * 
		 * @version $Revision: 1074 $
		 * @author <a href="mailto:Rick@almende.org">Rick</a>
		 *
		 */
		public static class Builder extends
				AbstractCoordinationFactBuilder<Request, Builder>
		{

			private AgentID currentlyOccpupiedResource = null;
			
			private AgentID destinationResource = null;

			/**
			 * @param initiator the {@link ActivityParticipant}
			 * @param cause the {@link ActivityParticipation.Request}
			 */
			public static Builder forProducer(
					final ActivityParticipant initiator,
					final ActivityParticipation.Request cause,
					final boolean tomorrow)
			{
				AgentID targetSpace = null;
				for (ActivityParticipationResourceInformation otherResource : cause
						.getOtherResourceInfo())
				{
					if (otherResource.getResourceType().equals(
							ARUMResourceType.ASSEMBLY_LINE))
					{
						targetSpace = otherResource.getResourceAgent();
					}
					// TODO Later also for equipment?
				}
				if (tomorrow)
					targetSpace = new AgentID(initiator.getOwnerID().getModelID(),"world");
				return new Builder()
						.withID(initiator.getTime(), cause)
						.withProducer(initiator)
						.withDestinationResource(targetSpace)
						.withCurrentlyOccpupiedResource(
								initiator.getCurrentlyOccupiedResource(cause)).withReceiverID(initiator.getScenarioAgentID());
			}

			/** @see CoordinationFact.Builder#build() */
			@Override
			public Request build()
			{
//				LOG.info("Builder.build():"+this.currentlyOccpupiedResource);
				return new Request(getFactID(), getProducerID(), getSenderID(),
						getReceiverID(), getExpiration())
						.withDestinationResource(this.destinationResource)
						.withCurrentlyOccpupiedResource(
								this.currentlyOccpupiedResource);
			}

			/**
			 * @param destinationResource the destinationResource to set
			 * @return The RouteLookup.Request
			 */
			private Builder withDestinationResource(AgentID destinationResource)
			{
				this.destinationResource = destinationResource;
				return this;
			}

			/**
			 * @param currentlyOccpupiedResource the currentlyOccpupiedResource
			 *            to set
			 * @return The RouteLookup.Request
			 */
			private Builder withCurrentlyOccpupiedResource(
					AgentID currentlyOccpupiedResource)
			{
				this.currentlyOccpupiedResource = currentlyOccpupiedResource;
				return this;
			}

			/**
			 * @param distanceMatrix
			 */
			public static Builder forProducer(AbstractInitiator<?> initiator, final AgentID scenarioAgentID,final AgentID sourceSpace, final AgentID targetSpace)
			{
				
				return new Builder()
						.withID(initiator.getOwnerID().getModelID(), initiator.getTime())
						.withProducer(initiator)
						.withDestinationResource(targetSpace)
						.withCurrentlyOccpupiedResource(sourceSpace)
						.withReceiverID(scenarioAgentID);
			
			}

			/**
			 * @param initiator
			 * @param currentLocation
			 * @return
			 */
			public static Builder forProducer(
					final NonSkeletonActivityCapability initiator,
					final AgentID currentLocation, final AgentID scenarioAgentID)
			{
				return new Builder()
				.withID(initiator.getOwnerID().getModelID(), initiator.getTime())
				.withProducer(initiator)
				.withDestinationResource(new AgentID(initiator.getOwnerID().getModelID(),ScenarioManagementWorld.WORLD_NAME))
				.withCurrentlyOccpupiedResource(currentLocation)
				.withReceiverID(scenarioAgentID);
			}

		}

		/**
		 * @return the AgentID of the destination Resource
		 */
		public AgentID getDestinationResource()
		{
			return destinationResource;
		}

		/**
		 * @param destinationResource the destinationResource to set
		 * @return The RouteLookup.Request
		 */
		public Request withDestinationResource(AgentID destinationResource)
		{
			setDestinationResource(destinationResource);
			return this;
		}

		/**
		 * @param destinationResource the destinationResource to set
		 */
		public void setDestinationResource(AgentID destinationResource)
		{
			this.destinationResource = destinationResource;
		}

		/**
		 * @return the currentlyOccpupiedResource
		 */
		public AgentID getCurrentlyOccpupiedResource()
		{
			return this.currentlyOccpupiedResource;
		}

		/**
		 * @param currentlyOccpupiedResource the currentlyOccpupiedResource to
		 *            set
		 */
		public void setCurrentlyOccpupiedResource(
				AgentID currentlyOccpupiedResource)
		{
			this.currentlyOccpupiedResource = currentlyOccpupiedResource;
		}

		/**
		 * @param currentlyOccpupiedResource the currentlyOccpupiedResource to
		 *            set
		 * @return The RouteLookup.Request
		 */
		public Request withCurrentlyOccpupiedResource(
				AgentID currentlyOccpupiedResource)
		{
			setCurrentlyOccpupiedResource(currentlyOccpupiedResource);
//			LOG.info("Request.withCurrentlyOccpupiedResource():"+getCurrentlyOccpupiedResource());
			return this;
		}

	}

	/**
	 * {@link Result}
	 * 
	 * @version $Revision: 1074 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 * 
	 */
	public class Result extends AbstractCoordinationFact implements RouteLookup
	{

		/** */
		private static final long serialVersionUID = 1L;

		private LinkedHashMap<AgentID, List<Number>> routeOfRepresentativesWithCoordidnates;

		/**
		 * {@link Result} zero-arg bean constructor
		 */
		protected Result()
		{
			super();
		}

		/**
		 * {@link Result} constructor
		 * 
		 * @param id
		 * @param producerID
		 * @param senderID
		 * @param receiverID
		 * @param expiration
		 */
		protected Result(final FactID id, final ModelComponentID<?> producerID,
				final AgentID senderID, final AgentID receiverID,
				final SimTime expiration)
		{
			super(id, producerID, senderID, receiverID, expiration);
		}

		/**
		 * @return the routeOfRepresentativesWithCoordidnates
		 */
		public LinkedHashMap<AgentID, List<Number>> getRouteOfRepresentativesWithCoordidnates()
		{
			return this.routeOfRepresentativesWithCoordidnates;
		}

		/**
		 * @param routeOfRepresentativesWithCoordidnates the
		 *            routeOfRepresentativesWithCoordidnates to set
		 */
		public void setRouteOfRepresentativesWithCoordidnates(
				LinkedHashMap<AgentID, List<Number>> routeOfRepresentativesWithCoordidnates)
		{
			this.routeOfRepresentativesWithCoordidnates = routeOfRepresentativesWithCoordidnates;
		}

		/**
		 * @param routeOfRepresentativesWithCoordidnates the
		 *            routeOfRepresentativesWithCoordidnates to set
		 * @return the RouteLookup result containing the route to the resource
		 *         in the request as a map of agents to coordinates
		 */
		public Result withRouteOfRepresentativesWithCoordidnates(
				LinkedHashMap<AgentID, List<Number>> routeOfRepresentativesWithCoordidnates)
		{
			setRouteOfRepresentativesWithCoordidnates(routeOfRepresentativesWithCoordidnates);
			return this;
		}

		/**
		 * {@link Builder}
		 * 
		 * @version $Revision: 1074 $
		 * @author <a href="mailto:Rick@almende.org">Rick</a>
		 *
		 */
		public static class Builder extends
				AbstractCoordinationFactBuilder<Result, Builder>
		{

			private LinkedHashMap<AgentID, List<Number>> routeOfRepresentativesWithCoordidnates;

			/**
			 * @return the routeOfRepresentativesWithCoordidnates
			 */
			public LinkedHashMap<AgentID, List<Number>> getRouteOfRepresentativesWithCoordidnates()
			{
				return this.routeOfRepresentativesWithCoordidnates;
			}

			/**
			 * @param routeOfRepresentativesWithCoordidnates the
			 *            routeOfRepresentativesWithCoordidnates to set
			 */
			public void setRouteOfRepresentativesWithCoordidnates(
					LinkedHashMap<AgentID, List<Number>> routeOfRepresentativesWithCoordidnates)
			{
				this.routeOfRepresentativesWithCoordidnates = routeOfRepresentativesWithCoordidnates;
			}

			/**
			 * @param executor
			 * @param cause
			 */
			public static Builder forProducer(
					final RouteProvider executor,
					final Request cause,
					final LinkedHashMap<AgentID, List<Number>> routeOfRepresentativesWithCoordidnates)
			{
				return new Builder()
						.withID(CoordinationFactType.STATED,
								executor.getTime(), cause)
						.withProducer(executor)
						.withReceiverID(cause.getSenderID())
						.withRouteOfRepresentativesWithCoordidnates(
								routeOfRepresentativesWithCoordidnates);
			}

			/** @see CoordinationFact.Builder#build() */
			@Override
			public Result build()
			{
				return new Result(getFactID(), getProducerID(), getSenderID(),
						getReceiverID(), getExpiration())
						.withRouteOfRepresentativesWithCoordidnates(getRouteOfRepresentativesWithCoordidnates());
			}

			/**
			 * @param routeOfRepresentativesWithCoordidnates the
			 *            routeOfRepresentativesWithCoordidnates to set
			 * @return the RouteLookup result containing the route to the
			 *         resource in the request as a map of agents to coordinates
			 */
			public Builder withRouteOfRepresentativesWithCoordidnates(
					LinkedHashMap<AgentID, List<Number>> routeOfRepresentativesWithCoordidnates)
			{
				setRouteOfRepresentativesWithCoordidnates(routeOfRepresentativesWithCoordidnates);
				return this;
			}

		}

	}

}