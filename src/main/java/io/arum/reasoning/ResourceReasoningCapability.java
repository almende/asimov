package io.arum.reasoning;

import io.coala.bind.Binder;
import io.coala.capability.BasicCapability;
import io.coala.capability.know.ReasoningCapability;
import io.coala.log.InjectLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import rx.Observable;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

import com.google.inject.Inject;

public class ResourceReasoningCapability  extends BasicCapability implements ReasoningCapability {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7532847081851738718L;
	
	@InjectLogger
	private Logger LOG;
	
	private Map<String,Set<Map<String, Object>>> kBase = new HashMap<String,Set<Map<String, Object>>>();
	
	@Inject
	protected ResourceReasoningCapability(Binder binder) {
		super(binder);
	}

	@Override
	public Belief toBelief(final Object javaObject, final Map<String, Object> keyValuePairs) {
		final Collection<Object> params = keyValuePairs.values();
		Belief b = new Belief(){
			
			/**
			 * 
			 */
			private static final long serialVersionUID = -8375188781021114858L;
			
			public boolean not = false;
			
			@Override
			public Belief negate() {
				if (not) {
					not = false;
				} else {
					not = true;
				}
				return this;
			};
			
			@Override
			public String toString() {
				String prefix = "";
				if (not) {
					prefix += ">>NOT>>";
				}
				prefix += javaObject.toString();
				if (params != null){
					String strParams = "";
					for (Object p : params) {
						strParams += "|~>" + p;
					}
					return prefix+" -> "+strParams;
				}
				return prefix;
			}
		};
		
		return b;
	}

	@Override
	public Belief toBelief(final Object javaObject, final Object... params) {
		Belief b = new Belief(){
			
			/**
			 * 
			 */
			private static final long serialVersionUID = -8375180781021114858L;
			
			public boolean not = false;
			
			@Override
			public Belief negate() {
				if (not) {
					not = false;
				} else {
					not = true;
				}
				return this;
			};
			
			@Override
			public String toString() {
				String prefix = "";
				if (not) {
					prefix += ">>NOT>>";
				}
				prefix += javaObject.toString();
				if (params != null){
					String strParams = "";
					for (Object p : params) {
						strParams += "|~>" + p;
					}
					return prefix+" -> "+strParams;
				}
				return prefix;
			}
		};
		
		return b;
	}

	@Override
	public Query toQuery(final Object javaObject, final Map<String, Object> keyValuePairs) {
		return new Query() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 4982765245208211093L;
			
			Belief b = toBelief(javaObject, keyValuePairs.values());
			
			@Override
			public Query negate() {
				b.negate();
				return this;
			}
			
			@Override
			public String toString() {
				return b.toString();
			}
			
		};
	}

	@Override
	public Query toQuery(final Object javaObject, final Object... params) {
		return new Query() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 4982765245208211093L;
			
			Belief b = toBelief(javaObject, params);
			
			@Override
			public Query negate() {
				b.negate();
				return this;
			}
			
			@Override
			public String toString() {
				return b.toString();
			}
			
		};
	}

	@Override
	public void addBeliefToKBase(Belief belief) {
		if (belief.toString().startsWith(">>NOT>>"))
			removeBeliefFromKBase(belief.negate());
		else
			kBase.put(belief.toString(),Collections.singleton(Collections.singletonMap(belief.toString(), (Object)belief)));
	}

	@Override
	public void removeBeliefFromKBase(Belief belief) {
		kBase.remove(belief.toString());
	}

	@Override
	public Observable<Map<String, Object>> queryToKBase(Query query) {
		Subject<Map<String,Object>,Map<String,Object>> result = ReplaySubject.create();
		Set<Map<String,Object>> queryResults = kBase.get(query.toString());
		int score = 0;
		if (queryResults != null)
			for (Map<String,Object> qr : queryResults) {
				if (LOG.isEnabledFor(Level.INFO)) LOG.info("REASONER:"+qr);
				result.onNext(qr);
				score++;
			}
		if (LOG.isEnabledFor(Level.INFO)) LOG.info("REASONER: score -> "+score);
		result.onNext(Collections.singletonMap("score",(Object) score));
		result.onCompleted();
		return result.asObservable();
	}

	@Override
	public Object getKBase() {
		return kBase;
	}

}
