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
 * {@link Proposal}
 * 
 * @version $Revision: 1049 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
public class Proposal extends AbstractMessage<ASIMOVMessageID> implements
		Serializable, JSONConvertible<Proposal>
{
	/** */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LogUtil.getLogger(Proposal.class);

	private ASIMOVMessageID replyToId;

	private double score;

	protected Proposal()
	{
		// zero argument constructor
	}

	public Proposal(final ProposalRequest source, final SimTime time,
			final AgentID senderID, final AgentID receiverID)
	{
		super(source.getID(), senderID.getModelID(), senderID, receiverID);
		replyToId = source.getID();
	}

	/**
	 * @return the score
	 */
	public double getScore()
	{
		return this.score;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(double score)
	{
		this.score = score;
	}

	/**
	 * @return the id
	 */
	public ASIMOVMessageID getReplyToId()
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
	public Proposal fromJSON(final String jsonValue)
	{
		return JsonUtil.fromJSONString(jsonValue, Proposal.class);
	}

}
