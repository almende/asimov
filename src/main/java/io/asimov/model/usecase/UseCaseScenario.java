package io.asimov.model.usecase;

import io.asimov.agent.scenario.Replication;
import io.asimov.agent.scenario.SimStatus;
import io.asimov.db.Datasource;
import io.asimov.model.ASIMOVResourceDescriptor;
import io.asimov.model.process.Process;
import io.asimov.model.xml.XmlUtil;
import io.asimov.unavailability.UnavailabilityDistribution;
import io.asimov.xml.ProcessValueRefEnum;
import io.asimov.xml.TContext;
import io.asimov.xml.TDistribution;
import io.asimov.xml.TProcessType;
import io.asimov.xml.TProperty;
import io.asimov.xml.TResource;
import io.asimov.xml.TSkeletonActivityType;
import io.asimov.xml.TSkeletonActivityType.UsedResource;
import io.asimov.xml.TUseCase;
import io.coala.bind.Binder;
import io.coala.exception.CoalaException;
import io.coala.log.LogUtil;
import io.coala.random.ProbabilityMass;
import io.coala.random.RandomDistribution;
import io.coala.random.RandomNumberStream;
import io.coala.random.impl.RandomDistributionFactoryImpl;
import io.coala.resource.FileUtil;
import io.coala.time.SimDuration;
import io.coala.time.TimeUnit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.measure.quantity.Duration;
import javax.measure.unit.SI;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Node;

