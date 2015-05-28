package io.asimov.model.sl;

import io.coala.json.JsonUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;




public class ASIMOVNotNode implements ASIMOVNode<ASIMOVNotNode> {
	
	@JsonIgnore
	public String type = getNodeType();
	
	public String name;
	
	public Map<String,ASIMOVNode<?>> notNodeProperties;
	
	public Map<String, ASIMOVNode<?>> getNotNodeProperties() {
		if (this.notNodeProperties == null)
			this.notNodeProperties = new HashMap<String, ASIMOVNode<?>>();
		return notNodeProperties;
	}
	
	public ASIMOVNotNode(ASIMOVFormula formula){
		this();
		this.add("NOT", formula);
	}
	
	public ASIMOVNotNode(){
		super();
		getNotNodeProperties();
	}

	@Override
	public String toJSON() {
		return JsonUtil.toJSONString(this);
	}
	
	public ASIMOVFormula negate() {
		return (ASIMOVFormula)getPropertyValue("NOT");
	}

	@Override
	public ASIMOVNode<ASIMOVNotNode> fromJSON(String jsonValue) {
		InputStream is = new ByteArrayInputStream(jsonValue.getBytes());
		ASIMOVNotNode result = JsonUtil.fromJSON(is,ASIMOVNotNode.class);
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public ASIMOVNode<ASIMOVNotNode> fromXML(Object xmlBean) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object toXML() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ASIMOVNotNode withName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getNodeType() {
		return "NOT";
	}

	@Override
	public ASIMOVNotNode instantiate() {
		return this;
	}

	@Override
	public ASIMOVNotNode add(String key, ASIMOVNode<?> value){
		this.withName("not_"+value.getName());
		getNotNodeProperties().put(key,value);
		return this;
	}
	
	@Override
	public Set<String> getKeys() {
		return Collections.singleton("NOT");
	}

	@Override
	public Object getPropertyValue(String key) {
		return notNodeProperties.get(key);
	}
	@SuppressWarnings("unchecked")
	@Override
	public <N extends ASIMOVNode<N>> N toSL() {
		return (N) this;
	}

	@Override
	public <N extends ASIMOVNode<N>> ASIMOVNode<ASIMOVNotNode> fromSL(N node) {
		this.fromJSON(node.toJSON());
		return this;
	}

	@Override
	public String toString(){
		return this.toJSON();
	}
	
	@Override
	public ASIMOVNode<ASIMOVNotNode> replace(String key, ASIMOVNode<?> value) {
		if (getKeys().contains(key))
			add(key, value);
		return this;
	}
}
