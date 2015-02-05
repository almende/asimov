package io.asimov.messaging;

import io.coala.message.MessageID;
import io.coala.model.ModelID;
import io.coala.time.SimTime;

/**
 * {@link ARUMMessageID}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class ARUMMessageID extends MessageID<Long, SimTime>
{

	/** */
	private static final long serialVersionUID = 1L;

	private static long msgID = 0;
	
	private static synchronized long getNextCounterValue() {
		msgID++;
		return msgID;
	}

	protected ARUMMessageID()
	{

	}

	/**
	 * {@link ARUMMessageID} constructor
	 * 
	 * @param modelID
	 * @param instant
	 */
	public ARUMMessageID(final ModelID modelID, final SimTime instant)
	{
		super(modelID, getNextCounterValue(), instant);
	}

}