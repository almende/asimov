package io.asimov.model.resource;

import io.asimov.model.Named;
import io.asimov.model.XMLConvertible;
import io.asimov.model.sl.ASIMOVNode;
import io.asimov.xml.TResource;

import java.util.List;

public interface ResourceDescriptor extends Named, XMLConvertible<TResource, ResourceDescriptor> {
	
	void setReplicationID(String replicationID);
	
	void getReplicationID(String replicationID);
	
	void setDescriptions(ASIMOVNode<?>... description);
	
	List<ASIMOVNode<?>> getDescriptions();
	
	ResourceDescriptor withDescriptions(ASIMOVNode<?>... description);
	
	void setType(String type);
	
	String getType();
	
	ResourceDescriptor withType(String type);
	
	void setSubTypes();
	
	List<String> getSubTypes();
	
	ResourceDescriptor withSubTypes(String... subtypes);
	
}
