package io.asimov.agent.scenario;

import io.arum.model.resource.ARUMResourceType;
import io.asimov.model.AbstractEmbodied;
import io.asimov.model.Resource;
import io.asimov.model.ResourceType;

import java.util.Set;

public interface Context {
	
	Set<ARUMResourceType> getResourceTypes();
	<T extends AbstractEmbodied<T>> Iterable<?> getResources(Class<?> clazz);
}
