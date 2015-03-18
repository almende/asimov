package io.asimov.model.resource;

import io.asimov.model.Named;
import io.asimov.model.XMLConvertible;
import io.asimov.model.sl.ASIMOVNode;
import io.asimov.xml.TResource;

import java.util.List;

public interface ResourceDescriptor<T extends ResourceDescriptor<T>> extends Named, XMLConvertible<TResource, ResourceDescriptor<T>> {
	
	T withReplicationID(String replicationID);
	
	void setReplicationID(String replicationID);
	
	String getReplicationID(String replicationID);
	
	void setDescriptions(ASIMOVNode<?>... description);
	
	List<ASIMOVNode<?>> getDescriptions();
	
	T withDescriptions(ASIMOVNode<?>... description);
	
	void setType(String type);
	
	String getType();
	
	T withType(String type);
	
	void setSubTypes(String... subtypes);
	
	List<String> getSubTypes();
	
	T withSubTypes(String... subtypes);
	
}
