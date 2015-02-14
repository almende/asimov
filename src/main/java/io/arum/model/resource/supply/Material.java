package io.arum.model.resource.supply;

import java.util.ArrayList;

import io.arum.model.resource.assemblyline.AssemblyLine;
import io.asimov.model.AbstractEmbodied;
import io.asimov.model.Body;
import io.asimov.model.ResourceType;
import io.asimov.model.XMLConvertible;
import io.asimov.xml.TComponent;

import javax.persistence.Basic;
import javax.persistence.Embedded;
import javax.persistence.Entity;

import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 * {@link Material}
 * 
 * @date $Date: 2014-03-28 11:22:14 +0100 (Fri, 28 Mar 2014) $
 * @version $Revision: 806 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Entity
@NoSql(dataType = "materials")
public class Material extends AbstractEmbodied<Material> implements ResourceType,
		 XMLConvertible<Object, Material>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	@Embedded
	@Field(name = "types")
	private ArrayList<SupplyType> types;

	/** */
	@Basic
	@Field(name = "replicationID")
	private String replicationID;
	
	/** */
	@Field(name = "body")
	private Body body;
	
	/** */
	@Field(name = "inRoom")
	private AssemblyLine inRoom;

	/** @param type the {@link SupplyType} to set */
	protected void setTypes(final ArrayList<SupplyType> types)
	{
		this.types = types;
	}

	/**
	 * @param name the (new) {@link SupplyType}
	 * @return this {@link Material} object
	 */
	public Material withTypes(final ArrayList<SupplyType> types)
	{
		setTypes(types);
		return this;
	}

	/** @return the type */
	public ArrayList<SupplyType> getTypes()
	{
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
	public Material withReplicationID(final String replicationID)
	{
		setReplicationID(replicationID);
		return this;
	}

	/**
	 * @return the inRoom
	 */
	public AssemblyLine getInRoom()
	{
		return inRoom;
	}

	/**
	 * @param inRoom the inRoom to set
	 */
	public void setInRoom(AssemblyLine inRoom)
	{
		this.inRoom = inRoom;
		this.withContainmentInBody(this.inRoom);
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
	public Material withBody(final Body body)
	{
		setBody( body );
		return this;
	}


	/** @see XMLConvertible#toXML() */
	@Override
	public Object toXML()
	{
		io.asimov.xml.TContext.Material m = new io.asimov.xml.TContext.Material().withMaterialId(getName());
		for (SupplyType t : getTypes()) {
			TComponent component = new TComponent().withType(t.getName());
			m.withComponent(component);
		}
		return m;
	}

	/** @see XMLConvertible#fromXML(Object) */
	@Override
	public Material fromXML(final Object xmlBean)
	{
		if (xmlBean instanceof io.asimov.xml.TContext.Material) {
			io.asimov.xml.TContext.Material m = (io.asimov.xml.TContext.Material)xmlBean;
			withName(m.getMaterialId());
			ArrayList<SupplyType> types = new ArrayList<SupplyType>();
			for (TComponent c : m.getComponent()) {
				types.add(new SupplyType().withName(c.getType()));
			}
			withTypes(types);
			return this;
		} else {
			throw new IllegalStateException("Expected xmlBean to be an instanceof io.asimov.xml.TContext.Material");
		}
	}

}