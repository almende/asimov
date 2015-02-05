package io.asimov.model;


/**
 * {@link PersonTraceEventProducer}
 * 
 * @date $Date: 2014-07-21 11:41:22 +0200 (ma, 21 jul 2014) $
 * @version $Revision: 1004 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public interface PersonTraceEventProducer extends PersonTraceModelComponent
{
	
	/** @param listener the listener */
	void addListener(PersonTraceEventListener listener);

}
