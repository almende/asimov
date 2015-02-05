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

public class AvailabilityCheck extends AbstractMessage<ARUMMessageID> implements
		Serializable, JSONConvertible<AvailabilityCheck>
{
	/** */
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private static final Logger LOG = LogUtil
			.getLogger(AvailabilityCheck.class);

	public Serializable requirements;

	protected AvailabilityCheck()
	{
		// Zero arguments constructor
	}

	public AvailabilityCheck(final SimTime time, final AgentID senderID,
			final AgentID receiverID)
	{

		super(new ARUMMessageID(senderID.getModelID(), time), senderID
				.getModelID(), senderID, receiverID);
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
			return String.format("id=\"%s\"", getID());
		}
	}

	@Override
	public AvailabilityCheck fromJSON(final String jsonValue)
	{
		return JsonUtil.fromJSONString(jsonValue, AvailabilityCheck.class);
	}

}