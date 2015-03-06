package io.coala.jsa.sl;

import io.asimov.model.sl.ASIMOVAndNode;
import io.asimov.model.sl.ASIMOVFormula;
import io.asimov.model.sl.ASIMOVNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class KBase implements List<ASIMOVNode<?>> {
	
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
		// TODO Auto-generated method stub
		return kbase.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
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
		return kbase.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return kbase.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return kbase.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends ASIMOVNode<?>> c) {
		return kbase.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends ASIMOVNode<?>> c) {
		return kbase.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
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
		kbase.add(index,element);
	}

	@Override
	public ASIMOVNode<?> remove(int index) {
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
	
	public static Map<String,Object> matchNode(Object formulaObject, Object factObject) {
		if ((formulaObject instanceof ASIMOVNode<?>) == false)
		{
			if ((factObject instanceof ASIMOVNode<?>) == false)
			{
				if (formulaObject.equals(factObject))
					return new HashMap<String,Object>();
				else
					return null;
			} else {
				return null;
			}
		}
		
		ASIMOVNode<?> formula = (ASIMOVNode<?>)formulaObject;
		ASIMOVNode<?> fact = (ASIMOVNode<?>)factObject;
		boolean anyMatch = false;
		final Map<String,Object> result = new HashMap<String, Object>();
		if (formulaObject instanceof ASIMOVAndNode) {
			ASIMOVAndNode andFormula = (ASIMOVAndNode)formulaObject;
			for (String andKey : andFormula.getKeys()) {
				final Map<String,Object> hornResult = matchNode(andFormula.getPropertyValue(andKey), factObject);
				if (hornResult == null)
					return null;
				for (final String hornKey : hornResult.keySet())
					result.put(hornKey, hornResult.get(hornKey));
			}
			anyMatch = true;
		} else if (formula.getNodeType().equals(fact.getNodeType()) && formula.getName().equals(fact.getName())) {
			final Set<String> matchKeys = new HashSet<String>();
			boolean matched = true;
			for (final String key : formula.getKeys()) {
				if (formula.getPropertyValue(key) == null) { 
					matchKeys.add(key);
				} else {
					Object formulaProperty = formula.getPropertyValue(key);
					Object factProperty = fact.getPropertyValue(key);
					
					Map<String,Object> childMatches = matchNode(formulaProperty, factProperty);
					if (childMatches == null) {
						matched = false;
						break;
					} else {
						for (final String childMatchKey : childMatches.keySet()) {
							result.put(childMatchKey, childMatches.get(childMatchKey));
						}
					}
				}
					
			}
			if (matched || (formula instanceof NotNode && !matched)) {
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

	public Set<Map<String,Object>> query(ASIMOVFormula query) {
		boolean matched = false;
		Set<Map<String,Object>> queryResult = new HashSet<Map<String, Object>>();
		for (ASIMOVNode<?> node : this.kbase) {
			if (node.getNodeType().equals("FORMULA")) {
				Map<String,Object> row = matchNode(query,node);
				if (row != null) {
					matched = true;
					queryResult.add(row);
				}
			}
		}
		if (matched)
			return queryResult;
		else
			return null; // FALSE
	}

}
