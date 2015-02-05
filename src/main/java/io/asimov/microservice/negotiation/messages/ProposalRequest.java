package io.asimov.microservice.negotiation.messages;

import io.asimov.messaging.ASIMOVMessageID;
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

/**
 * {@link ProposalRequest}
 * 
 * @version $Revision: 1049 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
public class ProposalRequest extends AbstractMessage<ASIMOVMessageID> implements
		Serializable, JSONConvertible<ProposalRequest>
{
	/** */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LogUtil.getLogger(ProposalRequest.class);

	private Serializable query;

	private Claim claim;

	protected ProposalRequest()
	{
		// zero argument constructor
	}

	public ProposalRequest(final SimTime time, final AgentID senderID,
			final AgentID receiverID)
	{
		super(new ASIMOVMessageID(senderID.getModelID(), time), senderID
				.getModelID(), senderID, receiverID);
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
	public ProposalRequest fromJSON(final String jsonValue)
	{
		return JsonUtil.fromJSONString(jsonValue, ProposalRequest.class);
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
	 * @return the claim
	 */
	public Serializable getClaim()
	{
		return claim;
	}

	/**
	 * @param claim the claim to set
	 */
	public void setClaim(Claim claim)
	{
		this.claim = claim;
	}
}