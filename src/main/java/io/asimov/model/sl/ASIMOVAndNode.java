package io.asimov.model.sl;

import io.coala.json.JsonUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ASIMOVAndNode implements ASIMOVNode<ASIMOVAndNode> {

	Map<String,ASIMOVFormula> namedChildren;
	
	
	String type = getNodeType();
	
	public ASIMOVAndNode(ASIMOVFormula... formulas) {
		int count = 0;
		if (namedChildren != null)
			count = namedChildren.size();
		if (formulas != null)
			for (ASIMOVFormula f : formulas) {
				this.instantiate(""+(count++), f);
			}
				
	}
	
	@Override
	public String toJSON() {
		return JsonUtil.toJSONString(this);
	}

	@Override
	public ASIMOVAndNode fromJSON(String jsonValue) {
		InputStream is = new ByteArrayInputStream(jsonValue.getBytes());
		ASIMOVAndNode result = JsonUtil.fromJSON(is,ASIMOVAndNode.class);
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}


	@Override
	public ASIMOVNode<ASIMOVAndNode> fromXML(Object xmlBean) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object toXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <N extends ASIMOVNode<N>> N toSL() {
		return (N) this;
	}

	@Override
	public <N extends ASIMOVNode<N>> ASIMOVNode<ASIMOVAndNode> fromSL(N node) {
		this.fromJSON(node.toJSON());
		return this;
	}

	@Override
	public ASIMOVNode<ASIMOVAndNode> withName(String name) {
		return this;
	}

	@Override
	public String getName() {
		return "&&";
	}

	@Override
	public String getNodeType() {
		return "AND";
	}

	@Override
	public ASIMOVNode<ASIMOVAndNode> instantiate(String key, ASIMOVNode<?> value) {
		if (this.namedChildren == null)
			this.namedChildren = new HashMap<String, ASIMOVFormula>();
		this.namedChildren.put(key, (ASIMOVFormula)value);
		return this;
	}

	@Override
	public Set<String> getKeys() {
		return namedChildren.keySet();
	}

	@Override
	public Object getPropertyValue(String key) {
		return namedChildren.get(key);
	}

}
