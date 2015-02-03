package io.asimov.agent.resource;



/**
 * 
 * @author suki
 */
public abstract class Connection extends AbstractEntity<Connection> implements XMLConvertible<Object, Connection>
{

	/** */
	private static final long serialVersionUID = 1L;
	
	public static final String DELAY = "DELAY";

	private Time delay;

	public Time getDelay()
	{
		if (delay == null)
			delay = new Time();
		return delay;
	}

	public void setDelay(Time delay)
	{
		this.delay = delay;
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
		final Connection other = (Connection) obj;
		if (this.delay != other.delay
				&& (this.delay == null || !this.delay.equals(other.delay)))
		{
			return false;
		}
		return true;
	}

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 37 * hash + (this.delay != null ? this.delay.hashCode() : 0);
		return hash;
	}

}
