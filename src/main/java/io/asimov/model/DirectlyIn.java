package io.asimov.model;

import io.asimov.model.sl.ASIMOVTerm;

import javax.persistence.Entity;

import org.eclipse.persistence.nosql.annotations.NoSql;

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
	public static ASIMOVTerm PATTERN = new ASIMOVTerm().withName(TERM_NAME)
			.instantiate(DELAY,  null)
			.instantiate(SOURCE_BODY, null);
	
	protected ASIMOVTerm getPattern(){
		return PATTERN;
	}

	
}
