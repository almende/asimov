package io.arum.model.resource.assemblyline;

import io.asimov.model.AbstractEmbodied;
import io.asimov.model.Body;
import io.asimov.model.ResourceType;
import io.asimov.model.XMLConvertible;
import io.asimov.xml.TAssemblyLineType;

import java.util.ArrayList;

import javax.persistence.Basic;
import javax.persistence.Embedded;
import javax.persistence.Entity;

import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 * {@link AssemblyLine}
 * 
 * @date $Date: 2014-03-28 11:22:14 +0100 (Fri, 28 Mar 2014) $
 * @version $Revision: 806 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Entity
@NoSql(dataType = "assemblyLines")
public class AssemblyLine extends AbstractEmbodied<AssemblyLine> implements ResourceType, XMLConvertible<Object, AssemblyLine>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	@Embedded
	@Field(name = "types")
	private ArrayList<AssemblyLineType> types;

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

	/** @param type the {@link AssbemblyLineType} to set */
	protected void setTypes(final ArrayList<AssemblyLineType> types)
	{
		this.types = types;
	}

	/**
	 * @param name the (new) {@link AssbemblyLineType}
	 * @return this {@link AssemblyLine} object
	 */
	public AssemblyLine withTypes(final ArrayList<AssemblyLineType> types)
	{
		setTypes(types);
		return this;
	}

	/** @return the type */
	public ArrayList<AssemblyLineType> getTypes()
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
	public AssemblyLine withReplicationID(final String replicationID)
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
	public AssemblyLine withBody(final Body body)
	{
		setBody( body );
		return this;
	}

	
	/** @see XMLConvertible#toXML() */
	@Override
	public Object toXML()
	{
		io.asimov.xml.TContext.AssemblyLine m = new io.asimov.xml.TContext.AssemblyLine().withAssemblyLineId(getName());
		for (AssemblyLineType t : getTypes()) {
			TAssemblyLineType component = new TAssemblyLineType().withType(t.getName());
			m.withAssemblyLineType(component);
		}
		return m;
	}

	/** @see XMLConvertible#fromXML(Object) */
	@Override
	public AssemblyLine fromXML(final Object xmlBean)
	{
		if (xmlBean instanceof io.asimov.xml.TContext.AssemblyLine) {
			io.asimov.xml.TContext.AssemblyLine m = (io.asimov.xml.TContext.AssemblyLine)xmlBean;
			withName(m.getAssemblyLineId());
			ArrayList<AssemblyLineType> types = new ArrayList<AssemblyLineType>();
			for (TAssemblyLineType c : m.getAssemblyLineType()) {
				types.add(new AssemblyLineType().withName(c.getType()));
			}
			withTypes(types);
			return this;
		} else {
			throw new IllegalStateException("Expected xmlBean to be an instanceof io.asimov.xml.TContext.AssemblyLine");
		}
	}


}
