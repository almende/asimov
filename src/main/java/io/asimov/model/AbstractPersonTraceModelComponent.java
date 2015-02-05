package io.asimov.model;

import io.coala.dsol.util.AbstractDsolModelComponent;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * {@link AbstractPersonTraceModelComponent}
 * 
 * @date $Date: 2014-05-07 11:59:38 +0200 (Wed, 07 May 2014) $
 * @version $Revision: 880 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public abstract class AbstractPersonTraceModelComponent extends
		AbstractDsolModelComponent<DEVSSimulatorInterface, PersonTraceModel>
		implements PersonTraceModelComponent
{

	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link AbstractPersonTraceModelComponent} constructor
	 * 
	 * @param model
	 * @param name
	 */
	public AbstractPersonTraceModelComponent(final PersonTraceModel model,
			final String name)
	{
		super(model, name);
	}

}
