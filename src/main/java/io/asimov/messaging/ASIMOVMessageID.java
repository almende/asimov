package io.asimov.messaging;

import io.coala.message.MessageID;
import io.coala.model.ModelID;
import io.coala.time.SimTime;

/**
 * {@link ASIMOVMessageID}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class ASIMOVMessageID extends MessageID<Long, SimTime>
{

	/** */
	private static final long serialVersionUID = 1L;

	private static long msgID = 0;
	
	private static synchronized long getNextCounterValue() {
		msgID++;
		return msgID;
	}

	protected ASIMOVMessageID()
	{

	}

	/**
	 * {@link ASIMOVMessageID} constructor
	 * 
	 * @param modelID
	 * @param instant
	 */
	public ASIMOVMessageID(final ModelID modelID, final SimTime instant)
	{
		super(modelID, getNextCounterValue(), instant);
	}

}