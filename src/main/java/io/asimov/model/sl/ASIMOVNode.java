package io.asimov.model.sl;

import java.util.Set;

import io.asimov.model.XMLConvertible;
import io.coala.json.JSONConvertible;

public interface ASIMOVNode<T> extends JSONConvertible<ASIMOVNode<T>>, XMLConvertible<Object, ASIMOVNode<T>>, SLConvertible<ASIMOVNode<T>>{

	ASIMOVNode<T> withName(final String name);
	
	String getName();
	
	String getNodeType();
	
	ASIMOVNode<T> instantiate(final String key, final ASIMOVNode<?> value);
	
	Set<String> getKeys();
	
	Object getPropertyValue(final String key);
	
}
