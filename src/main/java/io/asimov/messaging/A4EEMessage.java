package io.asimov.messaging;

import io.coala.agent.AgentID;
import io.coala.message.AbstractMessage;
import io.coala.time.SimTime;

import java.io.Serializable;

/**
 * {@link A4EEMessage}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class A4EEMessage extends AbstractMessage<A4EEMessageID>
{
	/** */
	private static final long serialVersionUID = 1L;

	public Serializable content = null;

	protected A4EEMessage()
	{

	}

	/**
	 * {@link A4EEMessage} constructor
	 * 
	 * @param time
	 * @param senderID
	 * @param receiverID
	 * @param content
	 */
	public A4EEMessage(final SimTime time, final AgentID senderID,
			final AgentID receiverID, final Serializable content)
	{
		super(new A4EEMessageID(senderID.getModelID(), time), senderID
				.getModelID(), senderID, receiverID);
		this.content = content;
	}

}