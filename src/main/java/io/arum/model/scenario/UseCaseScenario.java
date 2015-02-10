package io.arum.model.scenario;

import io.arum.model.resource.assemblyline.AssemblyLine;
import io.arum.model.resource.person.Person;
import io.arum.model.resource.person.PersonRole;
import io.arum.model.resource.supply.Material;
import io.asimov.agent.scenario.Replication;
import io.asimov.agent.scenario.SimStatus;
import io.asimov.db.Datasource;
import io.asimov.model.Body;
import io.asimov.model.process.Process;
import io.asimov.model.xml.XmlUtil;
import io.asimov.xml.ProcessValueRefEnum;
import io.asimov.xml.TContext;
import io.asimov.xml.TDistribution;
import io.asimov.xml.TProcessType;
import io.asimov.xml.TProperty;
import io.asimov.xml.TSkeletonActivityType;
import io.asimov.xml.TSkeletonActivityType.RoleInvolved;
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
public interface UseCaseScenario
{

	/**
	 * @return
	 */
	Set<Material> getMaterials();

	/**
	 * @return
	 */
	Set<AssemblyLine> getAssemblyLines();

	/**
	 * @return
	 */
	Set<Person> getPersons();

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
	 * {@link Builder}
	 * 
	 * @version $Revision: 1124 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 *
	 */
	class Builder
	{

		/** */
		private static final Logger LOG = LogUtil
				.getLogger(UseCaseScenario.Builder.class);

		/** */
		private final Set<Material> materials = new HashSet<>();

		/** */
		private final Set<AssemblyLine> assemblyLines = new HashSet<>();

		/** */
		private final Set<Person> persons = new HashSet<>();

		/** */
		private final Map<String, Process> processTypes = new HashMap<>();

		/** */
		private final Map<String, RandomDistribution<SimDuration>> processStartDelayDistributions = new HashMap<>();

		// private

		/**
		 * {@link Builder} constructor
		 * 
		 * @param replicationConfig
		 */
		private Builder()
		{
		}

		public static Builder fromXML(final Replication replication,
				final TUseCase useCase,
				final RandomDistribution.Factory distFactory,
				final RandomNumberStream rng)
		{
			final Builder result = new Builder();

			final TContext context = UseCaseScenario.Util.importTContext(useCase.getContext());
			
			final List<TProcessType> processTypes = useCase.getProcess();
			
			Util.parseMaterialsAndAssemblyLines(context, result.materials, result.assemblyLines);

			final Map<String, Set<TDistribution>> processDists = new HashMap<>();
			if (useCase.getProcessTemplate() != null)
				for (TDistribution dist : useCase.getProcessTemplate())
				{
					if (!processDists.containsKey(dist.getProcessRef()))
						processDists.put(dist.getProcessRef(),
								new HashSet<TDistribution>());
					processDists.get(dist.getProcessRef()).add(dist);
				}

			if (distFactory != null && rng != null)
				for (TProcessType processType : processTypes)
				{
					final String processTypeID = processType.getName();
					Process p = new Process().fromXML(processType);
					if (p == null)
						continue;
					result.withProcessType(processTypeID,p);

					final Set<TDistribution> dists = processDists
							.get(processTypeID);
					if (dists == null)
					{
						LOG.warn("No random distribution(s) parsed "
								+ "for proces type " + processTypeID);
						continue;
					}
					final RandomDistribution<SimDuration> dist = (processDists.isEmpty()) ? new RandomDistributionFactoryImpl()
							.getConstant(SimDuration.ZERO.plus(1,
									TimeUnit.HOURS)) : Util
							.getProcessStartDelayDist(dists, distFactory, rng);
					result.withProcessStartDelayDistribution(processTypeID,
							dist);
					/*
					FIXME split PMF values and masses into separate TProperty/ies
					
					final List<ProbabilityMass<SimDuration, Number>> pmf = new ArrayList<ProbabilityMass<SimDuration, Number>>();
					for (TDistribution dist : calibration.getProcessTemplate())
					{
						if (!dist.getProcessRef().equals(processTypeID))
							continue;

						if (!"startTime".equals(dist.getOtherRef()))
							continue;

						// first cache the values
						final Map<String, SimDuration> valueCache = new HashMap<>();
						for (TProperty prop : dist.getProperty())
						{
							final int pos = prop.getName().indexOf("value");
							if (pos > 0)
								valueCache.put(prop.getName().substring(0, pos),
										new SimDuration((Number) prop.getValue(),
												TimeUnit.MILLIS));
						}
						// then lookup the values for each mass
						for (TProperty prop : dist.getProperty())
						{
							final int pos = prop.getName().indexOf("mass");
							if (pos < 0)
								continue;
							pmf.add(ProbabilityMass.of(valueCache.get(prop
									.getName().substring(0, pos)), (Number) prop
									.getValue()));
						}
					}
					*/
				}
			Util.removeInfeasibleProcesses(processTypes, context);
			final Set<Person> roleDistribution = Util
					.createPersonRoleDistribution(processTypes);
			// simCase.setRoles(new Roles());
			// for (Entry<Person, RoleTemplate> entry :
			// roleDistribution.entrySet())
			// simCase.getRoles()
			// .getRole()
			// .add(new TRole().withRoleName(entry.getKey().getName())
			// .withId(entry.getValue().getRoleRef()));
			result.persons.addAll(roleDistribution);
			return result;
		}

