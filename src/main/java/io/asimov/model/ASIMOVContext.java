package io.asimov.model;

import io.asimov.agent.scenario.Context;
import io.asimov.model.resource.ARUMResourceType;
import io.asimov.model.resource.ResourceDescriptor;
import io.asimov.xml.TContext;
import io.asimov.xml.TResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ASIMOVContext implements Context {

	
	final private static Map<String, List<AbstractEmbodied<?>>> resources = 
			new HashMap<String, List<AbstractEmbodied<?>>>();
	
	
	
	@Override
	public Set<String> getResourceTypes() {
		return resources.keySet();
	}

	@Override
	public <T extends AbstractEmbodied<T>> Iterable<?> getResources(
			final String resourceType) {
				return resources.get(resourceType);
	}

	@Override
	public ASIMOVContext fromXML(TContext xmlBean) {
		for (io.asimov.xml.TResource resource : xmlBean.getResource()) {
			if (resources.containsKey(resource.getResourceType()))
				resources.put(resource.getResourceType(), new ArrayList<AbstractEmbodied<?>>());
			List<AbstractEmbodied<?>> resourceContainer = resources.get(resource.getResourceType());
			ASIMOVResourceDescriptor resourceDescriptor = new ASIMOVResourceDescriptor().fromXML(resource);
			resourceContainer.add((AbstractEmbodied<?>)resourceDescriptor);
		}
		return this;
	}

	@Override
	public TContext toXML() {
		TContext context = new TContext();
		for (String type : getResourceTypes()) {
			for (Object r : getResources(type)) {
				context.getResource().add((TResource)((ASIMOVResourceDescriptor) r).toXML());
			}
		}
		return context;
	}

}
