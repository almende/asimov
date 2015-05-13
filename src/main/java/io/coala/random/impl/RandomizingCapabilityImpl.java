package io.coala.random.impl;

import io.coala.bind.Binder;
import io.coala.capability.BasicCapability;
import io.coala.capability.replicate.BasicReplicatingCapability;
import io.coala.capability.replicate.RandomizingCapability;
import io.coala.config.CoalaProperty;
import io.coala.log.InjectLogger;
import io.coala.random.RandomNumberStream;
import io.coala.random.RandomNumberStreamID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;

/**
 * {@link RandomizingCapabilityImpl}
 * 
 * @date $Date$
 * @version $Revision$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public class RandomizingCapabilityImpl extends BasicCapability implements
		RandomizingCapability
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private final Map<RandomNumberStreamID, RandomNumberStream> rng = Collections
			.synchronizedMap(new HashMap<RandomNumberStreamID, RandomNumberStream>());

	@InjectLogger
	private Logger LOG;

	/**
	 * {@link BasicReplicatingCapability} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected RandomizingCapabilityImpl(final Binder binder)
	{
		super(binder);
	}

	/** @see .RandomizingCapability#getRNG() */
	@Override
	public RandomNumberStream getRNG()
	{
		return getRNG(MAIN_RNG_ID);
	}

	/** @see RandomizingCapability#getRNG(RandomNumberStreamID) */
	@Override
	public RandomNumberStream getRNG(RandomNumberStreamID rngID)
	{
		if (!this.rng.containsKey(rngID))
			this.rng.put(rngID, newRNG(rngID));
		return this.rng.get(rngID);
	}

	private RandomNumberStream newRNG(final RandomNumberStreamID streamID)
	{
		// add owner ID hash code for reproducible seeding variance across
		// owner agents
		LOG.info("SEED => "+(CoalaProperty.randomSeed.value().getLong()
				+ getID().getOwnerID().hashCode()));
		return getBinder().inject(RandomNumberStream.Factory.class).create(
				streamID,
				CoalaProperty.randomSeed.value().getLong()
						+ getID().getOwnerID().hashCode());
	}

}
