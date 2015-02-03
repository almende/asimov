package io.asimov.agent.resource;

import javax.persistence.Entity;

import org.eclipse.persistence.nosql.annotations.NoSql;

import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.tools.SL;

/**
 * 
 * @author suki
 */
@Entity
@NoSql(dataType = "containers")
public class DirectlyIn extends ConnectionFrom
{

	/** */
	private static final long serialVersionUID = 1L;


	public static String TERM_NAME = "DIRECTLY_IN";
	
	/** Pattern for a {@link DirectlyIn} {@link Term} representation */
	public static Term PATTERN = SL.term(String.format(
			"(%s :%s ??%s :%s ??%s)", TERM_NAME, 
			DELAY,  DELAY, SOURCE_BODY, SOURCE_BODY));
	
	@Override
	protected Term getPattern(){
		return PATTERN;
	}
	
}
