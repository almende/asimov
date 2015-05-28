package io.asimov.model.sl;

import io.coala.json.JsonUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ASIMOVTermSequenceNode implements ASIMOVNode<ASIMOVTermSequenceNode> {
	
	@JsonIgnore
	public String type = getNodeType();
	
	public String name;
	
	public List<ASIMOVTerm> termSequence;


	public ASIMOVTermSequenceNode() {
		super();
		getTermSequence();
	}
	
	public ASIMOVTermSequenceNode(List<ASIMOVTerm> terms) {
		if (termSequence == null)
			termSequence = new ArrayList<ASIMOVTerm>();
		termSequence = terms;
	}
	
	@Override
	public String toJSON() {
		return JsonUtil.toJSONString(this);
	}

	@Override
	public ASIMOVTermSequenceNode fromJSON(String jsonValue) {
		InputStream is = new ByteArrayInputStream(jsonValue.getBytes());
		ASIMOVTermSequenceNode result = JsonUtil.fromJSON(is,ASIMOVTermSequenceNode.class);
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public ASIMOVTermSequenceNode fromXML(Object xmlBean) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object toXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ASIMOVTermSequenceNode instantiate() {
		final ASIMOVTermSequenceNode copy = new ASIMOVTermSequenceNode().withName(this.name);
		copy.termSequence = new ArrayList<ASIMOVTerm>( this.termSequence == null?new ArrayList<ASIMOVTerm>(0):this.termSequence);
		return copy;
	}
	
	@Override
	public ASIMOVTermSequenceNode add(String key, ASIMOVNode<?> value){
		this.termSequence.add((ASIMOVTerm) value);
		return this;
	}

	@Override
	public String getNodeType() {
		return "TERM_SEQUENCE";
	}

	@Override
	public ASIMOVTermSequenceNode withName(String name) {
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
	public <N extends ASIMOVNode<N>> ASIMOVNode<ASIMOVTermSequenceNode> fromSL(N node) {
		this.fromJSON(node.toJSON());
		return this;
	}

	@Override
	public Set<String> getKeys() {
		Set<String> indexes = new HashSet<String>();
		for (int i = 0; i < termSequence.size(); i++)
			indexes.add(""+i);
		return indexes;
	}

	@Override
	public Object getPropertyValue(String index) {
		return termSequence.get(Integer.valueOf(index));
	}
	
	@Override
	public String toString(){
		return this.toJSON();
	}

	public List<ASIMOVTerm> getTermSequence() {
		return termSequence;
	}
	
	@Override
	public ASIMOVNode<ASIMOVTermSequenceNode> replace(String key, ASIMOVNode<?> value) {
		for (ASIMOVTerm t : this.termSequence)
			t.replace(key, value);
		return this;
	}



}