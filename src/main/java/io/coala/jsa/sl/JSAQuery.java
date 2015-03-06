package io.coala.jsa.sl;

import io.asimov.model.sl.ASIMOVFormula;
import io.asimov.model.sl.ASIMOVNode;
import io.coala.capability.know.ReasoningCapability.Query;

/**
 * {@link JSAQuery}
 * 
 * @version $Revision: 237 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
public class JSAQuery implements Query
{

	/** */
	private static final long serialVersionUID = 1L;

	protected final ASIMOVNode<?> node;

	public JSAQuery(ASIMOVNode<?> node)
	{
		super();
		this.node = node;
	}

	public final ASIMOVNode<?> getNode()
	{
		return node;
	}

	@Override
	public String toString()
	{
		return node.toString();
	}

	/** @see com.almende.coala.service.reasoner.ReasonerService.Query#negate() */
	@Override
	public Query negate()
	{
		return new JSAQuery(new NotNode((ASIMOVFormula) getNode()));
	}

}