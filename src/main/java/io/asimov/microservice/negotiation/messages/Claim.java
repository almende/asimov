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

public class Claim extends AbstractMessage<A4EEMessageID> implements
		Serializable, JSONConvertible<Claim>
{
	/** */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LogUtil.getLogger(Claim.class);

	private boolean deClaim = false;

	private Serializable query;

	private Serializable assertion;

	protected Claim()
	{
		// zero argument constructor
	}

	public Claim(final SimTime time, final AgentID senderID,
			final AgentID receiverID)
	{

		super(new A4EEMessageID(senderID.getModelID(), time), senderID
				.getModelID(), senderID, receiverID);
	}

	/**
	 * @return the deClaim
	 */
	public boolean isDeClaim()
	{
		return deClaim;
	}

	/**
	 * @param deClaim the deClaim to set
	 */
	public void setDeClaim(boolean deClaim)
	{
		this.deClaim = deClaim;
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
	public Claim fromJSON(final String jsonValue)
	{
		return JsonUtil.fromJSONString(jsonValue, Claim.class);
	}

	/**
	 * @return the query
	 */
	public Serializable getQuery()
	{
		return query;
	}

	/**
	 * @param query the query to set
	 */
	public void setQuery(Serializable query)
	{
		this.query = query;
	}

	/**
	 * @return the assertion
	 */
	public Serializable getAssertion()
	{
		return assertion;
	}

	/**
	 * @param assertion the assertion to set
	 */
	public void setAssertion(Serializable assertion)
	{
		this.assertion = assertion;
	}
}