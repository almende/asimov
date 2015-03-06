package io.coala.jsa.sl;



import io.asimov.model.sl.ASIMOVFormula;

public class NotNode extends ASIMOVFormula {
	
	public NotNode(ASIMOVFormula formula){
		this.fromJSON(formula.negate().toJSON());
	}
	
	public NotNode(){
		super.isNotNode = true;
	}
}
