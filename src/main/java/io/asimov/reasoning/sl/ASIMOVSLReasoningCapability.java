package io.asimov.reasoning.sl;

import io.asimov.agent.resource.GenericResourceManagementWorld;
import io.asimov.microservice.negotiation.messages.AvailabilityReply;
import io.asimov.model.sl.ASIMOVAndNode;
import io.asimov.model.sl.ASIMOVFormula;
import io.asimov.model.sl.ASIMOVNode;
import io.asimov.model.sl.ASIMOVTerm;
import io.asimov.model.sl.ASIMOVSLUtil;
import io.asimov.model.sl.ASIMOVNotNode;
import io.asimov.model.sl.SL;
import io.coala.bind.Binder;
import io.coala.capability.BasicCapability;
import io.coala.capability.configure.ConfiguringCapability;
import io.coala.capability.know.ReasoningCapability;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.exception.CoalaException;
import io.coala.exception.CoalaExceptionFactory;
import io.coala.log.InjectLogger;
import io.coala.log.LogUtil;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observable;
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
	
	private static ASIMOVSLUtil ASIMOVSL = ASIMOVSLUtil.getInstance();
	
	
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
		result = getSLForObject(javaObject, keyValuePairs);
		return result;
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T extends ASIMOVNode<T>> T getTermForObject(Object javaObject, Map<String,Object> keyValuePairs) throws Exception {
		return (T) getSLForObject(javaObject, toParameters(keyValuePairs));
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
		result = getSLForObject(javaObject, params);
		return result;
	}
	
	public void debug(boolean includeStackTrace, final String... line) {
		try {
			if (getBinder().inject(ConfiguringCapability.class).getProperty("debugResources").getJSON(new ArrayList<String>()).contains(getID().getOwnerID().getValue())) {
				String message = getBinder().inject(ReplicatingCapability.class).getTime().toString()+" for "+getID()+" DEBUG: ";
				for (String part : line) {
					message += part;
				}
				PrintWriter pw = new PrintWriter(System.out);
				pw.flush();
				pw.println(message);
				if (includeStackTrace) {
					final IllegalStateException e 
						= new IllegalStateException("NO ERROR but DEBUG INFO");
					e.printStackTrace(pw);
				}
				pw.flush();
			}
		} catch (CoalaException e) {
			LOG.error("Failed to read property",e);
		}
		
	}
	
	/**
	 * 
	 * @param javaObject
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ASIMOVNode<T>> T getTermForObject(Object javaObject, Object... params) throws Exception {
		return (T) getSLForObject(javaObject, params);
	}
	
	public void addNodeToKBase(final Object javaObject, final Object... params){
		try
		{
			ASIMOVNode<?> node = getFormulaForObject(javaObject, params);
			debug(
					false,
					" add belief: ",
					node.toJSON()
			);
			kbase.add(node);
		} catch (Exception e)
		{
			CoalaExceptionFactory.VALUE_NOT_ALLOWED.create("Could not instantiate and assert SL formula.",javaObject,params,e);
		}
	}
	
	public void retractNodeFromKBase(final Object javaObject, final Object... params){
		try
		{
			ASIMOVNode<?> node = getFormulaForObject(javaObject, params);
			debug(
					true,
					" remove belief: ",
					node.toJSON()
			);
			kbase.remove(node);
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
				debug(
						false,
						" |-- FALSE : ",
						query.toJSON()
				);
				result.onNext(null);
			}
			else { 
				for (Map<String,Object> next : values) {
					debug(
							false,
							" |-- TRUE : ",
							query.toJSON(), 
							"\n |-- WITH : ",
							next.toString()
					);
					result.onNext(next);
				}
			}
			result.onCompleted();
			
			
			return result.asObservable();
		
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends ASIMOVNode<T>> T getSLForObject(Object javaObject, Map<String,Object> keyValuePairs) throws Exception  {
		return (T) getSLForObject(javaObject, toParameters(keyValuePairs));
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends ASIMOVNode<T>> T getSLForObject(Object javaObject, Object... params) throws Exception {
		if (javaObject == null)
			throw new NullPointerException("Can not instantiate null object.");
		T node;
		node = (T) ASIMOVSL.instantiateAsSL(javaObject);
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
					value = getSLForObject(params[i]);
			}
			if (!recursiveReplace(node, key, value))
				node = (T) SL.add(SL.instantiate(node),key,value);
			key = null;
			value = null;
		}
		return node;
	}

	@Override
	public KBase getKBase()
	{
		return kbase;
	}
	
	static boolean recursiveReplace(ASIMOVNode<?> sl, String searchKey, ASIMOVNode<?> value) {
		boolean replaced = false;
		if (value != null) {
			for (String nodeKey : sl.getKeys()) {
				if (nodeKey.equals(searchKey)) {
					sl.replace(nodeKey, value);
					replaced = true;
				} else {
					Object propertyValue = sl.getPropertyValue(nodeKey);
					if (propertyValue instanceof ASIMOVNode<?>) {
						boolean rReplace = recursiveReplace(((ASIMOVNode<?>)propertyValue),searchKey,value);
						replaced = rReplace || replaced;
					}
				}
			}
		}
		return replaced;
	}

	@Override
	public Belief toBelief(Object javaObject, Object... params)
	{
		ASIMOVNode<?> slNode;
		try
		{
			slNode = getSLForObject(javaObject, params);
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
		if (node instanceof ASIMOVNotNode) {
			log.info("Found not node in:"+belief.toString());
			ASIMOVFormula childNode = ((ASIMOVNotNode) node).negate();
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
