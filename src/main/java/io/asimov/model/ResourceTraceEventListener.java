package io.asimov.model;

import io.asimov.model.events.ActivityEvent;

/**
 * {@link ResourceTraceEventListener}
 * 
 * @date $Date: 2014-09-11 15:17:30 +0200 (do, 11 sep 2014) $
 * @version $Revision: 1049 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public interface ResourceTraceEventListener extends PersonTraceModelComponent
{
	
	/** @param event the newly triggered {@link ActivityEvent} */
	void onActivity(ActivityEvent event);

	/** called when no more events occur */
	void onTraceComplete();
}