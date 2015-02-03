package io.asimov.messaging;

import io.coala.message.MessageID;
import io.coala.model.ModelID;
import io.coala.time.SimTime;

/**
 * {@link A4EEMessageID}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class A4EEMessageID extends MessageID<Long, SimTime>
{

	/** */
	private static final long serialVersionUID = 1L;

	private static long msgID = 0;
	
	private static synchronized long getNextCounterValue() {
		msgID++;
		return msgID;
	}

	protected A4EEMessageID()
	{

	}

	/**
	 * {@link A4EEMessageID} constructor
	 * 
	 * @param modelID
	 * @param instant
	 */
	public A4EEMessageID(final ModelID modelID, final SimTime instant)
	{
		super(modelID, getNextCounterValue(), instant);
	}

}