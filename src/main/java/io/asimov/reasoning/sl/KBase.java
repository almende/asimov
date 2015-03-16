package io.asimov.reasoning.sl;

import io.asimov.model.sl.ASIMOVAndNode;
import io.asimov.model.sl.ASIMOVFormula;
import io.asimov.model.sl.ASIMOVFunctionNode;
import io.asimov.model.sl.ASIMOVIntegerTerm;
import io.asimov.model.sl.ASIMOVNode;
import io.asimov.model.sl.ASIMOVStringTerm;
import io.asimov.model.sl.ASIMOVNotNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;


public class KBase implements List<ASIMOVNode<?>> {
	
	private static boolean showProgressOnStdOut = false;

	private static boolean showReasoningStdErr = false;

	
	
	List<ASIMOVNode<?>> kbase;
	
	public KBase() {
		kbase = new ArrayList<ASIMOVNode<?>>();
	}

	@Override
	public int size() {
		return kbase.size();
	}

	@Override
	public boolean isEmpty() {
		return kbase.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return kbase.contains(o);
	}

	@Override
	public Iterator<ASIMOVNode<?>> iterator() {
		return kbase.iterator();
	}

	@Override
	public Object[] toArray() {
		return kbase.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return kbase.toArray(a);
	}

	@Override
	public boolean add(ASIMOVNode<?> e) {
		if (showProgressOnStdOut) System.out.print('+');
		if (!kbase.contains(e))
			return kbase.add(e);
		else
			return false;
	}

	@Override
	public boolean remove(Object o) {
		if (showProgressOnStdOut) System.out.print('-');
		return kbase.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return kbase.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends ASIMOVNode<?>> c) {
		for (int i =0 ; i <c.size(); i++)
			if (showProgressOnStdOut) System.out.print('+');
		return kbase.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends ASIMOVNode<?>> c) {
		for (int i =0 ; i <c.size(); i++)
			if (showProgressOnStdOut) System.out.print('+');
		return kbase.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		for (int i =0 ; i < c.size(); i++)
			if (showProgressOnStdOut) System.out.print('-');
		return kbase.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return kbase.retainAll(c);
	}

	@Override
	public void clear() {
		kbase.clear();
	}

	@Override
	public ASIMOVNode<?> get(int index) {
		return kbase.get(index);
	}

	@Override
	public ASIMOVNode<?> set(int index, ASIMOVNode<?> element) {
		return kbase.set(index, element);
	}

	@Override
	public void add(int index, ASIMOVNode<?> element) {
		if (showProgressOnStdOut) System.out.print('+');	
		kbase.add(index,element);
	}

	@Override
	public ASIMOVNode<?> remove(int index) {
		if (showProgressOnStdOut) System.out.print('-');	
		return kbase.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return kbase.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return kbase.lastIndexOf(o);
	}

	@Override
	public ListIterator<ASIMOVNode<?>> listIterator() {
		return kbase.listIterator();
	}

	@Override
	public ListIterator<ASIMOVNode<?>> listIterator(int index) {
		return kbase.listIterator(index);
	}

	@Override
	public List<ASIMOVNode<?>> subList(int fromIndex, int toIndex) {
		return kbase.subList(fromIndex, toIndex);
	}
	
	public static Object parsePrimitives(final Object inFormulaObject) {
		final ASIMOVNode<?> formulaObject = ((ASIMOVNode<?>)inFormulaObject);
		final String nodeType = formulaObject.getNodeType();
		Object result;
		if (nodeType.equals("STRING")) {
			result = ((ASIMOVStringTerm)formulaObject).stringValue;
		} else if (nodeType.equals("INTEGER")) {
			result = new Long(((ASIMOVIntegerTerm)formulaObject).integerValue);
		} else {
			result = inFormulaObject;
		}
		return result;
	}
	
