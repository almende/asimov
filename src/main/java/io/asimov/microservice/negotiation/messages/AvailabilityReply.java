package io.asimov.microservice.negotiation.messages;

import io.asimov.messaging.ARUMMessageID;
import io.coala.agent.AgentID;
import io.coala.json.JSONConvertible;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.message.AbstractMessage;
import io.coala.name.Identifier;
import io.coala.time.SimTime;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * {@link AvailabilityReply}
 * 
 * @version $Revision: 1049 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public class AvailabilityReply extends AbstractMessage<ARUMMessageID> implements
		Serializable, JSONConvertible<AvailabilityReply>
{
	/** */
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private static final Logger LOG = LogUtil
			.getLogger(AvailabilityReply.class);

	public boolean available;
	public Serializable requirements;
	public ARUMMessageID replyToId;

	protected AvailabilityReply()
	{
		// Zero arguments constructor
	}

	public AvailabilityReply(final AvailabilityCheck source,
			final SimTime time, final AgentID senderID, final AgentID receiverID)
	{
		super(source.getID(), senderID.getModelID(), senderID, receiverID);
		requirements = source.getRequirements();
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
	 * @return the requirements
	 */
	public Serializable getRequirements()
	{
		return requirements;
	}

	/**
	 * @param requirements the requirements to set
	 */
	public void setRequirements(Serializable requirements)
	{
		this.requirements = requirements;
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
			return String.format("id=\"%s\"", getReplyToId());
		}
	}

	@Override
	public AvailabilityReply fromJSON(final String jsonValue)
	{
		return JsonUtil.fromJSONString(jsonValue, AvailabilityReply.class);
	}

	public ARUMMessageID getReplyToId()
	{
		return this.replyToId;
	}

}
