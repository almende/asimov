package io.asimov.model;

import io.arum.model.resource.assemblyline.AssemblyLine;
import io.arum.model.resource.person.Person;
import io.arum.model.resource.supply.Material;
import io.asimov.agent.scenario.Context;
import io.asimov.model.resource.ARUMResourceType;
import io.asimov.xml.TContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ARUMContext implements Context {

	final private static Map<ARUMResourceType, List<AbstractEmbodied<?>>> resources = 
			new HashMap<ARUMResourceType, List<AbstractEmbodied<?>>>();
	
	
	public ARUMContext() {
		if (resources.isEmpty()) {
			resources.put(ARUMResourceType.ASSEMBLY_LINE, new ArrayList<AbstractEmbodied<?>>());
			resources.put(ARUMResourceType.MATERIAL, new ArrayList<AbstractEmbodied<?>>());
			resources.put(ARUMResourceType.PERSON, new ArrayList<AbstractEmbodied<?>>());
		}
	}
	
	@Override
	public Set<ARUMResourceType> getResourceTypes() {
		return resources.keySet();
	}

	@Override
	public <T extends AbstractEmbodied<T>> Iterable<?> getResources(
			Class<?> clazz) {
		for (ARUMResourceType type : getResourceTypes()) {
			if (type.getClass().equals(clazz)) {
				return resources.get(type);
			}
		}
		return null;
	}

	@Override
	public ARUMContext fromXML(TContext xmlBean) {
		List<AbstractEmbodied<?>> resourceContainer = resources.get(ARUMResourceType.ASSEMBLY_LINE);
		for (io.asimov.xml.TContext.AssemblyLine resource : xmlBean.getAssemblyLine()) {
			AssemblyLine al = new AssemblyLine().fromXML((Object)resource);
			resourceContainer.add((AbstractEmbodied<?>)al);
		}
		resourceContainer = resources.get(ARUMResourceType.MATERIAL);
		for (io.asimov.xml.TContext.Material resource : xmlBean.getMaterial()) {
			Material m = new Material().fromXML((Object)resource);
			resourceContainer.add((AbstractEmbodied<?>)m);
		}
		resourceContainer = resources.get(ARUMResourceType.PERSON);
		for (io.asimov.xml.TContext.Person resource : xmlBean.getPerson()) {
			Person m = new Person().fromXML((Object)resource);
			resourceContainer.add((AbstractEmbodied<?>)m);
		}
		return this;
	}

	@Override
	public TContext toXML() {
		TContext context = new TContext();
		for (Object r : getResources(ARUMResourceType.ASSEMBLY_LINE.getClass())) {
			context.getAssemblyLine().add((TContext.AssemblyLine)((AssemblyLine) r).toXML());
		}
		for (Object r : getResources(ARUMResourceType.MATERIAL.getClass())) {
			context.getMaterial().add((TContext.Material)((Material) r).toXML());
		}
		for (Object r : getResources(ARUMResourceType.PERSON.getClass())) {
			context.getPerson().add((TContext.Person)((Person) r).toXML());
		}
		return context;
	}

}