/**
 * {@link UseCaseScenario}
 * 
 * @date $Date: 2014-11-27 12:41:42 +0100 (do, 27 nov 2014) $
 * @version $Revision: 1124 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
public interface UseCaseScenario {

	/**
	 * @return
	 */
	Set<ASIMOVResourceDescriptor> getResourceDescriptors();

	/**
	 * @return
	 */
	Set<String> getProcessTypeIDs();

	/**
	 * @param processTypeID
	 * @return
	 */
	Process getProcess(String processTypeID);

	/**
	 * @param processTypeID
	 * @return
	 */
	RandomDistribution<SimDuration> getProcessStartDelayDistribution(
			String processTypeID);

	/**
	 * 
	 * @param resourceId
	 * @return
	 */
	RandomDistribution<SimDuration> getResourceUnavailabilityDistribution(
			String resourceId);

	/**
	 * {@link Builder}
	 * 
	 * @version $Revision: 1124 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 *
	 */
	class Builder {

		/** */
		private static final Logger LOG = LogUtil
				.getLogger(UseCaseScenario.Builder.class);

		/** */
		private final Set<ASIMOVResourceDescriptor> resourceDescriptors = new HashSet<ASIMOVResourceDescriptor>();

		/** */
		private final Map<String, Process> processTypes = new HashMap<>();

		/** */
		private final Map<String, RandomDistribution<SimDuration>> processStartDelayDistributions = new HashMap<>();

		/** */
		private Map<String, RandomDistribution<SimDuration>> resourceUnavailabilityDistributions = new HashMap<>();

		// private

		/**
		 * {@link Builder} constructor
		 * 
		 * @param replicationConfig
		 */
		private Builder() {
		}

		public static Builder fromXML(final Replication replication,
				final TUseCase useCase,
				final RandomDistribution.Factory distFactory,
				final RandomNumberStream rng) {
			final Builder result = new Builder();

			final TContext context = UseCaseScenario.Util
					.importTContext(useCase.getContext());

			final List<TProcessType> processTypes = useCase.getProcess();

			try {
				result.resourceDescriptors.addAll(Util.parseResources(context));
			} catch (JAXBException e) {
				LOG.error("Failed to parse input xml resource descriptions to ASIMOV resources:",e);
			} catch (IOException e) {
				LOG.error("Failed to read input xml",e);
			}
			result.withResourceUnavailablilityDistribution(new UnavailabilityDistribution(
					distFactory, rng).fromXML(context).getRandomDistributionOfUnavailabilityDists());
			final Map<String, Set<TDistribution>> processDists = new HashMap<>();
			if (useCase.getProcessTemplate() != null)
				for (TDistribution dist : useCase.getProcessTemplate()) {
					if (!processDists.containsKey(dist.getProcessRef()))
						processDists.put(dist.getProcessRef(),
								new HashSet<TDistribution>());
					processDists.get(dist.getProcessRef()).add(dist);
				}

			if (distFactory != null && rng != null)
				for (TProcessType processType : processTypes) {
					final String processTypeID = processType.getName();
					Process p = new Process().fromXML(processType);
					if (p == null)
						continue;
					result.withProcessType(processTypeID, p);

					final Set<TDistribution> dists = processDists
							.get(processTypeID);
					if (dists == null) {
						LOG.warn("No random distribution(s) parsed "
								+ "for proces type " + processTypeID);
						continue;
					}
					final RandomDistribution<SimDuration> dist = (processDists
							.isEmpty()) ? new RandomDistributionFactoryImpl()
							.getConstant(SimDuration.ZERO.plus(1,
									TimeUnit.HOURS)) : Util
							.getProcessStartDelayDist(dists, distFactory, rng);
					result.withProcessStartDelayDistribution(processTypeID,
							dist);
					/*
					 * FIXME split PMF values and masses into separate
					 * TProperty/ies
					 * 
					 * final List<ProbabilityMass<SimDuration, Number>> pmf =
					 * new ArrayList<ProbabilityMass<SimDuration, Number>>();
					 * for (TDistribution dist :
					 * calibration.getProcessTemplate()) { if
					 * (!dist.getProcessRef().equals(processTypeID)) continue;
					 * 
					 * if (!"startTime".equals(dist.getOtherRef())) continue;
					 * 
					 * // first cache the values final Map<String, SimDuration>
					 * valueCache = new HashMap<>(); for (TProperty prop :
					 * dist.getProperty()) { final int pos =
					 * prop.getName().indexOf("value"); if (pos > 0)
					 * valueCache.put(prop.getName().substring(0, pos), new
					 * SimDuration((Number) prop.getValue(), TimeUnit.MILLIS));
					 * } // then lookup the values for each mass for (TProperty
					 * prop : dist.getProperty()) { final int pos =
					 * prop.getName().indexOf("mass"); if (pos < 0) continue;
					 * pmf.add(ProbabilityMass.of(valueCache.get(prop
					 * .getName().substring(0, pos)), (Number) prop
					 * .getValue())); } }
					 */
				}
			Util.removeInfeasibleProcesses(processTypes, context);
			
			return result;
		}

		/**
		 * @param processTypeID
		 * @param processType
		 */
		public Builder withProcessType(final String processTypeID,
				final Process processType) {
			this.processTypes.put(processTypeID, processType);
			return this;
		}

		/**
		 * @param processTypeID
		 * @param enumerated
		 */
		public Builder withProcessStartDelayDistribution(
				final String processTypeID,
				final RandomDistribution<SimDuration> dist) {
			this.processStartDelayDistributions.put(processTypeID, dist);
			return this;
		}

		/**
		 * @param resourceID
		 * @param enumerated
		 */
		public Builder withResourceUnavailablilityDistribution(
				final Map<String,RandomDistribution<SimDuration>> dist) {
			this.resourceUnavailabilityDistributions = dist;
			return this;
		}

		public UseCaseScenario build() {
			return new CIMScenarioImpl(this.resourceDescriptors, this.processTypes,
					this.processStartDelayDistributions,
					this.resourceUnavailabilityDistributions);
		}

	}

	/**
	 * {@link CIMScenarioImpl}
	 * 
	 * @version $Revision: 1124 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 */
	class CIMScenarioImpl implements UseCaseScenario {

		/** */
		private Set<ASIMOVResourceDescriptor> resourceDescriptors;

		/** */
		private Map<String, Process> processTypes;

		/** */
		private Map<String, RandomDistribution<SimDuration>> processStartDelayDistributions;

		/** */
		private Map<String, RandomDistribution<SimDuration>> resourceUnavailabilityDistributions;

		/**
		 * {@link CIMScenarioImpl} constructor
		 */
		protected CIMScenarioImpl() {
			//
		}

		/**
		 * {@link CIMScenarioImpl} constructor
		 * 
		 * @param replicationConfig
		 * @param materials
		 * @param assemblyLines
		 * @param persons
		 * @param processTypes
		 * @param processStartDelayDistribution
		 */
		public CIMScenarioImpl(
				final Set<ASIMOVResourceDescriptor> resourceDescriptors,
				final Map<String, Process> processTypes,
				final Map<String, RandomDistribution<SimDuration>> processStartDelayDistribution,
				final Map<String, RandomDistribution<SimDuration>> resourceUnavailablilityDistribution) {
			this.resourceDescriptors = Collections.unmodifiableSet(resourceDescriptors);
			this.processTypes = Collections.unmodifiableMap(processTypes);
			this.processStartDelayDistributions = Collections
					.unmodifiableMap(processStartDelayDistribution);
			this.resourceUnavailabilityDistributions = Collections
					.unmodifiableMap(resourceUnavailablilityDistribution);
		}

		@Override
		public Set<ASIMOVResourceDescriptor> getResourceDescriptors() {
			return this.resourceDescriptors;
		}

		@Override
		public Set<String> getProcessTypeIDs() {
			return this.processTypes.keySet();
		}

		@Override
		public Process getProcess(final String processTypeID) {
			return this.processTypes.get(processTypeID);
		}

		@Override
		public RandomDistribution<SimDuration> getProcessStartDelayDistribution(
				final String processTypeID) {
			return this.processStartDelayDistributions.get(processTypeID);
		}

		@Override
		public RandomDistribution<SimDuration> getResourceUnavailabilityDistribution(
				String resourceId) {
			return this.resourceUnavailabilityDistributions.get(resourceId);
		}

	}

	/**
	 * {@link Util}
	 * 
	 * @version $Revision: 1124 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 *
	 */
	class Util {
		/** */
		private static final Logger LOG = LogUtil
				.getLogger(UseCaseScenario.Util.class);

		/**
		 * @param processes
		 * @param gbXML
		 */
		public static void removeInfeasibleProcesses(
				final Collection<TProcessType> processes, final TContext context) {
			if (processes.size() == 0 || context == null)
				return;

			final Set<TProcessType> availableProcesses = new HashSet<TProcessType>(
					processes);
			final Set<TProcessType> unavailableProcesses = new HashSet<TProcessType>();

			for (TProcessType process : processes) {
				for (TSkeletonActivityType activity : process.getActivity()) {
					
					boolean foundResourceDescriptor = activity.getUsedResource().size() == 0;
					if (!foundResourceDescriptor)
						for (UsedResource t : activity.getUsedResource()) {
							for (TResource p : context
									.getResource()) {
								if (p.getResourceType().equals(t.getResourceTypeRef()))
									for (String tr : p.getResourceSubType())
										if (t.getResourceSubTypeRef().equalsIgnoreCase(
												tr)) {
											foundResourceDescriptor = true;
										}
							}
						}
					
					if (!foundResourceDescriptor) {
						LOG.error("Activity "
								+ activity.getName()
								+ " needs more resources that are not in the context.");
						if (availableProcesses.remove(process))
							unavailableProcesses.add(process);
					}
				}
			}
			LOG.info(availableProcesses.size() + " of " + processes.size()
					+ " processes are feasible with "
					+ " this contexts initial resources");
//			if (unavailableProcesses.size() > 0) {
//				LOG.warn("Removing infeasible processes: "
//						+ unavailableProcesses);
//				processes.removeAll(unavailableProcesses);
//			}
		}

		/**
		 * FIXME Should be non static real implemenation from trained data
		 * 
		 * @param distributionsForProcessType
		 * @param distFactory
		 * @param rng
		 * @param simDuration
		 * @return
		 */
		// public static RandomDistribution<SimDuration> getOccupancyOnSiteDist(
		// final Set<TDistribution> distributionsForRole,
		// final RandomDistribution.Factory distFactory,
		// final RandomNumberStream rng, final SimDuration simDuration)
		// {
		// RandomDistribution<SimDuration> result = distFactory
		// .getConstant(new SimDuration(8, TimeUnit.HOURS));
		// return result;
		// }
		//
		// /**
		// * FIXME Should be non static real implemenation from trained data
		// *
		// * @param distributionsForProcessType
		// * @param distFactory
		// * @param rng
		// * @param simDuration
		// * @return
		// */
		// public static RandomDistribution<SimDuration>
		// getOccupancyOffSiteDist(
		// final Set<TDistribution> distributionsForRole,
		// final RandomDistribution.Factory distFactory,
		// final RandomNumberStream rng, final SimDuration simDuration)
		// {
		// RandomDistribution<SimDuration> result = distFactory
		// .getConstant(new SimDuration(24 - 8, TimeUnit.HOURS));
		// return result;
		// }

		/**
		 * @param distributionsForProcessType
		 * @param distFactory
		 * @param rng
		 * @return
		 */
		public static RandomDistribution<SimDuration> getProcessStartDelayDist(
				final Set<TDistribution> distributionsForProcessType,
				final RandomDistribution.Factory distFactory,
				final RandomNumberStream rng) {
			final List<ProbabilityMass<SimDuration, Number>> probabilities = new ArrayList<ProbabilityMass<SimDuration, Number>>();

			// long lowestTime = Long.MAX_VALUE;
			// long latestTime = Long.MIN_VALUE;

			for (final TDistribution distForProcessType : distributionsForProcessType) {
				if (distForProcessType.getValueRef().equals(
						ProcessValueRefEnum.OTHER)
						&& distForProcessType.getOtherRef().equals("startTime"))
				// for (final TProperty startTimeProperty : distForProcessType
				// .getProperty())
				// {
				// lowestTime = Math.min(lowestTime,
				// Long.valueOf(startTimeProperty.getName()));
				// // FIXME Calculate latest end time instead of latest
				// // start time
				// latestTime = Math.max(latestTime,
				// Long.valueOf(startTimeProperty.getName()));
				// }
				{
					for (final TProperty startTimeProperty : distForProcessType
							.getProperty()) {
						probabilities
								.add(new ProbabilityMass<SimDuration, Number>(
										new SimDuration(Long
												.valueOf(startTimeProperty
														.getName()),
												TimeUnit.MILLIS),
										(Integer) startTimeProperty.getValue()));
					}
					RandomDistribution<SimDuration> result = distFactory
							.getEnumerated(rng, probabilities);
					return result;
				}
			}

			// for (final TDistribution distForProcessType :
			// distributionsForProcessType)
			// {
			// if (distForProcessType.getValueRef().equals(
			// ProcessValueRefEnum.OTHER)
			// && distForProcessType.getOtherRef().equals("startTime"))
			// for (final TProperty startTimeProperty : distForProcessType
			// .getProperty())
			// {
			// long time = Long.valueOf(startTimeProperty.getName())
			// - lowestTime;
			// if (time == 0)
			// {
			// // first instance so apply cyclic time
			// time += Math.abs(latestTime - lowestTime);
			// }
			// probabilities
			// .add(new ProbabilityMass<SimDuration, Number>(
			// new SimDuration(time, TimeUnit.MILLIS),
			// (Integer) startTimeProperty.getValue()));
			// }
			// }
			throw new IllegalStateException(
					"Did not find startTime distribution in trained tempate.");

		}

		

		/** */
		public static Collection<ASIMOVResourceDescriptor> parseResources(final TContext context)
				throws JAXBException, IOException {

			final Collection<ASIMOVResourceDescriptor> result = new HashSet<>();
			for (TResource m : context.getResource())
				result.add(new ASIMOVResourceDescriptor().fromXML(m));
			return result;
		}


		/**
		 * @param xmlElement
		 * @return
		 */
		public static TContext importTContext(final Object xmlElement) {
			if (xmlElement == null)
				throw new NullPointerException("No TContext node or file name!");

			if (xmlElement instanceof TContext)
				return (TContext) xmlElement;

			if (xmlElement instanceof Node) {
				// Support the nested gbXML tags from CIMIM.
				// cloning to make sure
				Node node = (Node) xmlElement;
				// System.err.println("node name: " + node.getNodeName()
				// + " type: " + node.getClass().getName());
				if (node.getFirstChild() != null
						&& node.getFirstChild().getNodeName()
								.contains("context"))
					return importTContext(node.getFirstChild());

				try {
					final Object result = XmlUtil.getCIMUnmarshaller()
							.unmarshal(node);
					if (result instanceof TContext)
						return (TContext) result;
				} catch (final Exception e) {
					throw new RuntimeException("Problem parsing TContext from "
							+ xmlElement, e);
				}
			}

			if (xmlElement instanceof String)
				try {
					return (TContext) XmlUtil.getCIMUnmarshaller().unmarshal(
							FileUtil.getFileAsInputStream((String) xmlElement));
				} catch (final Exception e) {
					throw new RuntimeException("Problem parsing GbXML from "
							+ xmlElement, e);
				}

			throw new IllegalStateException("Unsupported gbXML element type "
					+ xmlElement.getClass().getName());
		}

		/**
		 * @param configFile
		 * @param cimFile
		 * @param trainingFile
		 * @param projectID
		 * @param replicationID
		 * @param nofPersons
		 * @param duration
		 * @throws CoalaException
		 * @throws JAXBException
		 */
		public static Replication saveDefaultReplication(final Binder binder,
				final File cimFile, final Amount<Duration> duration)
				throws CoalaException, JAXBException {
			final String replicationID = binder.getID().getModelID().getValue();

			final io.asimov.db.Datasource ds = binder.inject(Datasource.class);

			// remove the replication from the database
			ds.removeReplication();

			final Replication replication = new Replication.Builder()
					.withId(replicationID).withStatus(SimStatus.PREPARING)
					.withContextUri(cimFile.getAbsolutePath())
					.withDurationMS(duration.doubleValue(SI.MILLI(SI.SECOND)))
					.build();

			ds.save(replication);

			return replication;
		}
	}

}
