package io.asimov.model.sl;

import io.coala.json.JsonUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class ASIMOVFormula implements  ASIMOVNode<ASIMOVFormula> {
	
	@JsonIgnore
	int cachedHashCode;
	
	@JsonIgnore
	boolean hashCacheIsValid = false;
	
	
	@JsonIgnore
	public String type = getNodeType();
	
	public String name;
	
	public Map<String,ASIMOVNode<?>> formulaProperties;
	
	public ASIMOVFormula(){
		super();
		getFormulaProperties();
	}

	public Map<String, ASIMOVNode<?>> getFormulaProperties() {
		if (this.formulaProperties == null)
			this.formulaProperties = new HashMap<String, ASIMOVNode<?>>();
		hashCacheIsValid = false;
		return formulaProperties;
	}

	@Override
	public String toJSON() {
		return JsonUtil.toJSONString(this);
	}

	@Override
	public ASIMOVFormula fromJSON(String jsonValue) {
		hashCacheIsValid = false;
		InputStream is = new ByteArrayInputStream(jsonValue.getBytes());
		ASIMOVFormula result = JsonUtil.fromJSON(is,ASIMOVFormula.class);
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public ASIMOVFormula fromXML(Object xmlBean) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object toXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ASIMOVFormula instantiate() {
		final ASIMOVFormula copy = new ASIMOVFormula().withName(this.name);
		copy.formulaProperties = new HashMap<String, ASIMOVNode<?>>(this.formulaProperties == null?new HashMap<String,ASIMOVNode<?>>(0):this.formulaProperties);
		copy.type = type;
		return copy;
	}
	
	@Override
	public ASIMOVFormula add(String key, ASIMOVNode<?> value){
		this.formulaProperties.put(key,value);
		return this;
	}
	
	public ASIMOVNotNode negate() {
		return new ASIMOVNotNode(this);
	}

	@Override
	public String getNodeType() {
		return "FORMULA";
	}

	@Override
	public ASIMOVFormula withName(String name) {
		hashCacheIsValid = false;
		this.name = name;
		return this;
	}

	@Override
	public String getName() {
		return name;
	}


	@SuppressWarnings("unchecked")
	@Override
	public <N extends ASIMOVNode<N>> N toSL() {
		return (N) this;
	}

	@Override
	public <N extends ASIMOVNode<N>> ASIMOVNode<ASIMOVFormula> fromSL(N node) {
		this.fromJSON(node.toJSON());
		return this;
	}

	@Override
	public Set<String> getKeys() {
		return formulaProperties.keySet();
	}

	@Override
	public ASIMOVNode<?> getPropertyValue(String key) {
		return formulaProperties.get(key);
	}

	@Override
	public String toString(){
		return this.toJSON();
	}
	
	@Override
	public ASIMOVNode<ASIMOVFormula> replace(String key, ASIMOVNode<?> value) {
		hashCacheIsValid = false;
		if (getKeys().contains(key))
			this.formulaProperties.put(key, value);
		return this;
	}
	

	@Override
	public boolean equals(Object other){
		if (other == null)
			return false;
		return (this.hashCode() == other.hashCode());
	}

	
	@Override
	public int hashCode(){
		if (!hashCacheIsValid)
			cachedHashCode = toString().hashCode();
		return cachedHashCode;
	}

	
}
