package io.arum.model.resource.assemblyline;

import io.arum.model.resource.ResourceManagementOrganization;
import io.coala.bind.Binder;
import io.coala.log.InjectLogger;

import javax.inject.Inject;

import org.apache.log4j.Logger;

/**
 * {@link AssemblyLineResourceManagementOrganization}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class AssemblyLineResourceManagementOrganization extends
		ResourceManagementOrganization<AssemblyLineResourceManagementWorld>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	@InjectLogger
	private Logger LOG;

	/**
	 * {@link AssemblyLineResourceManagementOrganization} constructor
	 * 
	 * @param binder
	 * @throws Exception
	 */
	@Inject
	public AssemblyLineResourceManagementOrganization(final Binder binder)
			throws Exception
	{
		super(binder);
	}

}
