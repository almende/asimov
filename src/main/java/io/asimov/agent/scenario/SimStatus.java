package io.asimov.agent.scenario;

import javax.persistence.Embeddable;

/**
 * 
 * {@link SimStatus}
 * 
 * @date $Date: 2013-08-01 11:03:17 +0200 (do, 01 aug 2013) $
 * @version $Revision: 328 $
 * @author <a href="mailto:Jos@almende.org">Jos</a>
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
@Embeddable
public enum SimStatus
{
	/** simulation has been created */
	CREATED, 
	
	/** world is being build for simulation before simulation start time */
	PREPARING,
	
	/** simulation is running */
	RUNNING, 
	
	/** simulation has finished */
	FINISHED, 
	
	/** simulation has been cancelled */
	CANCELLED,
	
	;
}