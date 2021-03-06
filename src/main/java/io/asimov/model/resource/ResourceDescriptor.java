package io.asimov.model.resource;

import io.asimov.model.Named;
import io.asimov.model.XMLConvertible;
import io.asimov.model.sl.ASIMOVNode;
import io.asimov.xml.TConnectedResource;
import io.asimov.xml.TResource;
import io.coala.agent.AgentID;

import java.util.List;

public interface ResourceDescriptor<T extends ResourceDescriptor<T>> extends Named, XMLConvertible<TResource, ResourceDescriptor<T>> {
	
	AgentID getAgentID();
	
	TConnectedResource getContainerResource();
	
	void setContainerResource(TConnectedResource containerAgentId);
	
	T withContainerResource(TConnectedResource containerAgentId);
	
	List<TConnectedResource> getConnectedResources();
	
	void setConnectedResources(TConnectedResource... containerAgentId);
	
	T withConnectedResources(TConnectedResource... containerAgentId);
	
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
	
	boolean isMoveable();
	
	void setMoveable(boolean moveable);
	
	T withMoveability(
			boolean moveable);
	
	boolean isInfrastructural();
	
	void setInfrastructural(boolean infrastructural);
	
	T withInfrastructural(
			boolean infrastructural);
	
	Long getMaxNofUsesInActivity();

	void setMaxNofUsesInActivity(Long maxNofUsesInActivity);

	T withMaxNofUsesInActivity(Long maxNofUsesInActivity);
	
	Long getMaxNofUsesInProcess();

	void setMaxNofUsesInProcess(Long maxNofUsesInProcess);
	
	T withMaxNofUsesInProcess(Long maxNofUsesInProcess);
	
}
