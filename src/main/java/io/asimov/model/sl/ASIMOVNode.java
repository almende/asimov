package io.asimov.model.sl;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.asimov.model.XMLConvertible;
import io.asimov.reasoning.sl.SLParsable;
import io.coala.json.JSONConvertible;

@JsonTypeInfo(  
	    use = JsonTypeInfo.Id.NAME,  
	    include = JsonTypeInfo.As.PROPERTY,  
	    property = "type")  
@JsonSubTypes({  
		@Type(value = ASIMOVNotNode.class, name = "NOT"),
    	@Type(value = ASIMOVFormula.class, name = "FORMULA"),  
	    @Type(value = ASIMOVTerm.class, name = "TERM"),  
	    @Type(value = ASIMOVAndNode.class, name = "AND"),
	    @Type(value = ASIMOVFunctionNode.class, name = "FUNCTION"),
	    @Type(value = ASIMOVIntegerTerm.class, name = "INTEGER"),
	    @Type(value = ASIMOVStringTerm.class, name = "STRING")
	    
	})  
public interface ASIMOVNode<T> extends JSONConvertible<ASIMOVNode<T>>, XMLConvertible<Object, ASIMOVNode<T>>, SLConvertible<ASIMOVNode<T>> {

	
	ASIMOVNode<T> withName(final String name);
	
	String getName();
	
	@JsonIgnore
	String getNodeType();
	
	ASIMOVNode<T> instantiate(final String key, final ASIMOVNode<?> value);
	
	@JsonIgnore
	Set<String> getKeys();
	
	@JsonIgnore
	Object getPropertyValue(final String key);
	
	ASIMOVNode<T> replace(final String key, final ASIMOVNode<?> value);
	
}