		/**
		 * @param processTypeID
		 * @param processType
		 */
		public Builder withProcessType(final String processTypeID,
				final Process processType)
		{
			this.processTypes.put(processTypeID, processType);
			return this;
		}

		/**
		 * @param processTypeID
		 * @param enumerated
		 */
		public Builder withProcessStartDelayDistribution(
				final String processTypeID,
				final RandomDistribution<SimDuration> dist)
		{
			this.processStartDelayDistributions.put(processTypeID, dist);
			return this;
		}

		public UseCaseScenario build()
		{
			return new CIMScenarioImpl(this.materials, this.assemblyLines, this.persons,
					this.processTypes, this.processStartDelayDistributions);
		}

	}

	/**
	 * {@link CIMScenarioImpl}
	 * 
	 * @version $Revision: 1124 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 */
	class CIMScenarioImpl implements UseCaseScenario
	{

		/** */
		private Set<Material> materials;

		/** */
		private Set<AssemblyLine> assemblyLines;

		/** */
		private Set<Person> persons;

		/** */
		private Map<String, Process> processTypes;

		/** */
		private Map<String, RandomDistribution<SimDuration>> processStartDelayDistributions;

		/**
		 * {@link CIMScenarioImpl} constructor
		 */
		protected CIMScenarioImpl()
		{
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
				final Set<Material> materials,
				final Set<AssemblyLine> assemblyLines,
				final Set<Person> persons,
				final Map<String, Process> processTypes,
				final Map<String, RandomDistribution<SimDuration>> processStartDelayDistribution)
		{
			this.materials = Collections.unmodifiableSet(materials);
			this.assemblyLines = Collections.unmodifiableSet(assemblyLines);
			this.persons = Collections.unmodifiableSet(persons);
			this.processTypes = Collections.unmodifiableMap(processTypes);
			this.materials = Collections.unmodifiableSet(materials);
			this.processStartDelayDistributions = Collections
					.unmodifiableMap(processStartDelayDistribution);
		}

		@Override
		public Set<Material> getMaterials()
		{
			return this.materials;
		}

		@Override
		public Set<AssemblyLine> getAssemblyLines()
		{
			return this.assemblyLines;
		}

		@Override
		public Set<Person> getPersons()
		{
			return this.persons;
		}

		@Override
		public Set<String> getProcessTypeIDs()
		{
			return this.processTypes.keySet();
		}

