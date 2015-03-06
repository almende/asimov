package io.asimov.reasoning.sl;

import io.arum.model.resource.DirectoryLookup.Result;
import io.asimov.model.sl.ASIMOVAndNode;
import io.asimov.model.sl.ASIMOVFormula;
import io.asimov.model.sl.ASIMOVFunctionNode;
import io.asimov.model.sl.ASIMOVNode;
import io.asimov.model.sl.ASIMOVTerm;
import io.asimov.model.sl.JsaUtil;
import io.asimov.model.sl.SL;
import io.coala.bind.Binder;
import io.coala.capability.BasicCapability;
import io.coala.capability.know.ReasoningCapability;
import io.coala.exception.CoalaExceptionFactory;
import io.coala.json.JsonUtil;
import io.coala.log.InjectLogger;
import io.coala.log.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

/**
 * {@link ReasonerService}
 * 
 * @version $Revision: 327 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public class ASIMOVSLReasoningCapability extends BasicCapability implements ReasoningCapability//<KBase>
{
	
	/** */
	private static final long serialVersionUID = -6239797259870529710L;
	
	private final KBase kbase;
	
	private static final Logger LOG = LogUtil
			.getLogger(ASIMOVSLReasoningCapability.class);;
	
	@InjectLogger
	private Logger log;
	
	private static JsaUtil JSA = JsaUtil.getInstance();
	
	
	public static enum Ontology {
		FORMULA(ASIMOVFormula.class),
		TERM(ASIMOVTerm.class),
		AGENTID(ASIMOVTerm.class),
		;
		
		private final Class<?> clazz;
		
		private <T extends ASIMOVNode<T>> Ontology(Class<T> type) {
			this.clazz = type;
		}
		
		/**
		 * @return the serviceType
		 */
		@SuppressWarnings("unchecked")
		public <T extends ASIMOVNode<T>> Class<T> getType()
		{
			return (Class<T>)this.clazz;
		}
		
		
	}
	
	public static Ontology SLTypes;

	@Inject
	protected ASIMOVSLReasoningCapability(final Binder binder)
	{
		super(binder);
		
		kbase = new KBase();
	}
	
	private static Object[] toParameters(Map<String, Object> args)
	{
		List<Object> params = new ArrayList<Object>();
		for (Entry<String, Object> entry : args.entrySet()) {
			params.add(entry.getKey());
			params.add(entry.getValue());
		}
		return params.toArray();
	}
			
	
	public static ASIMOVNode<?> getFormulaForObject(Object javaObject, Map<String,Object> keyValuePairs) throws Exception {
		ASIMOVNode<?> result;
		try {
		result = getSLForObject(ASIMOVFormula.class, javaObject, keyValuePairs);
		} catch (ClassCastException ce) {
			try {
				result = getSLForObject(ASIMOVAndNode.class, javaObject, keyValuePairs);
			} catch (ClassCastException ce2) {
				result = getSLForObject(ASIMOVFunctionNode.class, javaObject, keyValuePairs);
				ce.printStackTrace();
				ce2.printStackTrace();
			}
		}
		return result;
	}
	
	
	public static ASIMOVTerm getTermForObject(Object javaObject, Map<String,Object> keyValuePairs) throws Exception {
		return getSLForObject(ASIMOVTerm.class, javaObject, toParameters(keyValuePairs));
	}
	
	/**
	 * 
	 * @param javaObject
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static ASIMOVNode<?> getFormulaForObject(Object javaObject, Object... params) throws Exception {
		ASIMOVNode<?> result;
		try {
		result = getSLForObject(ASIMOVFormula.class, javaObject, params);
		} catch (Exception ce) {
			try {
				result = getSLForObject(ASIMOVAndNode.class, javaObject, params);
			} catch (Exception ce2) {
				result = getSLForObject(ASIMOVFunctionNode.class, javaObject, params);
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param javaObject
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static ASIMOVTerm getTermForObject(Object javaObject, Object... params) throws Exception {
		return getSLForObject(ASIMOVTerm.class, javaObject, params);
	}
	
	public void addNodeToKBase(final Object javaObject, final Object... params){
		try
		{
			kbase.add(getFormulaForObject(javaObject, params));
		} catch (Exception e)
		{
			CoalaExceptionFactory.VALUE_NOT_ALLOWED.create("Could not instantiate and assert SL formula.",javaObject,params,e);
		}
	}
	
	public void retractNodeFromKBase(final Object javaObject, final Object... params){
		try
		{
			kbase.remove(getFormulaForObject(javaObject, params));
		} catch (Exception e)
		{
			CoalaExceptionFactory.VALUE_NOT_ALLOWED.create("Could not instantiate and retract SL formula.",javaObject,params,e);
		}
	}
	
	/**
	 * Observer for the queryResults
	 * @param javaObject
	 * @param params
	 * @return
	 */
	public Observable<Map<String, Object>> queryToKBase(final Object javaObject, final Object... params){
		
		if (javaObject == null)
			throw new NullPointerException("A query must always be about something, querying null is not possible.");
			// temporarly disabled caching for debugging purposes
			//if (subjects.containsKey(javaObject))
			//	return (Observable<Map<String, Object>>) subjects.get(javaObject).asObservable();
		
			final Subject<Map<String, Object>,Map<String, Object>> result;
			result = ReplaySubject.create();
			
			ASIMOVNode<?> query = null;
			try
			{
				query = ASIMOVSLReasoningCapability.getFormulaForObject(
						javaObject,
						params
						);
			} catch (Exception e1)
			{
				log.error("Query "+javaObject+" could not be created.", e1);
				return null;
			}
			LOG.info("QUERY:"+query.toString());
			Set<Map<String,Object>> values = null;
			
			
			
			values = this.getKBase().query(query);
			if (values == null) {
				result.onNext(null);
			}
			else { 
				for (Map<String,Object> next : values) {
								result.onNext(next);
				}
			}
			result.onCompleted();
			
			
			return result.asObservable();
		
	}
	
	public static <T extends ASIMOVNode<T>> T getSLForObject(Class<T> slNodeType, Object javaObject, Map<String,Object> keyValuePairs) throws Exception  {
		return getSLForObject(slNodeType, javaObject, toParameters(keyValuePairs));
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends ASIMOVNode<T>> T getSLForObject(Class<T> slNodeType, Object javaObject, Object... params) throws Exception {
		if (javaObject == null)
			throw new NullPointerException("Can not instantiate null object.");
		T node;
		if (javaObject instanceof SLConvertible) {
			node = ((SLConvertible<?>)javaObject).toSL();
		} else if (javaObject instanceof SLParsable || javaObject instanceof ASIMOVNode<?>) {
		LOG.info("Parsing SL parsable string"+javaObject);
		if (slNodeType.equals(ASIMOVNode.class)) {
			final String nodeType = JsonUtil.toJSON(javaObject).path("type").asText();
			if (nodeType.equals("NOT") || nodeType.equals("FORMULA"))
				node = (T) JSA.instantiateAsSL(ASIMOVFormula.class, javaObject);
			else if (nodeType.equals("AND")) {
				node = (T) JSA.instantiateAsSL(ASIMOVAndNode.class, javaObject);
			} else if (nodeType.equals("FUNCTION")) {
				node = (T) JSA.instantiateAsSL(ASIMOVFunctionNode.class, javaObject);
			} else {
				LOG.error("Failed to parse SL Node type: "+nodeType+" for "+javaObject,new Exception());
				return null;
			}
		} else {
			node = JSA.instantiateAsSL(slNodeType, javaObject);
		}
		if (params.length % 2 != 0)
			throw new Exception("Invalid amount of paramters, expected key value pairs");
		String key = null;
		ASIMOVNode<?> value = null;
		for (int i = 0; i < params.length; i++) {
			if (key == null) {
				if (params[i] instanceof String)
					key = (String)params[i];
				else
					throw new Exception("Invalid argument "+params[i].getClass()+" ["+params[i]+"], expected key.");
				continue;
			}
			if (value == null) {
				if ((params[i] instanceof Belief))
					value = (ASIMOVNode<?>)((ASIMOVSLBelief)params[i]).getNode();
				else if (params[i] instanceof ASIMOVNode)
					value = (ASIMOVNode<?>)params[i];
				else
					value = getSLForObject(ASIMOVNode.class, params[i]);
			}
			node = (T) SL.instantiate(node,key,value);
			key = null;
			value = null;
		}
		} else {
			LOG.info("Converting string to SLString: "+javaObject);
			node = (T) SL.string(javaObject.toString());
		}
		return node;
	}

	@Override
	public KBase getKBase()
	{
		return kbase;
	}

	@Override
	public Belief toBelief(Object javaObject, Object... params)
	{
		ASIMOVNode<?> slNode;
		try
		{
			slNode = getSLForObject(ASIMOVNode.class, javaObject, params);
		} catch (Exception e)
		{
			log.error("Failed to create SL Node", e);
			return null;
		}
		return new ASIMOVSLBelief(slNode);
	}

	@Override
	public Query toQuery(
			Object javaObject, Object... params)
	{
		try
		{
			return new ASIMOVSLQuery(getFormulaForObject(javaObject, params));
		} 
		 catch (ClassCastException ce)
			{
			 	log.warn("The String: \""+javaObject+"\" is not a valid query.",ce);
				return null;
			}
		catch (Exception e)
		{
			log.error("Failed to create SL Node", e);
			return null;
		}
	}

	@Override
	public void addBeliefToKBase(Belief belief)
	{
		ASIMOVNode<?> node = ((ASIMOVSLBelief)belief).getNode();
		if (node instanceof NotNode) {
			log.info("Found not node in:"+belief.toString());
			ASIMOVFormula childNode = ((NotNode) node).negate();
			log.info("Removing node from KBASE:"+childNode.toString());
			removeBeliefFromKBase(new ASIMOVSLBelief((ASIMOVFormula)childNode));
		}
		else if (node instanceof ASIMOVAndNode) {
			log.info("Found and node in:"+belief.toString());
			for (final String childKey : node.getKeys()) {
				ASIMOVFormula childNode = (ASIMOVFormula)node.getPropertyValue(childKey);
				log.info("Adding node to KBASE:"+childNode.toString());
				addBeliefToKBase(new ASIMOVSLBelief((ASIMOVFormula)childNode));
			}
		}
		else 
			addNodeToKBase(((ASIMOVSLBelief)belief).getNode());
	}

	@Override
	public void removeBeliefFromKBase(Belief belief)
	{
		ASIMOVNode<?> node = ((ASIMOVSLBelief)belief).getNode();
		if (node instanceof ASIMOVAndNode) {
			log.info("Found and node in:"+belief.toString());
			for (final String childKey : node.getKeys()) {
				ASIMOVFormula childNode = (ASIMOVFormula)node.getPropertyValue(childKey);
				log.info("Removing node from KBASE:"+childNode.toString());
				removeBeliefFromKBase(new ASIMOVSLBelief((ASIMOVFormula)childNode));
			}
		}
		retractNodeFromKBase((((ASIMOVSLBelief)belief).getNode()));
	}

	@Override
	public Observable<Map<String, Object>> queryToKBase(Query query)
	{
		return queryToKBase((((ASIMOVSLQuery)query).getNode()));
	}

	@Override
	public Belief toBelief(
			Object javaObject, Map<String, Object> keyValuePairs)
	{
		return toBelief(javaObject, toParameters(keyValuePairs));
	}

	@Override
	public Query toQuery(
			Object javaObject, Map<String, Object> keyValuePairs)
	{
		return toQuery(javaObject, toParameters(keyValuePairs));
	}
	
}
