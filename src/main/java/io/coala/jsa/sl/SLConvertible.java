package io.coala.jsa.sl;

import io.asimov.model.sl.ASIMOVNode;
import io.coala.jsa.sl.SLConvertible;



public interface SLConvertible<T extends SLConvertible<?>>
{

	/** @return the semantic {@link ASIMOVNode} equivalent */
	<N extends ASIMOVNode<N>> N toSL();
	
	/**
	 * @param node the semantic {@link ASIMOVNode} to interpret
	 * @return the interpreted bean
	 */
	<N extends ASIMOVNode<N>> T fromSL(N node);
}