	public Map<String,Object> matchNode(final Object inFormulaObject, final Object inFactObject) {
		if (inFormulaObject == null || inFactObject == null)
			return Collections.emptyMap();
		if (showProgressOnStdOut) System.out.print('.');	
		Object formulaObject = parsePrimitives(inFormulaObject);
		
		Object factObject = parsePrimitives(inFactObject);
		
		if ((formulaObject instanceof ASIMOVNode<?>) == false)
		{
			if ((factObject instanceof ASIMOVNode<?>) == false)
			{
				if (formulaObject.equals(factObject)) {
					return new HashMap<String,Object>();
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
		if ((factObject instanceof ASIMOVNode<?>) == false)
		{
			return null;
		}
		
		ASIMOVNode<?> formula = (ASIMOVNode<?>)formulaObject;
		ASIMOVNode<?> fact = (ASIMOVNode<?>)factObject;
		boolean anyMatch = false;
		final Map<String,Object> result = new HashMap<String, Object>();
		if (formulaObject instanceof ASIMOVNotNode) {
			ASIMOVFormula f = ((ASIMOVNotNode) formulaObject).negate();
			for (final ASIMOVNode<?> aFact : this.kbase) {
				if (matchNode(f, aFact) != null) {
					if (showProgressOnStdOut) System.out.print("<not=false>");
					return null;
				} else
					anyMatch = true;
			}
			if (anyMatch)
				if (showProgressOnStdOut) System.out.print("<not=true>");
		} else if (formulaObject instanceof ASIMOVAndNode) {
			final ASIMOVAndNode andFormula = ((ASIMOVAndNode)formulaObject);
			final Set<String> andKeys = new HashSet<String>();
			andKeyLoop: for (final String andKey : andFormula.getKeys()) {
					for (final ASIMOVNode<?> aFact : this.kbase) {
						{
							if (aFact == factObject) {
								final Map<String,Object> hornResult = matchNode(andFormula.getPropertyValue(andKey), factObject);
								if (hornResult != null) {
									if (showProgressOnStdOut) System.out.print("<&"+andFormula.getKeys()+"."+andKeys.size()+">");
									for (final String hornKey : hornResult.keySet()) {
										result.put(hornKey, hornResult);
									}
									andKeys.add(andKey);
								}
							} else {
								final Map<String,Object> otherHornResult = matchNode(andFormula.getPropertyValue(andKey), aFact);
								if (otherHornResult != null) {
									if (showProgressOnStdOut) System.out.print("<&"+andFormula.getKeys()+"."+andKeys+">");
									for (final String hornKey : otherHornResult.keySet()) {
										result.put(hornKey, otherHornResult.get(hornKey));
									}
									andKeys.add(andKey);
								}
							}
							if (andKeys.containsAll(andFormula.getKeys())) {
								anyMatch = true;
								break andKeyLoop;
							}
						}
					}			
			}
			if (!anyMatch) {
				if (showProgressOnStdOut) System.out.print("<!&>");
				if (showProgressOnStdOut) System.out.flush();
				if (showReasoningStdErr) System.err.println("|- REASON = (AND) NOT KNOWN:"+andFormula.namedChildren);
				if (showReasoningStdErr) System.err.flush();
				return null;
			}
		} else if (formula.getNodeType().equals("FUNCTION")) {
			ASIMOVFunctionNode function = (ASIMOVFunctionNode)formula;
			if (showProgressOnStdOut) System.out.print("<f(x)>");
			for (final ASIMOVNode<?> aFact : this.kbase) 
			{
				if (function.eval(aFact))
					break;
			}
			Object r =  function.getPropertyValue(function.getResultKey());
			if (r != null) {
				anyMatch = true;
				if (showProgressOnStdOut) System.out.print("<"+function.getName()+"(x)=true>");
				result.put(function.getResultKey(),r);
			} else {
				if (showProgressOnStdOut) System.out.print("<"+function.getName()+"(x)=false>");
			}	
		} else if (formula.getNodeType().equals(fact.getNodeType()) && formula.getName().equals(fact.getName())) {
			final Set<String> matchKeys = new HashSet<String>();
			boolean matched = true;
			for (final String key : formula.getKeys()) {
				if (formula.getPropertyValue(key) == null) { 
					if (showProgressOnStdOut) System.out.print("<?>");
					matchKeys.add(key);
				} else {
					Object formulaProperty = formula.getPropertyValue(key);
					Object factProperty = fact.getPropertyValue(key);
					
					Map<String,Object> childMatches = matchNode(formulaProperty, factProperty);
					if (childMatches == null) {
						if (showProgressOnStdOut) System.out.print("<!>");
						matched = false;
						if (showReasoningStdErr) System.err.println("|- REASON = ("+formula.getNodeType()+") NOT KNOWN :"+formulaProperty);
						if (showReasoningStdErr) System.err.flush();
						break;
					} else {
						for (final String childMatchKey : childMatches.keySet()) {
							if (showProgressOnStdOut) System.out.print("<"+childMatchKey+".found>");
							result.put(childMatchKey, childMatches.get(childMatchKey));
						}
					}
				}
					
			} 
			if (matched) {
				if (showProgressOnStdOut) System.out.print("<ok>");
				anyMatch = true;
				for (final String key : matchKeys) {
					result.put(key,fact.getPropertyValue(key));
				}
			}
		} 
		if (anyMatch) {
			return result;
		} else {
			return null; // FALSE
		}
	}

	public Set<Map<String,Object>> query(ASIMOVNode<?> query) {
		if (showReasoningStdErr) System.err.println("-> QUERY  := "+query);
		boolean matched = false;
		Set<Map<String,Object>> queryResult = new HashSet<Map<String, Object>>();
		for (ASIMOVNode<?> node : this.kbase) {
			if (node.getNodeType().equals("FORMULA") || 
					node.getNodeType().equals("AND") ||
					node.getNodeType().equals("FUNCTION") || 
					node.getNodeType().equals("NOT")) {
				Map<String,Object> row = matchNode(query,node);
				if (showReasoningStdErr) System.err.println((row != null) ? "|-TRUE" : "|-(FALSE)" + node);
				if (row != null)
					if (showReasoningStdErr) System.err.println("|-(WITH)"+row);
				if (row != null) {
					matched = true;
					queryResult.add(row);
				}
			}
		}
		if (matched) {
			if (showReasoningStdErr) System.err.println("-> TRUE  := "+query);
			return queryResult;
		} else {
			if (showReasoningStdErr) System.err.println("-> FALSE := "+query);
			return null; // FALSE
		}
	}

}
