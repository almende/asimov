package io.arum.model.resource.supply;

import io.arum.model.resource.ResourceManagementOrganization;
import io.coala.bind.Binder;
import io.coala.log.InjectLogger;

import javax.inject.Inject;

import org.apache.log4j.Logger;

/**
 * {@link MaterialResourceManagementOrganization}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class MaterialResourceManagementOrganization extends
		ResourceManagementOrganization<MaterialResourceManagementWorld>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	@InjectLogger
	private Logger LOG;

	/**
	 * {@link MaterialResourceManagementOrganization} constructor
	 * 
	 * @param binder
	 * @throws Exception
	 */
	@Inject
	public MaterialResourceManagementOrganization(final Binder binder)
			throws Exception
	{
		super(binder);
	}
	

}
