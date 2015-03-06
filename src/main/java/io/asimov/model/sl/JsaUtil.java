package io.asimov.model.sl;

import io.coala.agent.Agent;
import io.coala.agent.AgentID;
import io.coala.exception.CoalaExceptionFactory;
import io.coala.log.LogUtil;
import io.coala.util.Util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class JsaUtil implements Util
{
	
	private static final Map<Object,ASIMOVNode<?>> cachedSLNode = Collections
			.synchronizedMap(new HashMap<Object,ASIMOVNode<?>>());
	
	private static JsaUtil INSTANCE;
	
	private Logger LOG = LogUtil.getLogger(JsaUtil.class);
	

	private JsaUtil()
	{
		// utility methods only
	}
	
	public static synchronized JsaUtil getInstance() {
		if (INSTANCE == null)
			INSTANCE = new JsaUtil();
		return INSTANCE;
	}

	
	/**
	 * 
	 * {@link Converter} that converts the object to a SL node.
	 * 
	 * @date $Date: 2014-07-10 08:43:55 +0200 (Thu, 10 Jul 2014) $
	 * @version $Revision: 326 $
	 * @author <a href="mailto:suki@almende.org">suki</a>
	 *
	 * @param <T>
	 */
	public interface Converter<T extends ASIMOVNode<T>> {
		T convert(Object node);
	}
	
	@SuppressWarnings("unchecked")
	<T extends ASIMOVNode<T>> T getCachedSLNode(final Object node, final Converter<T> converter) {
		T result = (T) cachedSLNode.get(node);
		if (result == null) {
			result = converter.convert(node);
			cachedSLNode.put(node, result);
		}
		return (T) result;
	}
	
	
	@SuppressWarnings("unchecked")
	public <T extends ASIMOVNode<T>> T instantiateAsSL(Class<T> slNodeType, Object javaObject){
		T result = null;
		if (slNodeType.equals(ASIMOVTerm.class)) {
			if (javaObject instanceof AgentID)
				result = (T) getCachedSLNode(javaObject, new Converter<ASIMOVTerm>()
						{

					@Override
					public ASIMOVTerm convert(Object node)
					{
						return SL.string(node.toString());
					}
				});
			else
				result = (T) getCachedSLNode(javaObject, new Converter<ASIMOVTerm>()
						{

					@Override
					public ASIMOVTerm convert(Object node)
					{
						return SL.term(node.toString());
					}
				});
		} else if (slNodeType.equals(ASIMOVFormula.class)) {
			
				result = (T) getCachedSLNode(javaObject, new Converter<ASIMOVFormula>()
						{

					@Override
					public ASIMOVFormula convert(Object node)
					{
						return SL.formula(node.toString());
					}
				});
		}  
//		else if (slNodeType.equals(ASIMOVNode.class)) {
//			
//			result = (T) getCachedSLNode(javaObject, new Converter<ASIMOVNode>()
//					{
//
//				@Override
//				public ASIMOVNode<T> convert(Object node)
//				{
//					ASIMOVNode<T> slNode = null;
//					try
//					{
//						slNode = SLParser.getParser().parseTerm(node.toString(),true);
//					} catch (jade.semantics.lang.sl.parser.ParseException e)
//					{
//						try
//						{
//							slNode = SLParser.getParser().parseFormula(node.toString(),true);
//						} catch (jade.semantics.lang.sl.parser.ParseException e1)
//						{
//							LOG.error("Failed to create SL term",e);
//							LOG.error("Failed to create SL formula",e1);
//							return null;
//						}
//					}
//					return slNode;
//				}
//			});
//	   }  
		else
			CoalaExceptionFactory.VALUE_NOT_ALLOWED.create(javaObject, "Failed to convert JavaType to SL Node of type: "+slNodeType.getSimpleName());
		return (T)result;
	}
	
	public ASIMOVTerm getTermForAgentID(final AgentID agentID){
		return getCachedSLNode(agentID, new Converter<ASIMOVTerm>()
		{

			@Override
			public ASIMOVTerm convert(Object node)
			{
				return SL.string(node.toString());
			}
		});
	}

	

}
