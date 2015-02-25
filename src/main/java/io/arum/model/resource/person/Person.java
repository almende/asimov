package io.arum.model.resource.person;

import io.arum.model.resource.assemblyline.AssemblyLine;
import io.asimov.model.AbstractEmbodied;
import io.asimov.model.Body;
import io.asimov.model.ResourceType;
import io.asimov.model.XMLConvertible;
import io.asimov.model.xml.XmlUtil;
import io.asimov.xml.TRole;
import io.coala.log.LogUtil;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.xml.datatype.Duration;

import org.apache.log4j.Logger;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@link Person}
 * 
 * @date $Date: 2014-03-28 11:22:14 +0100 (Fri, 28 Mar 2014) $
 * @version $Revision: 806 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Entity
@NoSql(dataType = "occupants")
public class Person extends AbstractEmbodied<Person> implements ResourceType, XMLConvertible<Object, Person>
{

	/** */
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private static Logger LOG = LogUtil.getLogger(Person.class);
	
	/** */
	@Embedded
	@Field(name = "types")
	private ArrayList<PersonRole> types;

	/** */
	@Basic
	@Field(name = "replicationID")
	private String replicationID;
	
	/** */
	@Field(name = "body")
	private Body body;
	
	/** */
	@Field(name = "atAssemblyLine")
	private AssemblyLine atAssemblyLine;
	
	/**
	 */
	@Field(name = "availableFromTime")
	private Long availableFromTime;

	/**
	 * @return the availableFromTime
	 */
	public Long getAvailableFromTime() {
		return availableFromTime;
	}

	/**
	 * @param availableFromTime the availableFromTime to set
	 */
	public void setAvailableFromTime(Long availableFromTime) {
		this.availableFromTime = availableFromTime;
	}

	/**
	 * @param availableFromTime the availableFromTime to set
	 */
	public Person withAvailableFromTime(Long availableFromTime) {
		this.setAvailableFromTime(availableFromTime);
		return this;
	}

	/** @param type the {@link PersonRole} to set */
	protected void setTypes(final ArrayList<PersonRole> types)
	{
		this.types = types;
	}

	/**
	 * @param name the (new) {@link PersonRole}
	 * @return this {@link Person} object
	 */
	public Person withTypes(final ArrayList<PersonRole> types)
	{
		setTypes(types);
		return this;
	}

	/** @return the type */
	public ArrayList<PersonRole> getTypes()
	{
		if (this.types == null)
			this.types = new ArrayList<PersonRole>();
		return this.types;
	}

	/**
	 * @return the replicationID
	 */
	public String getReplicationID()
	{
		return this.replicationID;
	}
	

	/**
	 * @param replicationID the replication to set
	 */
	public void setReplicationID(final String replicationID)
	{
		this.replicationID = replicationID;
	}

	/**
	 * @param replicationID the replication to set
	 */
	public Person withReplicationID(final String replicationID)
	{
		setReplicationID(replicationID);
		return this;
	}

	/**
	 * @return the inRoom
	 */
	public AssemblyLine getAtAssemblyLine()
	{
		return atAssemblyLine;
	}

	/**
	 * @param inRoom the inRoom to set
	 */
	public void setAtAssemblyLine(AssemblyLine atAssemblyLine)
	{
		this.atAssemblyLine = atAssemblyLine;
		this.withContainmentInBody(this.atAssemblyLine);
	}

	/** @return the body */
	public Body getBody()
	{
		return this.body;
	}

	/** @param body the body to set */
	protected void setBody(final Body body)
	{
		this.body = body;
	}

	/** @param body the body to set */
	public Person withBody(final Body body)
	{
		setBody( body );
		return this;
	}

	/** @see XMLConvertible#toXML() */
	@Override
	public Object toXML()
	{
		io.asimov.xml.TContext.Person p = new io.asimov.xml.TContext.Person().withPersonId(getName());
		if (getAvailableFromTime() != null)
				p.setAvailableAfter(XmlUtil.toDuration(getAvailableFromTime()));
		for (PersonRole t : getTypes()) {
			TRole role = new TRole().withRoleName(t.getName()).withId(t.getName());
			p.withRole(role);
		}
		return p;
	}

	/** @see XMLConvertible#fromXML(Object) */
	@Override
	public Person fromXML(final Object xmlBean)
	{
		if (xmlBean instanceof io.asimov.xml.TContext.Person) {
			io.asimov.xml.TContext.Person p = (io.asimov.xml.TContext.Person)xmlBean;
			withName(p.getPersonId());
			Duration availableFromTime = p.getAvailableAfter();
			if (availableFromTime != null) {
				withAvailableFromTime(availableFromTime.getTimeInMillis(new Date(0L)));
			}
			ArrayList<PersonRole> types = new ArrayList<PersonRole>();
			for (TRole c : p.getRole()) {
				types.add(new PersonRole().withName(c.getId()));
			}
			withTypes(types);
			return this;
		} else {
			throw new IllegalStateException("Expected xmlBean to be an instanceof io.asimov.xml.TContext.Material");
		}
	}

}
