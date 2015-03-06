package io.coala.jsa.sl;

import io.asimov.model.sl.ASIMOVFormula;
import io.asimov.model.sl.ASIMOVNode;
import io.coala.capability.know.ReasoningCapability.Belief;

/**
 * {@link JSABelief}
 * 
 * @version $Revision$
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public class JSABelief implements Belief {
	
	/** */
	private static final long serialVersionUID = 1L;
	
	protected final ASIMOVNode<?> node;
	
	private String stringValue;
	
	public JSABelief(ASIMOVNode<?> node) {
		super();
		this.node = node;
	}
	
	public final ASIMOVNode<?> getNode() {
		return node;
	}
	
	@Override
	public String toString(){
		if (stringValue == null)
			stringValue = node.toString();
		return stringValue;
	}
	
	@Override
	public boolean equals(Object o) {
		return toString().equals(o.toString());
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public Belief negate()
	{
		return new JSABelief(new NotNode((ASIMOVFormula)getNode()));	
	}
}