		@Override
		public Process getProcess(final String processTypeID)
		{
			return this.processTypes.get(processTypeID);
		}

		@Override
		public RandomDistribution<SimDuration> getProcessStartDelayDistribution(
				final String processTypeID)
		{
			return this.processStartDelayDistributions.get(processTypeID);
		}

	}

	/**
	 * {@link Util}
	 * 
	 * @version $Revision: 1124 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 *
	 */
	class Util
	{
		/** */
		private static final Logger LOG = LogUtil
				.getLogger(UseCaseScenario.Util.class);


		/**
		 * @param processes
		 * @param gbXML
		 */
		public static void removeInfeasibleProcesses(
				final Collection<TProcessType> processes, final TContext context)
		{
			if (processes.size() == 0 || context == null)
				return;

			final Set<TProcessType> availableProcesses = new HashSet<TProcessType>(
					processes);
			final Set<TProcessType> unavailableProcesses = new HashSet<TProcessType>();

			for (TProcessType process : processes)
			{
				for (TSkeletonActivityType activity : process.getActivity())
				{
					boolean foundPersonRole = activity.getRoleInvolved().size() == 0;
					boolean foundAssemblyLineType = activity.getUsedAssemlyLineType().size() == 0;
					boolean foundMaterial = activity.getUsedComponent().size() == 0;
					if (!foundPersonRole)
					{
						LOG.error("Activity " + activity.getName()
								+ " can not be applied on the context because of missing person role.");
						if (availableProcesses.remove(process))
							unavailableProcesses.add(process);
					}
					if (!foundAssemblyLineType)
					{
						LOG.error("Activity " + activity.getName()
								+ " can not be applied on the context because of missing assemblyLine type.");
						if (availableProcesses.remove(process))
							unavailableProcesses.add(process);
					}
					if (!foundMaterial)
					{
						LOG.error("Activity "
								+ activity.getName()
								+ " needs more materials that are not in the context.");
						if (availableProcesses.remove(process))
							unavailableProcesses.add(process);
					}
				}
			}
			LOG.info(availableProcesses.size() + " of " + processes.size()
					+ " processes are feasible in "
					+ "this building's assemblyLine and equipment types");
			if (unavailableProcesses.size() > 0)
			{
				LOG.warn("Removing infeasible processes: "
						+ unavailableProcesses);
				processes.removeAll(unavailableProcesses);
			}
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
				final RandomNumberStream rng)
		{
			final List<ProbabilityMass<SimDuration, Number>> probabilities = new ArrayList<ProbabilityMass<SimDuration, Number>>();

			// long lowestTime = Long.MAX_VALUE;
			// long latestTime = Long.MIN_VALUE;

			for (final TDistribution distForProcessType : distributionsForProcessType)
			{
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
							.getProperty())
					{
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

		/**
		 * @param processes
		 * @param result
		 * @return
		 */
		public static Set<Person> createPersonRoleDistribution(
				final Collection<TProcessType> processes)
		{
			final Set<Person> result = new HashSet<Person>();

			for (TProcessType process : processes)
				for (TSkeletonActivityType activity : process.getActivity())
				{
					for (RoleInvolved roleInvolved : activity.getRoleInvolved())
					{
						int minNofPersonsRequired = 0;
						int nofPersonsWithRoleFound = 0;
						if (roleInvolved.getAverageNumberOfPersons() > 1)
						{
							minNofPersonsRequired = (int) Math
									.ceil(roleInvolved
											.getAverageNumberOfPersons());
						} else
						{
							minNofPersonsRequired = 1;
						}
						for (Person occ : result)
						{
							for (PersonRole r : occ.getTypes())
								if (r.getName()
									.equals(roleInvolved.getRoleRef()))
								nofPersonsWithRoleFound++;
						}
						for (int i = nofPersonsWithRoleFound; i < minNofPersonsRequired; i++)
						{
							final String type = roleInvolved.getRoleRef();
							ArrayList<PersonRole> roles = new ArrayList<PersonRole>();
							roles.add(new PersonRole().withName(type));
							result.add(new Person()
									.withName(type + "_" + result.size())
									.withTypes(roles)
									.withBody(
											new Body().withCoordinates(0, 0, 0,
													0, 0, 0).withDimensions(0,
													0, 0, 0, 0, 0)));
						}
					}
				}
			return result;
		}

	


		/** */
		public static Collection<Material> parseMaterials(final TContext context)
				throws JAXBException, IOException
		{
			

			final Collection<Material> result = new HashSet<>();
			for (io.asimov.xml.TContext.Material m : context.getMaterial())
				result.add(new Material().fromXML(m));
			return result;
		}

		
		/** */
		public static Collection<AssemblyLine> parseAssemblyLines(final TContext context)
				throws JAXBException, IOException
		{
			

			final Collection<AssemblyLine> result = new HashSet<>();
			for (io.asimov.xml.TContext.AssemblyLine m : context.getAssemblyLine())
				result.add(new AssemblyLine().fromXML(m));
			return result;
		}

		
		/** */
		public static Collection<Person> parsePersons(final TContext context)
				throws JAXBException, IOException
		{
			

			final Collection<Person> result = new HashSet<>();
			for (io.asimov.xml.TContext.Person m : context.getPerson())
				result.add(new Person().fromXML(m));
			return result;
		}
		
		/**
		 * @param gbXML
		 * @param materials
		 * @param assemblyLines
		 */
		public static void parseMaterialsAndAssemblyLines(final TContext context,
				final Set<Material> materials, final Set<AssemblyLine> assemblyLines)
		{
			try {
				materials.addAll(parseMaterials(context));
				assemblyLines.addAll(parseAssemblyLines(context));
			} catch (JAXBException | IOException e) {
				LOG.error(e.getMessage(),e);
			}
		}

		
		/**
		 * @param xmlElement
		 * @return
		 */
		public static TContext importTContext(final Object xmlElement)
		{
			if (xmlElement == null)
				throw new NullPointerException("No TContext node or file name!");

			if (xmlElement instanceof TContext)
				return (TContext) xmlElement;

			if (xmlElement instanceof Node)
			{
				// Support the nested gbXML tags from CIMIM.
				// cloning to make sure
				Node node = (Node) xmlElement;
				// System.err.println("node name: " + node.getNodeName()
				// + " type: " + node.getClass().getName());
				if (node.getFirstChild() != null
						&& node.getFirstChild().getNodeName().contains("context"))
					return importTContext(node.getFirstChild());

				try
				{
					final Object result = XmlUtil.getCIMUnmarshaller().unmarshal(node);
					if (result instanceof TContext)
						return (TContext) result;
				} catch (final Exception e)
				{
					throw new RuntimeException("Problem parsing TContext from "
							+ xmlElement, e);
				}
			}

			if (xmlElement instanceof String)
				try
				{
					return (TContext) XmlUtil.getCIMUnmarshaller().unmarshal(
							FileUtil.getFileAsInputStream((String) xmlElement));
				} catch (final Exception e)
				{
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
				final File cimFile, final Amount<Duration> duration) throws CoalaException,
				JAXBException
		{
			final String replicationID = binder.getID().getModelID().getValue();

			final io.asimov.db.Datasource ds = binder.inject(Datasource.class);

			// remove the replication from the database
			ds.removeReplication();


			final Replication replication = new Replication.Builder()
					.withId(replicationID)
					.withStatus(SimStatus.PREPARING)
					.withContextUri(cimFile.getAbsolutePath())
					.withDurationMS(duration.doubleValue(SI.MILLI(SI.SECOND)))
					.build();

			ds.save(replication);

			return replication;
		}
	}

}
