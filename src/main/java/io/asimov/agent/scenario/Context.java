package io.asimov.agent.scenario;

import io.asimov.model.ASIMOVContext;
import io.asimov.model.AbstractEmbodied;
import io.asimov.model.XMLConvertible;
import io.asimov.xml.TContext;

import java.util.Set;

public interface Context extends XMLConvertible<TContext,ASIMOVContext> {
	
	Set<String> getResourceTypes();
	<T extends AbstractEmbodied<T>> Iterable<?> getResources(String resourceType);
}
