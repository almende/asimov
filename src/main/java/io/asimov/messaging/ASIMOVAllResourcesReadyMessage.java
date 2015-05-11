package io.asimov.messaging;

import io.coala.agent.AgentID;
import io.coala.message.AbstractMessage;
import io.coala.time.SimTime;


public class ASIMOVAllResourcesReadyMessage extends AbstractMessage<ASIMOVMessageID>
{
	/** */
	private static final long serialVersionUID = 2L;

	public String token = null;

	protected ASIMOVAllResourcesReadyMessage()
	{

	}

	/**
	 * {@link ASIMOVAllResourcesReadyMessage} constructor
	 * 
	 * @param time
	 * @param senderID
	 * @param receiverID
	 * @param content
	 */
	public ASIMOVAllResourcesReadyMessage(final SimTime time, final AgentID senderID,
			final AgentID receiverID, final String token)
	{
		super(new ASIMOVMessageID(senderID.getModelID(), time), senderID
				.getModelID(), senderID, receiverID);
		this.token = token;
	}

}