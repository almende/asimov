package io.asimov.model.events;

import io.asimov.model.AbstractNamed;
import javax.persistence.Embeddable;

/**
 * {@link EventType}
 * 
 * @author <a href="suki@almende.org">Suki van Beusekom</a>
 * 
 */
@Embeddable
public class EventType extends AbstractNamed<EventType> {

	/** */
	private static final long serialVersionUID = 1L;

	/** Some example event types: */

	/**
	 * This {@link EventType} indicates a {@link ASIMOVResourceDescriptor} has
	 * "moved" to another resource {@link ASIMOVResourceDescriptor}
	 */
	public static final EventType TRANSIT_TO_RESOURCE = new EventType()
			.withValue("transitsToResource");

	/**
	 * This {@link EventType} indicates a {@link ASIMOVResourceDescriptor} has
	 * "moved" from another resource {@link ASIMOVResourceDescriptor}
	 */
	public static final EventType TRANSIT_FROM_RESOURCE = new EventType()
			.withValue("transitsFromResource");

	/**
	 * This {@link EventType} indicates an {@link ASIMOVResourceDescriptor} has
	 * started participating in some {@link Activity}
	 */
	public static final EventType START_ACTIVITY = new EventType()
			.withValue("startActivity");

	/**
	 * This {@link EventType} indicates an {@link ASIMOVResourceDescriptor} has
	 * stopped participating in some {@link Activity}
	 */
	public static final EventType STOP_ACTIVITY = new EventType()
			.withValue("stopActivity");

	/**
	 * This {@link EventType} indicates the global operational period (e.g.
	 * working day) has started for now.
	 */
	public static final EventType START_GLOBAL_OPERATIONAL_PERIOD = new EventType()
			.withValue("startGlobalOperationalPeriod");

	/**
	 * This {@link EventType} indicates the global operational period (e.g.
	 * working day) has ended for now.
	 */
	public static final EventType STOP_GLOBAL_OPERATIONAL_PERIOD = new EventType()
			.withValue("stopGlobalOperationalPeriod");

	/**
	 * This {@link EventType} indicates an {@link ASIMOVResourceDescriptor} is
	 * allocated for a process and/or activity
	 */
	public static final EventType ALLOCATED = new EventType()
			.withValue("allocated");

	/**
	 * This {@link EventType} indicates an {@link ASIMOVResourceDescriptor} is
	 * de-allocated for a process and/or activity
	 */
	public static final EventType DEALLOCATED = new EventType()
			.withValue("deAllocated");

	/**
	 * This {@link EventType} indicates an {@link ASIMOVResourceDescriptor} has
	 * become unavailable for all resources {@link AssemblyLine}
	 */
	public static final EventType START_GLOBAL_UNAVAILABILITY = new EventType()
			.withValue("startUnavailableForAllResources");

	/**
	 * This {@link EventType} indicates an {@link ASIMOVResourceDescriptor} has
	 * become available for all resources {@link AssemblyLine}
	 */
	public static final EventType STOP_GLOBAL_UNAVAILABILITY = new EventType()
			.withValue("stopUnavailableForAllResources");

	/**
	 * This {@link EventType} indicates a {@link Process} has started
	 * {@link AssemblyLine}
	 */
	public static final EventType START_PROCESS = new EventType()
			.withValue("startsProcess");

	/**
	 * This {@link EventType} indicates an {@link Process} has stopped
	 * {@link AssemblyLine}
	 */
	public static final EventType STOP_PROCESS = new EventType()
			.withValue("stopsProcess");
	
	/**
	 * This {@link EventType} indicates a {@link Process} has been created
	 */
	public static final EventType PROCESS_CREATED = new EventType()
			.withValue("ProcessCreated");
	
	/**
	 * This {@link EventType} indicates a {@link Process} has been created
	 */
	public static final EventType ACTIVITY_CREATED = new EventType()
			.withValue("ActivityCreated");
	
	/**
	 * This {@link EventType} indicates a {@link Process} has been created
	 */
	public static final EventType RESOURCE_CREATED = new EventType()
			.withValue("ResourceCreated");
	
	/**
	 * This {@link EventType} indicates a {@link Process} has been created
	 */
	public static final EventType INITIAL_MODEL_LOADED = new EventType()
			.withValue("InitialModelLoaded");

	/**
	 * This {@link EventType} indicates an {@link ASIMOVResourceDescriptor} is
	 * ready to participate in the {@link Activity}
	 */
	public static final EventType RESOURCE_READY_FOR_ACTIVITY = new EventType()
			.withValue("readyForParticipation");

	@Override
	public String toString() {
		return getName();
	}

	public boolean equals(Object other) {
		if ((other instanceof EventType) == false)
			return false;
		if (((EventType) other).getName() == null)
			if (this.getName() == null)
				return true;
			else
				return false;
		return (((EventType) other).getName().equals(getName()));

	}

}
