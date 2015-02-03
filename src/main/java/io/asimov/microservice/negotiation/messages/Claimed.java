package io.asimov.microservice.negotiation.messages;

import io.asimov.messaging.A4EEMessageID;
import io.coala.agent.AgentID;
import io.coala.json.JSONConvertible;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.message.AbstractMessage;
import io.coala.name.Identifier;
import io.coala.time.SimTime;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

public class Claimed extends AbstractMessage<A4EEMessageID> implements
		Serializable, JSONConvertible<Claimed>
{
	/** */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LogUtil.getLogger(Claimed.class);

	public boolean available;

	private A4EEMessageID replyToId;

	protected Claimed()
	{
		// zero argument constructor
	}

	public Claimed(final Claim source, final SimTime time,
			final AgentID senderID, final AgentID receiverID)
	{
		super(source.getID(), senderID.getModelID(), senderID, receiverID);
		replyToId = source.getID();
	}

	/**
	 * @return the available
	 */
	public boolean isAvailable()
	{
		return available;
	}

	/**
	 * @param available the available to set
	 */
	public void setAvailable(boolean available)
	{
		this.available = available;
	}

	/**
	 * @return the id
	 */
	public A4EEMessageID getReplyToId()
	{
		return replyToId;
	}

	/** @see Identifier#toString() */
	@Override
	public String toString()
	{
		return String.format("%s%s", getClass().getSimpleName(), toJSON());
	}

	/** @see JSONConvertible#toJSON() */
	@Override
	public String toJSON()
	{
		try
		{
			// final JsonNode node = JsonUtil.getJOM().valueToTree(this);
			return JsonUtil.getJOM().writeValueAsString(this);
		} catch (final JsonProcessingException e)
		{
			LOG.warn(
					"Problem marshalling " + getClass().getName() + " to JSON",
					e);
			return String.format("id=\"%s\"", getID());
		}
	}

	@Override
	public Claimed fromJSON(final String jsonValue)
	{
		return JsonUtil.fromJSONString(jsonValue, Claimed.class);
	}

}
