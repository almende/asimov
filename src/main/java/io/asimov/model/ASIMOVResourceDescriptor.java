/**
 * 
 */
package io.asimov.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Embedded;
import javax.xml.datatype.Duration;

import org.eclipse.persistence.nosql.annotations.Field;

import io.asimov.model.resource.ResourceDescriptor;
import io.asimov.model.resource.ResourceSubtype;
import io.asimov.model.sl.ASIMOVNode;
import io.asimov.model.xml.XmlUtil;
import io.asimov.xml.TConnectedResource;
import io.asimov.xml.TResource;
import io.asimov.xml.TUnavailablePeriodDistribution;
import io.coala.agent.AgentID;
import io.coala.model.ModelID;

/**
 * @author suki
 *
 */
public class ASIMOVResourceDescriptor extends
		AbstractEmbodied<ASIMOVResourceDescriptor> implements
		ResourceDescriptor<ASIMOVResourceDescriptor> {

	/** */
	@Embedded
	@Field(name = "agentID")
	private AgentID agentID;

	/** */
	@Embedded
	@Field(name = "containerResource")
	private TConnectedResource containerResource;

	/** */
	@Embedded
	@Field(name = "connectedResources")
	private List<TConnectedResource> connectedResources;

	/** */
	@Embedded
	@Field(name = "moveable")
	private boolean moveable;

	/** */
	@Embedded
	@Field(name = "infrastructural")
	private boolean infrastructural;

	/** */
	@Embedded
	@Field(name = "type")
	private String type;

	/** */
	@Embedded
	@Field(name = "types")
	private ArrayList<ResourceSubtype> types;

	/** */
	@Basic
	@Field(name = "replicationID")
	private String replicationID;

	/** */
	@Field(name = "body")
	private Body body;

	/** */
	@Field(name = "parentResource")
	private ASIMOVResourceDescriptor parentResource;

	/**
	 */
	@Field(name = "availableFromTime")
	private Long availableFromTime;

	/**
	 * 
	 */
	@Field(name = "unavailabilityDistribution")
	private TUnavailablePeriodDistribution unavailabilityDistribution;

	private List<ASIMOVNode<?>> descriptions;

	/**
	 * 
	 * @return unavailabilityDistribution
	 */
	public TUnavailablePeriodDistribution getUnavailabilityDistribution() {
		return unavailabilityDistribution;
	}

	/**
	 * 
	 * @param unavailabilityDistribution
	 */
	public void setUnavailabilityDistribution(
			TUnavailablePeriodDistribution unavailabilityDistribution) {
		this.unavailabilityDistribution = unavailabilityDistribution;
	}

	/**
	 * 
	 * @param unavailabilityDistribution
	 */
	public ASIMOVResourceDescriptor withUnavailabilityDistribution(
			TUnavailablePeriodDistribution unavailabilityDistribution) {
		setUnavailabilityDistribution(unavailabilityDistribution);
		return this;
	}

	/**
	 * @return the availableFromTime
	 */
	public Long getAvailableFromTime() {
		return availableFromTime;
	}

	/**
	 * @param availableFromTime
	 *            the availableFromTime to set
	 */
	public void setAvailableFromTime(Long availableFromTime) {
		this.availableFromTime = availableFromTime;
	}

	/**
	 * @param availableFromTime
	 *            the availableFromTime to set
	 */
	public ASIMOVResourceDescriptor withAvailableFromTime(Long availableFromTime) {
		this.setAvailableFromTime(availableFromTime);
		return this;
	}

	/**
	 * @param type
	 *            the {@link AssbemblyLineType} to set
	 */
	protected void setTypes(final ArrayList<ResourceSubtype> types) {
		this.types = types;
	}

	/**
	 * @param name
	 *            the (new) {@link AssbemblyLineType}
	 * @return this {@link AssemblyLine} object
	 */
	public ASIMOVResourceDescriptor withTypes(
			final ArrayList<ResourceSubtype> types) {
		setTypes(types);
		return this;
	}

	/** @return the type */
	public ArrayList<ResourceSubtype> getTypes() {
		return this.types;
	}

	/**
	 * @return the replicationID
	 */
	public String getReplicationID() {
		return this.replicationID;
	}

	/**
	 * @param replicationID
	 *            the replication to set
	 */
	@Override
	public ASIMOVResourceDescriptor withReplicationID(final String replicationID) {
		setReplicationID(replicationID);
		return this;
	}

	/**
	 * @return the inRoom
	 */
	public ASIMOVResourceDescriptor getParentResource() {
		return parentResource;
	}

	/**
	 * @param inRoom
	 *            the inRoom to set
	 */
	public void setParentResource(ASIMOVResourceDescriptor parentResource) {
		this.parentResource = parentResource;
		this.withContainmentInBody(this.parentResource);
	}

	/** @return the body */
	public Body getBody() {
		return this.body;
	}

	/**
	 * @param body
	 *            the body to set
	 */
	protected void setBody(final Body body) {
		this.body = body;
	}

	/**
	 * @param body
	 *            the body to set
	 */
	public ASIMOVResourceDescriptor withBody(final Body body) {
		setBody(body);
		return this;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6404864314198893543L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.asimov.model.XMLConvertible#fromXML(java.lang.Object)
	 */
	@Override
	public ASIMOVResourceDescriptor fromXML(final TResource xmlBean) {

		Duration availableFromTime = xmlBean.getAvailableAfter();
		if (availableFromTime != null) {
			this.withAvailableFromTime(availableFromTime
					.getTimeInMillis(new Date(0L)));
		}

		String[] subTypes = new String[xmlBean.getResourceSubType().size()];
		xmlBean.getResourceSubType().toArray(subTypes);
		// TODO Import initial formula's of resource state from xml
		ASIMOVNode<?>[] descriptions = new ASIMOVNode<?>[0];

		return this
				.withName(xmlBean.getResourceId())
				.withSubTypes(subTypes)
				.withType(xmlBean.getResourceType())
				.withDescriptions(descriptions)
				.withMoveability(!xmlBean.isStationary())
				.withInfrastructural(xmlBean.isInfrastructural())
				.withContainerResource(xmlBean.getContainerResourceId())
				.withConnectedResources(
						xmlBean.getConnectedResourceId().toArray(
								new TConnectedResource[0]));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.asimov.model.XMLConvertible#toXML()
	 */
	@Override
	public TResource toXML() {
		TResource resource = new TResource().withResourceId(getName());
		if (getAvailableFromTime() != null)
			resource.withAvailableAfter(XmlUtil
					.toDuration(getAvailableFromTime()));
		resource.withResourceType(getType());
		String[] subTypes = new String[getTypes().size()];
		int tc = 0;
		for (ResourceSubtype t : getTypes()) {
			subTypes[tc] = t.getName();
			tc++;
		}
		resource.withResourceSubType(subTypes);
		resource.withUnavailable(getUnavailabilityDistribution());
		resource.setStationary(!isMoveable());
		// TODO Export initial formula's of resource state from xml
		resource.setInfrastructural(isInfrastructural());
		resource.setContainerResourceId(getContainerResource());
		if (getConnectedResources() != null)
			resource.getConnectedResourceId().addAll(getConnectedResources());
		return resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.asimov.model.resource.ResourceDescriptor#setReplicationID(java.lang
	 * .String)
	 */
	@Override
	public void setReplicationID(final String replicationID) {
		this.replicationID = replicationID;
	}

	@Override
	public String getReplicationID(String replicationID) {
		return this.replicationID;
	}

	@Override
	public void setDescriptions(ASIMOVNode<?>... description) {
		if (description != null)
			setDescriptions(Arrays.asList(description));
	}

	@Override
	public List<ASIMOVNode<?>> getDescriptions() {
		return descriptions;
	}

	@Override
	public void setType(final String type) {
		this.type = type;
	}

	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public ASIMOVResourceDescriptor withType(String type) {
		setType(type);
		return this;
	}

	@Override
	public void setSubTypes(String... subtypes) {
		if (subtypes == null)
			return;
		this.types = new ArrayList<ResourceSubtype>();
		for (final String subType : subtypes) {
			this.types.add(new ResourceSubtype().withName(subType));
		}
	}

	@Override
	public List<String> getSubTypes() {
		if (this.types == null)
			this.types = new ArrayList<ResourceSubtype>();
		List<String> subTypeStrings = new ArrayList<String>();
		for (ResourceSubtype t : this.types) {
			subTypeStrings.add(t.getName());
		}
		return subTypeStrings;
	}

	@Override
	public ASIMOVResourceDescriptor withSubTypes(String... subtypes) {
		setSubTypes(subtypes);
		return this;
	}

	@Override
	public ASIMOVResourceDescriptor withDescriptions(
			ASIMOVNode<?>... description) {
		setDescriptions(description);
		return this;
	}

	/**
	 * @param descriptions
	 *            the descriptions to set
	 */

	public void setDescriptions(List<ASIMOVNode<?>> descriptions) {
		this.descriptions = descriptions;
	}

	@Override
	public AgentID getAgentID() {
		if (this.agentID == null) {
			this.agentID = new AgentID(new ModelID(getReplicationID()),
					getName());
		}
		return this.agentID;
	}

	/**
	 * @return the moveable
	 */
	@Override
	public boolean isMoveable() {
		return moveable;
	}

	/**
	 * @param moveable
	 *            the moveable to set
	 */
	@Override
	public void setMoveable(boolean moveable) {
		this.moveable = moveable;
	}

	@Override
	public ASIMOVResourceDescriptor withMoveability(boolean moveable) {
		setMoveable(moveable);
		return this;
	}

	@Override
	public boolean isInfrastructural() {
		return this.infrastructural;
	}

	@Override
	public void setInfrastructural(boolean infrastructural) {
		this.infrastructural = infrastructural;
	}

	@Override
	public ASIMOVResourceDescriptor withInfrastructural(boolean infrastructural) {
		setInfrastructural(infrastructural);
		return this;
	}

	@Override
	public TConnectedResource getContainerResource() {
		return this.containerResource;
	}

	@Override
	public void setContainerResource(TConnectedResource containerAgentId) {
		this.containerResource = containerAgentId;
	}

	@Override
	public ASIMOVResourceDescriptor withContainerResource(
			TConnectedResource containerAgentId) {
		setContainerResource(containerAgentId);
		return this;
	}

	@Override
	public List<TConnectedResource> getConnectedResources() {
		return this.connectedResources;
	}

	@Override
	public void setConnectedResources(TConnectedResource... containerAgentId) {
		if (containerAgentId == null)
			this.connectedResources = null;
		else
			this.connectedResources = Arrays.asList(containerAgentId);
	}

	@Override
	public ASIMOVResourceDescriptor withConnectedResources(
			TConnectedResource... containerAgentId) {
		setConnectedResources(containerAgentId);
		return this;
	}

}
