package io.asimov.model.sl;

import io.coala.json.JsonUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class ASIMOVFormula implements  ASIMOVNode<ASIMOVFormula> {
	
	public boolean isNotNode;
	
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
		return formulaProperties;
	}

	@Override
	public String toJSON() {
		return JsonUtil.toJSONString(this);
	}

	@Override
	public ASIMOVFormula fromJSON(String jsonValue) {
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
	public ASIMOVFormula instantiate(String key, ASIMOVNode<?> value) {
		final ASIMOVFormula copy = new ASIMOVFormula().withName(this.name);
		copy.formulaProperties = new HashMap<String, ASIMOVNode<?>>();
		copy.isNotNode = isNotNode;
		copy.type = type;
		if (this.formulaProperties != null)
			for (Entry<String, ASIMOVNode<?>>  entry : this.formulaProperties.entrySet())
				copy.formulaProperties.put(entry.getKey(), entry.getValue());
		copy.formulaProperties.put(key,value);
		return copy;
	}
	
	public ASIMOVFormula negate() {
		if (this.isNotNode) 
			this.isNotNode = false;
		else 
			this.isNotNode = true;
		this.type = getNodeType();
		return this;
	}

	@Override
	public String getNodeType() {
		return (isNotNode) ? "NOT" : "FORMULA";
	}

	@Override
	public ASIMOVFormula withName(String name) {
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

	
}
