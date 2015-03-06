package io.asimov.model.sl;

import java.util.Collections;
import java.util.Set;

public class ASIMOVIntegerTerm extends ASIMOVTerm {
	
	
	public ASIMOVIntegerTerm(){
		super();
	}
	
	@Override
	public Set<String> getKeys() {
		return Collections.singleton("integerValue");
	}

	@Override
	public Object getPropertyValue(String key) {
		if (!key.equals("integerValue"))
			throw new IllegalStateException("ASIMOVStringTerm is a primitive node only 'value' can be requested");
		return integerValue;
	}
	
	@Override
	public String getNodeType() {
		return "INTEGER";
	}
	
	public ASIMOVIntegerTerm withValue(final long integer){
		this.integerValue = integer;
		return this;
	}
	
	@Override
	public String toString(){
		return this.toJSON();
	}

	
}
