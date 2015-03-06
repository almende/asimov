package io.asimov.model.sl;

import java.util.Collections;
import java.util.Set;

public class ASIMOVIntegerTerm extends ASIMOVTerm {
	
	long value;
	
	@Override
	public Set<String> getKeys() {
		return Collections.singleton("value");
	}

	@Override
	public Object getPropertyValue(String key) {
		if (!key.equals("value"))
			throw new IllegalStateException("ASIMOVStringTerm is a primitive node only 'value' can be requested");
		return value;
	}
	
	@Override
	public String getNodeType() {
		return "INTEGER";
	}
	
	public ASIMOVIntegerTerm withValue(final long string){
		this.value = string;
		return this;
	}
	
}
