package io.asimov.model.sl;

import io.coala.json.JsonUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class ASIMOVTerm implements ASIMOVNode<ASIMOVTerm> {
	
	@JsonIgnore
	public String type = getNodeType();
	
	public String name;
	
	public String stringValue;
	
	public long integerValue;
	
	
	public Map<String,ASIMOVNode<?>> termProperties;
	
	public ASIMOVTerm(){
		super();
		getTermProperties();
	}
	
	public Map<String, ASIMOVNode<?>> getTermProperties() {
		if (this.termProperties == null)
			this.termProperties = new HashMap<String, ASIMOVNode<?>>();
		return termProperties;
	}

	@Override
	public String toJSON() {
		return JsonUtil.toJSONString(this);
	}

	@Override
	public ASIMOVTerm fromJSON(String jsonValue) {
		InputStream is = new ByteArrayInputStream(jsonValue.getBytes());
		ASIMOVTerm result = JsonUtil.fromJSON(is,ASIMOVTerm.class);
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public ASIMOVTerm fromXML(Object xmlBean) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object toXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ASIMOVTerm instantiate(String key, ASIMOVNode<?> value) {
		final ASIMOVTerm copy = new ASIMOVTerm().withName(this.name);
		copy.termProperties = new HashMap<String, ASIMOVNode<?>>();
		if (this.termProperties != null)
			for (Entry<String, ASIMOVNode<?>>  entry : this.termProperties.entrySet())
				copy.termProperties.put(entry.getKey(), entry.getValue());
		copy.termProperties.put(key,value);
		return copy;
	}

	@Override
	public String getNodeType() {
		return "TERM";
	}

	@Override
	public ASIMOVTerm withName(String name) {
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
	public <N extends ASIMOVNode<N>> ASIMOVNode<ASIMOVTerm> fromSL(N node) {
		this.fromJSON(node.toJSON());
		return this;
	}

	@Override
	public Set<String> getKeys() {
		return termProperties.keySet();
	}

	@Override
	public Object getPropertyValue(String key) {
		return termProperties.get(key);
	}

	@Override
	public String toString(){
		return this.toJSON();
	}

	@Override
	public ASIMOVNode<ASIMOVTerm> replace(String key, ASIMOVNode<?> value) {
		if (getKeys().contains(key))
			this.termProperties.put(key, value);
		return this;
	}

}
