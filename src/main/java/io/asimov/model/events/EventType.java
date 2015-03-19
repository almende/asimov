package io.asimov.model.events;

import io.asimov.model.AbstractNamed;
import io.asimov.model.XMLConvertible;
import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@link EventType}
 * 
 * @date $Date: 2014-03-28 11:22:14 +0100 (Fri, 28 Mar 2014) $
 * @version $Revision: 806 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Embeddable
public class EventType extends AbstractNamed<EventType>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** Some example event types: */
	

	/**
	 * This {@link EventType} indicates a {@link Resource} has "moved" to another resource
	 * {@link Resource}
	 */
	public static final EventType TRANSIT_TO_RESOURCE = new EventType().withValue("transitsToResource");

	/**
	 * This {@link EventType} indicates a {@link Resource} has "moved" from another resource
	 * {@link Resource}
	 */
	public static final EventType TRANSIT_FROM_RESOURCE = new EventType().withValue("transitsFromResource");
	
	/**
	 * This {@link EventType} indicates an {@link Person}  has started performing some
	 * {@link Activity}
	 */
	public static final EventType START_ACTIVITY = new EventType().withValue("startActivity");

	/**
	 * This {@link EventType} indicates an {@link Person}   has stopped performing some
	 * {@link Activity}
	 */
	public static final EventType STOP_ACTIVITY = new EventType().withValue("stopActivity");
	
	/**
	 * This {@link EventType} indicates an {@link Person} has started using some
	 * {@link Material}
	 */
	public static final EventType START_USE_MATERIAL = new EventType().withValue("startUseOfMaterial");

	/**
	 * This {@link EventType} indicates an {@link Person} has stopped using some
	 * {@link Material}
	 */
	public static final EventType STOP_USE_MATERIAL = new EventType().withValue("stopUseOfMaterial");
	
	/**
	 * This {@link EventType} indicates an {@link Person} is waiting to perform some activity
	 * {@link AssemblyLine}
	 */
	public static final EventType START_WAITING = new EventType().withValue("waiting");
	
	/**
	 * This {@link EventType} indicates an {@link Person} was waiting to perform some activity
	 * {@link AssemblyLine}
	 */
	public static final EventType STOP_WAITING = new EventType().withValue("waited");
	
	/**
	 * This {@link EventType} indicates an {@link Person} has become unavailable for all resources
	 * {@link AssemblyLine}
	 */
	public static final EventType START_GLOBAL_UNAVAILABILITY = new EventType().withValue("startUnavailableForAllResources");
	
	/**
	 * This {@link EventType} indicates an {@link Person} has become available for all resources
	 * {@link AssemblyLine}
	 */
	public static final EventType STOP_GLOBAL_UNAVAILABILITY = new EventType().withValue("stopUnavailableForAllResources");
	
	
	/**
	 * This {@link EventType} indicates a {@link Process} has started
	 * {@link AssemblyLine}
	 */
	public static final EventType START_PROCESS = new EventType().withValue("startsProcess");
	
	/**
	 * This {@link EventType} indicates an {@link Process} has stopped
	 * {@link AssemblyLine}
	 */
	public static final EventType STOP_PROCESS = new EventType().withValue("stopsProcess");
	
	/**
	 * This {@link EventType} indicates an {@link ASIMOVResourceDescriptor} is ready to participate in the {@link Activity}
	 */
	public static final EventType RESOURCE_READY_FOR_ACTIVITY = new EventType().withValue("readyForParticipation");

	/**
	 * The reference of the subject that the person performs an action on.
	 */
	@JsonIgnore
	public static final transient String sREFERENCE = "REFERENCE";
	
	/**
	 * The reference of the subject that the person performs an action on.
	 */
	@JsonIgnore
	public static final transient String sBE_REFERENCE = "BE_REFERENCE";
	
	@Override
	public String toString()
	{
		return getName();
	}
	
	public boolean equals(Object other) {
		if ((other instanceof EventType) == false)
			return false;
		if (((EventType)other).getName() == null)
			if (this.getName() == null)
				return true;
			else
				return false;
		return (((EventType)other).getName().equals(getName()));
			
			
	}
	

}
