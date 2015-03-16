package io.asimov.model;

import io.asimov.model.sl.ASIMOVNode;
import io.asimov.model.sl.ASIMOVTerm;
import io.asimov.model.sl.SL;
import io.asimov.reasoning.sl.SLConvertible;

import javax.persistence.Entity;

import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 * 
 * @author suki
 */
@Entity
@NoSql(dataType = "time")
public class Time extends AbstractEntity<Time> implements SLConvertible<Time>, XMLConvertible<Object, Time>
{

	/** */
	private static final long serialVersionUID = 1L;

	private long time = 0L;
	
	public static final String TERM_NAME = "TIME";
	
	public static final String MILLISECOND = "MILLISECOND";
	
	/** Pattern for a {@link Time} {@link Term} representation */
	public static ASIMOVTerm PATTERN = new ASIMOVTerm().withName(TERM_NAME)
			.instantiate(MILLISECOND,  null);
	
	protected ASIMOVTerm getPattern(){
		return PATTERN;
	}

	public long getMillisecond()
	{
		return this.time;
	}

	public void setMillisecond(long time)
	{
		this.time = time;
	}
	
	public Time withMillisecond(Long time) {
		setMillisecond(time.longValue());
		return this;
	}
	
	public Time withMillisecond(long time) {
		setMillisecond(time);
		return this;
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
		final Time other = (Time) obj;
		if (this.time != other.time)
		{
			return false;
		}
		return true;
	}

	@Override
	public int hashCode()
	{
		int hash = 3;
		hash = 41 * hash + (int)this.time;
		return hash;
	}

	@Override
	public Time fromXML(Object xmlBean)
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

	@Override
	@SuppressWarnings("unchecked")
	public ASIMOVTerm toSL()
	{
		ASIMOVTerm result = getPattern()
				.instantiate(MILLISECOND,SL.integer(getMillisecond()));
		return result;
	}

	@Override
	public <N extends ASIMOVNode<N>> Time fromSL(N term) {
		this.setMillisecond((Long)term.getPropertyValue(MILLISECOND));
		return this;
	}

}
