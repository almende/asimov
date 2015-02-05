package io.asimov.agent.scenario;

import io.arum.model.resource.ARUMResourceType;
import io.asimov.model.AbstractEmbodied;

import java.util.Set;

public interface Context {
	
	Set<ARUMResourceType> getResourceTypes();
	<T extends AbstractEmbodied<T>> Iterable<?> getResources(Class<?> clazz);
}
