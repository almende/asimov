package io.asimov.model;

import java.util.Set;

import io.arum.model.resource.ARUMResourceType;
import io.asimov.agent.scenario.Context;

public class ARUMContext implements Context {

	@Override
	public Set<ARUMResourceType> getResourceTypes() {
		throw new IllegalStateException("getResourceTypes for ARUM not yet implemented in ARUMContext");
	}

	@Override
	public <T extends AbstractEmbodied<T>> Iterable<?> getResources(
			Class<?> clazz) {
		throw new IllegalStateException("getResources for ARUM not yet implemented in ARUMContext");
	}

}
