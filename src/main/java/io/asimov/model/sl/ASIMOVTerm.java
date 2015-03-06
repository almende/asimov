package io.asimov.model.sl;

import io.coala.json.JsonUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class ASIMOVTerm implements ASIMOVNode<ASIMOVTerm> {
	
	String type = getNodeType();
	
	String termName;
	
	Map<String,ASIMOVNode<?>> termProperties;

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
		final ASIMOVTerm copy = new ASIMOVTerm().withName(this.termName);
		copy.termProperties = new HashMap<String, ASIMOVNode<?>>();
		if (this.termProperties != null)
			for (Entry<String, ASIMOVNode<?>>  entry : this.termProperties.entrySet())
				copy.termProperties.put(entry.getKey(), entry.getValue());
		this.termProperties.put(key, value);
		return copy;
	}

	@Override
	public String getNodeType() {
		return "TERM";
	}

	@Override
	public ASIMOVTerm withName(String name) {
		termName = name;
		return this;
	}

	@Override
	public String getName() {
		return termName;
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


}
