package io.asimov.agent.scenario;

import io.asimov.agent.resource.Resource;
import io.coala.resource.ResourceType;

import java.util.Set;

public interface Context {
	
	Set<ResourceType> getResourceTypes();
	Iterable<Resource> getResources();
	
}
