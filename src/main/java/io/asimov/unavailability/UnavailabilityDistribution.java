package io.asimov.unavailability;

import io.asimov.xml.DistributionTypeEnum;
import io.asimov.xml.TContext;
import io.asimov.xml.TPropertyTypeEnum;
import io.asimov.xml.TResource;
import io.asimov.xml.TUnavailablePeriodDistribution;
import io.coala.log.LogUtil;
import io.coala.random.ProbabilityMass;
import io.coala.random.RandomDistribution;
import io.coala.random.RandomNumberStream;
import io.coala.time.SimDuration;
import io.coala.time.TimeUnit;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


public class UnavailabilityDistribution {
	
	final static Logger LOG = LogUtil.getLogger(UnavailabilityDistribution.class);

	final Map<String, RandomDistribution<SimDuration>> resourceDistributions = new HashMap<String, RandomDistribution<SimDuration>>();

	final RandomDistribution.Factory distFactory;
	final RandomNumberStream rng;

	public UnavailabilityDistribution(final RandomDistribution.Factory distFactory,
			final RandomNumberStream rng) {
		this.rng = rng;
		this.distFactory = distFactory;
	}
	
	public Map<String,RandomDistribution<SimDuration>> getRandomDistributionOfUnavailabilityDists(){
		return resourceDistributions;
	}
	
	public RandomDistribution<SimDuration> getRandomDistributionOfUnavailabilityForResourceId(final String resourceId) {
		if (resourceDistributions.isEmpty()) {
			LOG.error("Random distribution is not yet initialized, first run UnavailabilityDistribution(distFactory,rng).fromXML(context); to intialize.");
			return distFactory.getConstant(SimDuration.ZERO);
		} else {
			return resourceDistributions.get(resourceId);
		}
	}
	
	public UnavailabilityDistribution fromXML(final TContext context) {
		for (TResource resource : context.getResource()) {
			final String resourceId = resource.getResourceId();
			RandomDistribution<SimDuration> dist = distFactory.getConstant(SimDuration.ZERO);
			try {
				dist = getUnavailabilityDurationDist(resource.getUnavailable(), distFactory, rng);
			} catch (Exception e) {
				LOG.error("Failed to load unavailability distribution for "+resourceId,e);
			}
			resourceDistributions.put(resourceId,dist);
		}
		return this;
	}

	/**
	 * 
	 * @param distributionsForUnavailability
	 * @param distFactory
	 * @param rng
	 * @return
	 * @throws Exception
	 */
	private RandomDistribution<SimDuration> getUnavailabilityDurationDist(
			final List<TUnavailablePeriodDistribution> distributionsForUnavailability,
			final RandomDistribution.Factory distFactory,
			final RandomNumberStream rng) throws Exception {
		RandomDistribution<SimDuration> result;

		if (distributionsForUnavailability.size() == 0) {
			result = distFactory.getConstant(SimDuration.ZERO);
			return result;
		}

		final List<ProbabilityMass<SimDuration, Number>> probabilities = new ArrayList<ProbabilityMass<SimDuration, Number>>();

		DistributionTypeEnum type = null;

		for (final TUnavailablePeriodDistribution distForUnavailability : distributionsForUnavailability) {
			if (type == null)
				type = distForUnavailability.getType();
			else if (type != distForUnavailability.getType())
				throw new Exception("Distribution types can not be combined "
						+ type.name() + " and "
						+ distForUnavailability.getType().name());
		}
		if (type == null)
			throw new Exception("No distribution type was found found");
		switch (type) {
		case ENUMERATED_DISCRETE:
			for (final TUnavailablePeriodDistribution distForUnavailability : distributionsForUnavailability) {
				if (distForUnavailability.getPeriod() != null) {
					for (final io.asimov.xml.TProperty pmfProperty : distForUnavailability
							.getProperty()) {
						if (pmfProperty.getName().equals("pmf"))
							if (pmfProperty.getType().equals(TPropertyTypeEnum.INT))
								probabilities
									.add(new ProbabilityMass<SimDuration, Number>(
											new SimDuration(
													Long.valueOf(distForUnavailability
															.getPeriod()
															.getTimeInMillis(
																	new Date())),
													TimeUnit.MILLIS),
											Integer.valueOf((String)pmfProperty.getValue())));
							else
								throw new Exception("Unexpected type for property, expected int.");
					}
				} else {
					throw new Exception("Expected period but got none.");
				}
			}
			result = distFactory.getEnumerated(rng, probabilities);
			break;
		default: {
			throw new IllegalStateException(
					"Not yet implemented unavailability distribution for type: "
							+ type);
		}
		}
		return result;
	}
}
