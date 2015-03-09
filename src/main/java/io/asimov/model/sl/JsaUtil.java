package io.asimov.model.sl;

import io.asimov.reasoning.sl.NotNode;
import io.asimov.reasoning.sl.SLParsable;
import io.asimov.reasoning.sl.SLConvertible;
import io.coala.agent.AgentID;
import io.coala.exception.CoalaExceptionFactory;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.util.Util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

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
	<T extends ASIMOVNode<T>> T getCachedSLNode(final Object node) {
		T result = (T) cachedSLNode.get(node);
		if (result == null) {
			if (node instanceof SLConvertible) {
				result = ((SLConvertible<T>)node).toSL();
			} if (result == null && node instanceof SLParsable) {
				result = getNodeFromJsonNode(JsonUtil.fromJSON(node.toString()));
			} if (result == null && node instanceof JsonNode) {
				result = getNodeFromJsonNode((JsonNode)node);
			} if (result == null && node instanceof Number) {
				result = (T) SL.integer(((Number)node).longValue());
			} if (result == null && node instanceof String) {
				result = (T) SL.string(node.toString());
			}
			if (result != null) 
				cachedSLNode.put(node, result);
		}
		return (T) result;
	}
	
	
	@SuppressWarnings("unchecked")
	public <T extends ASIMOVNode<T>> T getNodeFromJsonNode(final JsonNode jsonNode) {
			if (jsonNode.path("type") == null)
				return (T) null;
			final String nodeType = jsonNode.path("type").textValue();
			try {
				if (nodeType.equals("FORMULA")) {
					return (T) JsonUtil.getJOM().treeToValue(jsonNode, ASIMOVFormula.class);
				} else if (nodeType.equals("AND")) {	
					return (T) JsonUtil.getJOM().treeToValue(jsonNode, ASIMOVAndNode.class);
				} else if (nodeType.equals("FORMULA")) {				
					return (T)  JsonUtil.getJOM().treeToValue(jsonNode, ASIMOVTerm.class);
				} else if (nodeType.equals("TERM")) {					
					return (T) JsonUtil.getJOM().treeToValue(jsonNode, ASIMOVStringTerm.class);
				} else if (nodeType.equals("STRING")) {					
					return (T) JsonUtil.getJOM().treeToValue(jsonNode, ASIMOVIntegerTerm.class);
				} else if (nodeType.equals("INTEGER")) {				
					return (T) JsonUtil.getJOM().treeToValue(jsonNode, NotNode.class);
				} else if (nodeType.equals("NOT")) {				
					return (T) JsonUtil.getJOM().treeToValue(jsonNode, ASIMOVFormula.class);
				} else if (nodeType.equals("FUNCTION")) {					
					return (T) JsonUtil.getJOM().treeToValue(jsonNode, ASIMOVFormula.class);
				}
			} catch (JsonProcessingException e) {
				LOG.error("Failed to parse JSON to SL",e);
			}	
			return (T) null;
	}
	
	public <T extends ASIMOVNode<T>> T instantiateAsSL(Object javaObject){
		T result = null;
		result = getCachedSLNode(javaObject);
		if (result == null)
			CoalaExceptionFactory.VALUE_NOT_ALLOWED.create(javaObject, "Failed to convert JavaType to SL Node.");
		return (T)result;
	}
	
	public ASIMOVTerm getTermForAgentID(final AgentID agentID){
		return getCachedSLNode(SL.string(agentID.toString()));
	}

	

}
