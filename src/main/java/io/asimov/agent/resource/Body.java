package io.asimov.agent.resource;

import javax.persistence.Entity;

import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 * {@link Body}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Entity
@NoSql(dataType = "rooms")
public class Body extends AbstractEntity<Body> implements ResourceType
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private Number x;

	/** */
	private Number y;

	/** */
	private Number z;

	/** */
	private Number latitude;

	/** */
	private Number longitude;

	/** */
	private Number altitude;

	/** */
	private Number indoorWidth;

	/** */
	private Number indoorHeight;

	/** */
	private Number indoorDepth;

	/** */
	private Number outdoorWidth;

	/** */
	private Number outdoorHeight;

	/** */
	private Number outdoorDepth;

	
	/**
	 * @return the x
	 */
	public Number getX()
	{
		return this.x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(final Number x)
	{
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public Number getY()
	{
		return this.y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(final Number y)
	{
		this.y = y;
	}

	/**
	 * @return the z
	 */
	public Number getZ()
	{
		return this.z;
	}

	/**
	 * @param z the z to set
	 */
	public void setZ(final Number z)
	{
		this.z = z;
	}

	/**
	 * @return the latitude
	 */
	public Number getLatitude()
	{
		return this.latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	protected void setLatitude(final Number latitude)
	{
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public Number getLongitude()
	{
		return this.longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	protected void setLongitude(final Number longitude)
	{
		this.longitude = longitude;
	}

	/**
	 * @return the altitude
	 */
	public Number getAltitude()
	{
		return this.altitude;
	}

	/**
	 * @param altitude the altitude to set
	 */
	protected void setAltitude(final Number altitude)
	{
		this.altitude = altitude;
	}

	/**
	 * @return the indoorWidth
	 */
	public Number getIndoorWidth()
	{
		return this.indoorWidth;
	}

	/**
	 * @param indoorWidth the indoorWidth to set
	 */
	protected void setIndoorWidth(final Number indoorWidth)
	{
		this.indoorWidth = indoorWidth;
	}

	/**
	 * @return the indoorHeight
	 */
	public Number getIndoorHeight()
	{
		return this.indoorHeight;
	}

	/**
	 * @param indoorHeight the indoorHeight to set
	 */
	protected void setIndoorHeight(final Number indoorHeight)
	{
		this.indoorHeight = indoorHeight;
	}

	/**
	 * @return the indoorDepth
	 */
	public Number getIndoorDepth()
	{
		return this.indoorDepth;
	}

	/**
	 * @param indoorDepth the indoorDepth to set
	 */
	protected void setIndoorDepth(final Number indoorDepth)
	{
		this.indoorDepth = indoorDepth;
	}

	/**
	 * @return the outdoorWidth
	 */
	public Number getOutdoorWidth()
	{
		return this.outdoorWidth;
	}

	/**
	 * @param outdoorWidth the outdoorWidth to set
	 */
	protected void setOutdoorWidth(final Number outdoorWidth)
	{
		this.outdoorWidth = outdoorWidth;
	}

	/**
	 * @return the outdoorHeight
	 */
	public Number getOutdoorHeight()
	{
		return this.outdoorHeight;
	}

	/**
	 * @param outdoorHeight the outdoorHeight to set
	 */
	protected void setOutdoorHeight(final Number outdoorHeight)
	{
		this.outdoorHeight = outdoorHeight;
	}

	/**
	 * @return the outdoorDepth
	 */
	public Number getOutdoorDepth()
	{
		return this.outdoorDepth;
	}

	/**
	 * @param outdoorDepth the outdoorDepth to set
	 */
	protected void setOutdoorDepth(final Number outdoorDepth)
	{
		this.outdoorDepth = outdoorDepth;
	}
	
	
	/**
	 * @param name the (new) {@link RoomType}
	 * @return this {@link Body} object
	 */
	public Body withCoordinates(final Number x, final Number y, final Number z,
			final Number latitude, final Number longitude, final Number altitude)
	{
		this.setX(x);
		this.setY(y);
		this.setZ(z);
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		this.setAltitude(altitude);

		return this;
	}

	/**
	 * @param name the (new) {@link RoomType}
	 * @return this {@link Body} object
	 */
	public Body withDimensions(final Number indoorWidth,
			final Number indoorHeight, final Number indoorDepth,
			final Number outdoorWidth, final Number outdoorHeight,
			final Number outdoorDepth)
	{
		setIndoorWidth(indoorWidth);
		setIndoorHeight(indoorHeight);
		setIndoorDepth(indoorDepth);
		setOutdoorWidth(outdoorWidth);
		setOutdoorHeight(outdoorHeight);
		setOutdoorDepth(outdoorDepth);

		return this;
	}


	/** @see java.lang.Object#hashCode() */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 8;
		result = prime * result
				+ ((altitude == null) ? 0 : altitude.hashCode());
		result = prime * result
				+ ((indoorDepth == null) ? 0 : indoorDepth.hashCode());
		result = prime * result
				+ ((indoorHeight == null) ? 0 : indoorHeight.hashCode());
		result = prime * result
				+ ((indoorWidth == null) ? 0 : indoorWidth.hashCode());
		result = prime * result
				+ ((latitude == null) ? 0 : latitude.hashCode());
		result = prime * result
				+ ((longitude == null) ? 0 : longitude.hashCode());
		result = prime * result
				+ ((outdoorDepth == null) ? 0 : outdoorDepth.hashCode());
		result = prime * result
				+ ((outdoorHeight == null) ? 0 : outdoorHeight.hashCode());
		result = prime * result
				+ ((outdoorWidth == null) ? 0 : outdoorWidth.hashCode());
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		result = prime * result + ((z == null) ? 0 : z.hashCode());
		return result;
	}

	/** @see java.lang.Object#equals(java.lang.Object) */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Body other = (Body) obj;
		if (altitude == null)
		{
			if (other.altitude != null)
				return false;
		} else if (!altitude.equals(other.altitude))
			return false;
		if (indoorDepth == null)
		{
			if (other.indoorDepth != null)
				return false;
		} else if (!indoorDepth.equals(other.indoorDepth))
			return false;
		if (indoorHeight == null)
		{
			if (other.indoorHeight != null)
				return false;
		} else if (!indoorHeight.equals(other.indoorHeight))
			return false;
		if (indoorWidth == null)
		{
			if (other.indoorWidth != null)
				return false;
		} else if (!indoorWidth.equals(other.indoorWidth))
			return false;
		if (latitude == null)
		{
			if (other.latitude != null)
				return false;
		} else if (!latitude.equals(other.latitude))
			return false;
		if (longitude == null)
		{
			if (other.longitude != null)
				return false;
		} else if (!longitude.equals(other.longitude))
			return false;
		if (outdoorDepth == null)
		{
			if (other.outdoorDepth != null)
				return false;
		} else if (!outdoorDepth.equals(other.outdoorDepth))
			return false;
		if (outdoorHeight == null)
		{
			if (other.outdoorHeight != null)
				return false;
		} else if (!outdoorHeight.equals(other.outdoorHeight))
			return false;
		if (outdoorWidth == null)
		{
			if (other.outdoorWidth != null)
				return false;
		} else if (!outdoorWidth.equals(other.outdoorWidth))
			return false;
		if (x == null)
		{
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (y == null)
		{
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		if (z == null)
		{
			if (other.z != null)
				return false;
		} else if (!z.equals(other.z))
			return false;
		return true;
	}
	
	

}
