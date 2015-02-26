package io.asimov.unavailability;

import io.asimov.messaging.ASIMOVMessageID;
import io.coala.agent.AgentID;
import io.coala.json.JSONConvertible;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.message.AbstractMessage;
import io.coala.name.Identifier;
import io.coala.time.SimDuration;
import io.coala.time.SimTime;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

public class UnAvailabilityRequest extends AbstractMessage<ASIMOVMessageID> implements
		Serializable, JSONConvertible<UnAvailabilityRequest>
{
	/** */
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private static final Logger LOG = LogUtil
			.getLogger(UnAvailabilityRequest.class);

	public AgentID unavailableResource;
	
	public String resourceType;
	
	public String resourceSubType;
	
	public SimDuration unavailablePeriod;

	protected UnAvailabilityRequest()
	{
		// Zero arguments constructor
	}

	public UnAvailabilityRequest(final SimTime time, final AgentID senderID,
			final AgentID receiverID)
	{

		super(new ASIMOVMessageID(senderID.getModelID(), time), senderID
				.getModelID(), senderID, receiverID);
	}


	/**
	 * @return the resourceType
	 */
	public String getResourceType() {
		return resourceType;
	}

	/**
	 * @param resourceType the resourceType to set
	 */
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourceSubType() {
		return resourceSubType;
	}

	public void setResourceSubType(String resourceSubType) {
		this.resourceSubType = resourceSubType;
	}

	
	/**
	 * @return the unavailableResource
	 */
	public AgentID getUnavailableResource() {
		return unavailableResource;
	}

	/**
	 * @param unavailableResource the unavailableResource to set
	 */
	public void setUnavailableResource(AgentID unavailableResource) {
		this.unavailableResource = unavailableResource;
	}

	/**
	 * @return the unavailablePeriod
	 */
	public SimDuration getUnavailablePeriod() {
		return unavailablePeriod;
	}

	/**
	 * @param unavailablePeriod the unavailablePeriod to set
	 */
	public void setUnavailablePeriod(SimDuration unavailablePeriod) {
		this.unavailablePeriod = unavailablePeriod;
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
	public UnAvailabilityRequest fromJSON(final String jsonValue)
	{
		return JsonUtil.fromJSONString(jsonValue, UnAvailabilityRequest.class);
	}

}