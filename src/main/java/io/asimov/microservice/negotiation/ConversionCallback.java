package io.asimov.microservice.negotiation;

import java.io.Serializable;

public interface ConversionCallback
{
	/**
	 * Method that converts the formula to determine requirement match and
	 * availability to the formula for allocation confirmation.
	 * 
	 * @param f Formula to determine requirement match and availability.
	 * @return Converted formula for allocation confirmation.
	 */
	public Serializable convert(Serializable f);
}