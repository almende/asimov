package io.asimov.messaging;

import io.coala.agent.AgentID;
import io.coala.message.AbstractMessage;
import io.coala.time.SimTime;

import java.io.Serializable;

/**
 * {@link ARUMMessage}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class ARUMMessage extends AbstractMessage<ARUMMessageID>
{
	/** */
	private static final long serialVersionUID = 1L;

	public Serializable content = null;

	protected ARUMMessage()
	{

	}

	/**
	 * {@link ARUMMessage} constructor
	 * 
	 * @param time
	 * @param senderID
	 * @param receiverID
	 * @param content
	 */
	public ARUMMessage(final SimTime time, final AgentID senderID,
			final AgentID receiverID, final Serializable content)
	{
		super(new ARUMMessageID(senderID.getModelID(), time), senderID
				.getModelID(), senderID, receiverID);
		this.content = content;
	}

}