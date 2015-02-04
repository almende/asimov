package io.asimov.model;

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
public class DirectlyContains extends ConnectionTo
{

	/** */
	private static final long serialVersionUID = 1L;

	public static String TERM_NAME = "DIRECTLY_CONTAINS";
	
	/** Pattern for a {@link DirectlyContains} {@link Term} representation */
	public static Term PATTERN = SL.term(String.format(
			"(%s :%s ??%s :%s ??%s)", TERM_NAME, 
			DELAY,  DELAY, TARGET_BODY, TARGET_BODY));
	
	@Override
	protected Term getPattern(){
		return PATTERN;
	}

	
}
