package io.asimov.agent.resource;

import io.coala.jsa.sl.SLConvertible;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;

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
	public static Term PATTERN = SL.term(String.format(
			"(%s (::? :%s ??%s))", TERM_NAME, 
			MILLISECOND,  MILLISECOND));
	
	protected Term getPattern(){
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
	public Term toSL()
	{
		Term result = getPattern()
				.instantiate(MILLISECOND,SL.integer(getMillisecond()));
		return result;
	}

	@Override
	public Time fromSL(Node term)
	{
		MatchResult mr = getPattern().match(term);
		this.setMillisecond(Long.valueOf(mr.term(MILLISECOND).toString()));
		return this;
	}

}
