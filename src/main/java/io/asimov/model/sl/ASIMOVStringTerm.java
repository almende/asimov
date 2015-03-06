package io.asimov.model.sl;

import java.util.Collections;
import java.util.Set;

public class ASIMOVStringTerm extends ASIMOVTerm {
	
	public ASIMOVStringTerm(){
		super();
	}
	
	@Override
	public Set<String> getKeys() {
		return Collections.singleton("stringValue");
	}

	@Override
	public Object getPropertyValue(String key) {
		if (!key.equals("stringValue"))
			throw new IllegalStateException("ASIMOVStringTerm is a primitive node only 'value' can be requested");
		return stringValue;
	}
	
	@Override
	public String getNodeType() {
		return "STRING";
	}
	
	public ASIMOVStringTerm withValue(final String string){
		this.stringValue = string;
		return this;
	}
	
	@Override
	public String toString(){
		return this.toJSON();
	}

	
}
