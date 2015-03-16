package io.asimov.model;

import io.asimov.model.sl.ASIMOVTerm;

import javax.persistence.Entity;

import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 * 
 * @author suki
 */
@Entity
@NoSql(dataType = "connections")
public class ConnectionFrom extends Connection
{

	/** */
	private static final long serialVersionUID = 1L;
	
	public static String TERM_NAME = "CONNECTION_FROM";
	
	public static final String SOURCE_BODY = "SOURCE_BODY";
	
	/** Pattern for a {@link ConnectionFrom} {@link Term} representation */
	public static ASIMOVTerm PATTERN = new ASIMOVTerm().withName(TERM_NAME)
			.instantiate(DELAY,  null)
			.instantiate(SOURCE_BODY, null);
	
	protected ASIMOVTerm getPattern(){
		return PATTERN;
	}


	private Body sourceBody;

	/**
	 * 
	 * @return
	 */
	public Body getSourceBody()
	{
		return sourceBody;
	}

	/**
	 * 
	 * @param source
	 */
	public void setSourceBody(Body sourceBody)
	{
		this.sourceBody = sourceBody;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		final ConnectionFrom other = (ConnectionFrom) obj;
		if (this.getSourceBody() != other.getSourceBody()
				&& (this.getSourceBody() == null || !this.getSourceBody()
						.equals(other.getSourceBody())))
		{
			return false;
		}
		if (this.getDelay() != other.getDelay()
				&& (this.getDelay() == null || !this.getDelay().equals(
						other.getDelay())))
		{
			if (this.getDelay() == null || other.getDelay() == null)
				return true;
			else
				return false;
		}
		return true;
	}

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 89
				* hash
				+ (this.getSourceBody() != null ? this.getSourceBody()
						.hashCode() : 0);
		hash = 89 * hash
				+ (this.getDelay() != null ? this.getDelay().hashCode() : 0);
		return hash;
	}

	@Override
	public Connection fromXML(Object xmlBean)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object toXML()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
