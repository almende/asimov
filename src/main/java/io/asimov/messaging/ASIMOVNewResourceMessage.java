package io.asimov.messaging;

import io.asimov.model.ASIMOVResourceDescriptor;
import io.coala.agent.AgentID;
import io.coala.message.AbstractMessage;
import io.coala.time.SimTime;


public class ASIMOVNewResourceMessage extends AbstractMessage<ASIMOVMessageID>
{
	/** */
	private static final long serialVersionUID = 4L;

	public ASIMOVResourceDescriptor resource = null;

	protected ASIMOVNewResourceMessage()
	{

	}

	/**
	 * {@link ASIMOVNewResourceMessage} constructor
	 * 
	 * @param time
	 * @param senderID
	 * @param receiverID
	 * @param content
	 */
	public ASIMOVNewResourceMessage(final SimTime time, final AgentID senderID,
			final AgentID receiverID, final ASIMOVResourceDescriptor resource)
	{
		super(new ASIMOVMessageID(senderID.getModelID(), time), senderID
				.getModelID(), senderID, receiverID);
		this.resource = resource;
	}

}