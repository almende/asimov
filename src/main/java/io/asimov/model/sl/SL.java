package io.asimov.model.sl;

public class SL {
	
		public static ASIMOVNode<?> instantiate(final ASIMOVNode<?> node,final String key, final ASIMOVNode<?> value) {
		return node.instantiate(key, value);
	}
		
	public static ASIMOVTerm integer(final long integer) {
		return ((ASIMOVIntegerTerm)new ASIMOVIntegerTerm().withName("integer"))
				.withValue(integer);
	}
		
	
	public static ASIMOVTerm string(final String string) {
		return ((ASIMOVStringTerm)new ASIMOVStringTerm().withName("string"))
				.withValue(string);
	}
	
	public static ASIMOVTerm term(final String jsonString) {
		return new ASIMOVTerm().fromJSON(jsonString);
	}
	
	public static ASIMOVFormula formula(final String jsonString) {
		return new ASIMOVFormula().fromJSON(jsonString);
	}
}
