package io.asimov.agent.scenario;

import io.asimov.model.ARUMContext;
import io.asimov.model.AbstractEmbodied;
import io.asimov.model.XMLConvertible;
import io.asimov.model.resource.ARUMResourceType;
import io.asimov.xml.TContext;

import java.util.Set;

public interface Context extends XMLConvertible<TContext,ARUMContext> {
	
	Set<ARUMResourceType> getResourceTypes();
	<T extends AbstractEmbodied<T>> Iterable<?> getResources(Class<?> clazz);
}
