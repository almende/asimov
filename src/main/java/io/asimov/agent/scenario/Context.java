package io.asimov.agent.scenario;

import io.asimov.model.Resource;
import io.coala.resource.ResourceType;

import java.util.Set;

public interface Context {
	
	Set<ResourceType> getResourceTypes();
	Iterable<Resource> getResources();
	
}
