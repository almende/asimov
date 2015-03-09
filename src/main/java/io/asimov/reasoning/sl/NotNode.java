package io.asimov.reasoning.sl;



import io.asimov.model.sl.ASIMOVFormula;

public class NotNode extends ASIMOVFormula {
	
	
	public NotNode(ASIMOVFormula formula){
		this.formulaProperties = formula.formulaProperties;
		this.name = formula.name;
		this.negate();
		this.type = getNodeType();
	}
	
	public NotNode(){
		super();
		super.isNotNode = true;
		super.type = super.getNodeType();
	}
}
