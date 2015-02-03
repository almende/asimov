package io.asimov.microservice.negotiation;

import io.coala.agent.AgentID;
import io.coala.bind.Binder;

/**
 * {@link AgentServiceProxy}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
public class AgentServiceProxy
{
	private final Binder binder;

	/**
	 * @return the binder
	 */
	public Binder getBinder()
	{
		return this.binder;
	}

	public AgentServiceProxy(final Binder b)
	{
		this.binder = b;
	}

	public AgentID getID()
	{
		return getBinder().getID();
	}

}