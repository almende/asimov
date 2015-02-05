package io.asimov.model;

import org.joda.time.Duration;
import org.joda.time.Period;

import io.coala.dsol.util.DsolModelComponent;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * {@link PersonTraceModelComponent}
 * 
 * @date $Date: 2014-09-28 14:42:17 +0200 (zo, 28 sep 2014) $
 * @version $Revision: 1083 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
public interface PersonTraceModelComponent extends
		DsolModelComponent<DEVSSimulatorInterface, PersonTraceModel>
{

	/** */
	String ALL_ACTIVITIES = "ALL Activities";

	/** */
	String ANY_ACTIVITY = "Any Activity";

	/** */
	String ALL_EQUIPMENT = "ALL Equipment";

	/** */
	String ALL_LIGHTING = "ALL Lighting";

	/** */
	String ALL_ROOMS = "ALL Rooms";

	/** */
	String ALL_OCCUPANTS = "ALL Occupants";

	/** */
	String ANY_OCCUPANT = "ANY Occupant";

	/** */
	String EXTRAPOLATE_PERIOD_KEY = "extrapolate.period";

	/** */
	String EXTRAPOLATE_REPEAT_KEY = "extrapolate.repeat";

	/** */
	Duration extrapolatePeriod = Period.parse(
			System.getProperty(EXTRAPOLATE_PERIOD_KEY, "P7D"))
			.toStandardDuration();

	/** */
	Integer extrapolateRepeat = Integer.valueOf(System.getProperty(
			EXTRAPOLATE_REPEAT_KEY, "0"));

}
