package io.asimov.model;

import io.arum.model.resource.assemblyline.AssemblyLine;
import io.asimov.db.Datasource;
import io.coala.agent.AgentID;
import io.coala.time.SimDuration;
import io.coala.time.SimTime;
import io.coala.time.TimeUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link CoordinationUtil}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public class CoordinationUtil
{

	// 5km/hour
	// *
	// 1.5
	// =
	// 7.5km/hour
	public static long defaultWalkingSpeedInMilliMeterPerSecond = (long) (1389 * 1.5);


	public static final int X_COORDINATE_INDEX = 0;
	public static final int Y_COORDINATE_INDEX = 1;
	public static final int Z_COORDINATE_INDEX = 2;
		
	/** @see eu.a4ee.model.resource.CurrentLocationStateService#getCurrentCoordinates() */
	public static List<Number> getCoordinatesForNonMovingElement(final Datasource ds, final AgentID elementRepresentative) {
		AssemblyLine r = ds.findAssemblyLineByID(elementRepresentative.getValue());
		ArrayList<Number> result = new ArrayList<Number>();
		if (r == null) {
			r = new AssemblyLine().withName("world");
		}
		if (r.getBody() == null)
			r.withBody(new Body().withCoordinates(0, 0, 0, 0, 0, 0).withDimensions(0, 0, 0, 0, 0, 0));
		if (r != null) {
			result.add(getCentreForCoordinateIndex(r.getBody(), X_COORDINATE_INDEX));
			result.add(getCentreForCoordinateIndex(r.getBody(), Y_COORDINATE_INDEX));
			result.add(getCentreForCoordinateIndex(r.getBody(), Z_COORDINATE_INDEX));
		} 
		return result;
	}

	private static Number sum(Number... sum)
	{
		double result = 0;
		for (Number number : sum)
		{
			result += number.doubleValue();
		}
		return (Number) new Double(result);
	}

	private static Number average(Number... sum)
	{
		return (Number) new Double(sum(sum).doubleValue() / sum.length);
	}

	/**
	 * Returns the center of the smallest surrounding cube rotated 0 degrees.
	 * 
	 * @param body
	 * @param coordinateIndex
	 * @return
	 */
	private static Number getCentreForCoordinateIndex(Body body, int coordinateIndex)
	{
		switch (coordinateIndex)
		{
		case X_COORDINATE_INDEX:
			return average(body.getX(), sum(body.getX(), body.getIndoorWidth()));
		case Y_COORDINATE_INDEX:
			return average(body.getY(),
					sum(body.getY(), body.getIndoorHeight()));
		case Z_COORDINATE_INDEX:
			return average(body.getZ(), sum(body.getZ(), body.getIndoorDepth()));
		}
		return null;
	}
	
	
	public static SimDuration calculateTravelTime(SimTime timeOffset, List<Number> sourceBody,
			 List<Number> targetBody)
	{
		return new SimDuration(10, TimeUnit.SECONDS);
//		return calculateTravelTime(timeOffset, new Body().withCoordinates(
//				sourceBody.get(X_COORDINATE_INDEX), 
//				sourceBody.get(Y_COORDINATE_INDEX),
//				sourceBody.get(Z_COORDINATE_INDEX), 0, 0, 0)
//				.withDimensions(0, 0, 0, 0, 0, 0),
//				new Body().withCoordinates(targetBody.get(X_COORDINATE_INDEX), 
//				targetBody.get(Y_COORDINATE_INDEX),
//				targetBody.get(Z_COORDINATE_INDEX), 0, 0, 0)
//				.withDimensions(0, 0, 0, 0, 0, 0),
//				defaultWalkingSpeedInMilliMeterPerSecond);
	}

	public static SimDuration calculateTravelTime(SimTime timeOffset, Body sourceBody,
			Body targetBody)
	{
		return calculateTravelTime(timeOffset, sourceBody, targetBody,
				defaultWalkingSpeedInMilliMeterPerSecond);
	}

	
	public static SimDuration calculateTravelTime(final SimTime timeOffset,
			Body sourceBody, Body targetBody,
			Long walkingSpeedInMilliMeterPerSecond)
	{
		long expectedArivalTime = timeOffset.getMillis();

		Number sx = getCentreForCoordinateIndex(sourceBody, X_COORDINATE_INDEX);
		Number tx = getCentreForCoordinateIndex(targetBody, X_COORDINATE_INDEX);
		Number sy = getCentreForCoordinateIndex(sourceBody, Y_COORDINATE_INDEX);
		Number ty = getCentreForCoordinateIndex(targetBody, Y_COORDINATE_INDEX);
		Number sz = getCentreForCoordinateIndex(sourceBody, Z_COORDINATE_INDEX);
		Number tz = getCentreForCoordinateIndex(targetBody, Z_COORDINATE_INDEX);

		double dx = Math.max(sx.doubleValue(), tx.doubleValue())
				- Math.min(sx.doubleValue(), tx.doubleValue());
		double dy = Math.max(sy.doubleValue(), ty.doubleValue())
				- Math.min(sy.doubleValue(), ty.doubleValue());
		double dz = Math.max(sz.doubleValue(), tz.doubleValue())
				- Math.min(sz.doubleValue(), tz.doubleValue());

		double distanceInMilliMeters = Math.sqrt(dx * dx + dy * dy + dz * dz);
		expectedArivalTime += Math
				.floor(((distanceInMilliMeters * defaultWalkingSpeedInMilliMeterPerSecond) / TimeUnit.MILLIS.convertFrom(1, TimeUnit.SECONDS).longValue()));
		return new SimDuration(expectedArivalTime, TimeUnit.MILLIS);
	}
}
