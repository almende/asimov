package io.asimov.reasoning.sl;


import java.io.Serializable;

// Type for requirements
public class SLParsableSerializable implements SLParsable, Serializable {

	/** */
	private static final long serialVersionUID = 1L;
	
	protected SLParsableSerializable() {
		// zero argument constructor;
	}
	
	public SLParsableSerializable(final String string) {
		this.value = string;
	}
	
	private String value;

	/**
	 * @return the value
	 */
	public String getValue()
	{
		return this.value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(final String value)
	{
		this.value = value;
	}
	
	@Override
	public String toString(){
		return value;
	}
	
	@Override
	public boolean equals(Object o) {
		return toString().equals(o.toString());
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
}