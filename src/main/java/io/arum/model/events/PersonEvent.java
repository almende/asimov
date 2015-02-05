package io.arum.model.events;

import io.arum.model.resource.person.Person;
import io.asimov.model.XMLConvertible;
import io.asimov.xml.TEventTrace.EventRecord;
import io.coala.time.SimTime;

/**
 * {@link PersonEvent}
 * 
 * @date $Date: 2014-11-19 16:24:16 +0100 (wo, 19 nov 2014) $
 * @version $Revision: 1120 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public interface PersonEvent<THIS extends PersonEvent<THIS>> extends
		XMLConvertible<EventRecord, THIS>
{
	
	/** @return the virtual time of execution */
	SimTime getExecutionTime();

	/** @return the {@link Person}  */
	Person getPerson();

	/** @param person the {@link Person} to set */
	THIS withPerson(final Person person);
	
	/** @param the activity this event was performed for, null if none */
	void setActivity(final String activity);
	

	/**
	 * @return
	 */
	String getActivity();
}
