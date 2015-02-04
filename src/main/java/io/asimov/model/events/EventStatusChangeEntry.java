package io.asimov.model.events;

import io.asimov.model.AbstractNamed;
import io.coala.time.SimTime;

import java.util.Date;

import javax.persistence.Embeddable;

/**
 * {@link EventStatusChangeEntry}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
@Embeddable
public class EventStatusChangeEntry extends AbstractNamed<EventStatusChangeEntry>
{

	/** */
	private static final long serialVersionUID = 1L;
	
	/** */
	protected SimTime simTime;
	
	/** */
	protected Date actualTime;
	
	/** */
	protected EventStatus status;
	
}