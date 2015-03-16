package io.asimov.model;

import io.asimov.model.sl.ASIMOVFormula;
import io.asimov.model.sl.ASIMOVNode;
import io.asimov.reasoning.sl.SLConvertible;
import io.coala.agent.AgentID;
import io.coala.capability.embody.Percept;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;

import org.eclipse.persistence.nosql.annotations.Field;

/**
 * {@link AbstractEmbodied}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
@Entity
public abstract class AbstractEmbodied<T extends AbstractEmbodied<T>> extends
		AbstractEntity<T>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	//private static final Logger LOG = Logger.getLogger(AbstractEmbodied.class);
	
	/** */
	@Field(name = "connectionsTo")
	private Set<ConnectionTo> connectionsTo = null;
	/** */
	@Field(name = "connectionsFrom")
	private Set<ConnectionFrom> connectionsFrom = null;
	/** */
	@Field(name = "directlyContains")
	private Set<DirectlyContains> directlyContains = null;
	/** */
	@Field(name = "directlyIns")
	private Set<DirectlyIn> directlyIns = null;
	
	@Field(name = "unavailable")
	private boolean unAvailable = false;
	
	
	public static class InitialASIMOVPercept implements SLConvertible<InitialASIMOVPercept>, Percept {

		/** */
		private static final long serialVersionUID = 1L;
		
		private ASIMOVNode<?> internalBelief;
		
		
		/** @see io.asimov.reasoning.sl.SLConvertible#toSL() */
		@SuppressWarnings("unchecked")
		@Override
		public <N extends ASIMOVNode<N>> N toSL()
		{
			if (internalBelief instanceof ASIMOVNode)
				return (N) internalBelief;
			else
				throw new IllegalStateException("Non SL node can not be returned");
		}

		/** @see io.asimov.reasoning.sl.SLConvertible#fromSL(jade.semantics.lang.sl.grammar.Node) */
		@Override
		public <N extends ASIMOVNode<N>> InitialASIMOVPercept fromSL(N node)
		{
			this.internalBelief = node;
			return this;
		}
		
		public static <N extends ASIMOVNode<N>> InitialASIMOVPercept toBelief(N node)
		{
			InitialASIMOVPercept result = new InitialASIMOVPercept();
			result.internalBelief = node;
			return result;
		}
		
		@Override
		public String toString(){
			return this.toSL().toString();
		}
		
		
	}
	
	@Deprecated
	public Set<ASIMOVFormula> getInitialAgentKBFormulaSet(final AgentID agentID,String container) {
		throw new IllegalStateException("getInitialAgentKBFormulaSet() method with AID is depreacted, use agentID instead.");
	}
	
	private void addCounterConnectionToBody(AbstractEmbodied<?> body, Connection connection) {
		if (connection instanceof DirectlyContains) {
			DirectlyIn counterConnection = new DirectlyIn();
			counterConnection.setSourceBody(this.getBody());
			counterConnection.setName(this.getName());
			body.getDirectlyIns().add(counterConnection);
		} else if (connection instanceof DirectlyIn) {
			DirectlyContains counterConnection = new DirectlyContains();
			counterConnection.setTargetBody(this.getBody());
			counterConnection.setName(this.getName());
			body.getDirectlyContains().add(counterConnection);
		} else if (connection instanceof ConnectionTo) {
			ConnectionFrom counterConnection = new ConnectionFrom();
			counterConnection.setSourceBody(this.getBody());
			counterConnection.setName(this.getName());
			body.getConnectionsFrom().add(counterConnection);
		} else if (connection instanceof ConnectionFrom) {
			ConnectionTo counterConnection = new ConnectionTo();
			counterConnection.setTargetBody(this.getBody());
			counterConnection.setName(this.getName());
			body.getConnectionsTo().add(counterConnection);
		} 
	}
	
	public AbstractEmbodied<?> withConnectionToBody(AbstractEmbodied<?> body) {
		ConnectionTo connection = new ConnectionTo();
		connection.setName(body.getName());
		connection.setTargetBody(body.getBody());
		getConnectionsTo().add(connection);
		addCounterConnectionToBody(body,connection);
		return this;
	}
	
	public AbstractEmbodied<?> withConnectionFromBody(AbstractEmbodied<?> body) {
		ConnectionFrom connection = new ConnectionFrom();
		connection.setName(body.getName());
		connection.setSourceBody(body.getBody());
		getConnectionsFrom().add(connection);
		addCounterConnectionToBody(body,connection);
		return this;
	}
	
	public AbstractEmbodied<?> withContainmentOfBody(AbstractEmbodied<?> body) {
		DirectlyContains connection = new DirectlyContains();
		connection.setName(body.getName());
		connection.setTargetBody(body.getBody());
		getDirectlyContains().add(connection);
		addCounterConnectionToBody(body,connection);
		return this;
	}
	
	public AbstractEmbodied<?> withContainmentInBody(AbstractEmbodied<?> body) {
		DirectlyIn connection = new DirectlyIn();
		connection.setName(body.getName());
		connection.setSourceBody(body.getBody());
		getDirectlyIns().add(connection);
		addCounterConnectionToBody(body,connection);
		return this;
	}
	

	/**
	 * @return the connectionsTo
	 */
	public Set<ConnectionTo> getConnectionsTo()
	{
		if (connectionsTo == null)
			connectionsTo = new HashSet<ConnectionTo>();
		return connectionsTo;
	}

	/**
	 * @param connectionsTo the connectionsTo to set
	 */
	public void setConnectionsTo(Set<ConnectionTo> connectionsTo)
	{
		this.connectionsTo = connectionsTo;
	}

	/**
	 * @return the connectionsFrom
	 */
	public Set<ConnectionFrom> getConnectionsFrom()
	{
		if (connectionsFrom == null)
			connectionsFrom = new HashSet<ConnectionFrom>();
		return connectionsFrom;
	}

	/**
	 * @param connectionsFrom the connectionsFrom to set
	 */
	public void setConnectionsFrom(Set<ConnectionFrom> connectionsFrom)
	{
		this.connectionsFrom = connectionsFrom;
	}

	/**
	 * @return the directlyContains
	 */
	public Set<DirectlyContains> getDirectlyContains()
	{
		if (directlyContains == null)
			directlyContains = new HashSet<DirectlyContains>();
		return directlyContains;
	}

	/**
	 * @param directlyContains the directlyContains to set
	 */
	public void setDirectlyContains(Set<DirectlyContains> directlyContains)
	{
		this.directlyContains = directlyContains;
	}

	
	/**
	 * @return the directlyContains
	 */
	public Set<DirectlyIn> getDirectlyIns()
	{
		if (directlyIns == null)
			directlyIns = new HashSet<DirectlyIn>();
		return directlyIns;
	}

	/**
	 * @param directlyContains the directlyContains to set
	 */
	public void setDirectlyIns(Set<DirectlyIn> directlyIns)
	{
		this.directlyIns = directlyIns;
	}
	
	/** */
	@Field(name = "body")
	private Body body;

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
	@SuppressWarnings("unchecked")
	public T withBody(final Body body)
	{
		this.body = body;
		return (T)this;
	}
	

	/**
	 * @return the unAvailable
	 */
	public boolean isUnAvailable() {
		return unAvailable;
	}

	/**
	 * @param unAvailable the unAvailable to set
	 */
	public void setUnAvailable(boolean unAvailable) {
		this.unAvailable = unAvailable;
	}

}
