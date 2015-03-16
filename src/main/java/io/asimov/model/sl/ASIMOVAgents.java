/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.asimov.model.sl;

import io.asimov.model.ResourceAllocation;
import io.asimov.reasoning.sl.SLConvertible;


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
	public static final ASIMOVFormula BELONGS_TO_PROCESS_FORMULA = new ASIMOVFormula()
			.withName(BELONGS_TO_PROCESS_FORMULA_NAME)
			.instantiate(PROCESS_PROPERTY,null)
			.instantiate(PROCESS_NAME,null);

	
    public static final ASIMOVTerm REQUEST_ALLOCATION_ACTION_TERM = new ASIMOVTerm()
    		.withName("ALLOCATE_RESOURCE")
    			.instantiate(ResourceAllocation.RESOURCE_REQUIREMENT_ID,null)
    			.instantiate(PROCESS_NAME,null);
      
}
