/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.asimov.model.sl;

import io.asimov.model.ResourceAllocation;
import io.coala.jsa.sl.SLConvertible;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.tools.SL;


/**
 *
 * @author suki
 */
public interface ASIMOVAgents {
	
	
    
	/** */
	public static final String BELONGS_TO_PROCESS_FORMULA_NAME = "BELONGS_TO_PROCESS";
    
    /** */
	public static final String PROCESS_NAME = "PROCESS_NAME";
	
	/** */
	public static final String PROCESS_PROPERTY = "PROCESS_PROPERTY";

	/** */
	public static final Formula BELONGS_TO_PROCESS_FORMULA = SL.formula(String
			.format("(%s ??%s ??%s)", BELONGS_TO_PROCESS_FORMULA_NAME, PROCESS_PROPERTY,
					PROCESS_NAME));

	
    public static final Term REQUEST_ALLOCATION_ACTION_TERM = SL.term(
			String.format("(ALLOCATE_RESOURCE :%s ??%s :%s ??%s :%s ??%s :%s ??%s)",
					ResourceAllocation.RESOURCE_REQUIREMENT_ID,ResourceAllocation.RESOURCE_REQUIREMENT_ID,
					PROCESS_NAME,PROCESS_NAME
					)
			);
      
}
