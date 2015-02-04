package io.asimov.model;

import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.tools.SL;

import javax.persistence.Entity;

import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 * 
 * @author suki
 */
@Entity
@NoSql(dataType = "connections")
public class ConnectionTo extends Connection
{

	/** */
	private static final long serialVersionUID = 1L;
	
	public static String TERM_NAME = "CONNECTION_TO";
	
	public static final String TARGET_BODY = "TARGET_BODY";
	
	/** Pattern for a {@link ConnectionTo} {@link Term} representation */
	public static Term PATTERN = SL.term(String.format(
			"(%s :%s ??%s :%s ??%s)", TERM_NAME, 
			DELAY,  DELAY, TARGET_BODY, TARGET_BODY));
	
	protected Term getPattern(){
		return PATTERN;
	}

	private Body targetBody;

	/**
	 * 
	 * @return
	 */
	public Body getTargetBody()
	{
		return targetBody;
	}

	/**
	 * 
	 * @param source
	 */
	public void setTargetBody(Body targetBody)
	{
		this.targetBody = targetBody;
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
		final ConnectionTo other = (ConnectionTo) obj;
		if (this.getTargetBody() != other.getTargetBody()
				&& (this.getTargetBody() == null || !this.getTargetBody()
						.equals(other.getTargetBody())))
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
		int hash = 3;
		hash = 97 * hash
				+ (this.targetBody != null ? this.targetBody.hashCode() : 0);
		hash = 97 * hash
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
