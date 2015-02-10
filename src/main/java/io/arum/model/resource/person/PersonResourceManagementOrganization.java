package io.arum.model.resource.person;

import io.arum.model.resource.ResourceManagementOrganization;
import io.coala.bind.Binder;
import io.coala.log.InjectLogger;

import javax.inject.Inject;

import org.apache.log4j.Logger;

/**
 * {@link PersonResourceManagementOrganization}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public class PersonResourceManagementOrganization extends
		ResourceManagementOrganization<PersonResourceManagementWorld>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	@InjectLogger
	private Logger LOG;

	/**
	 * {@link PersonResourceManagementOrganization} constructor
	 * 
	 * @param binder
	 * @throws Exception
	 */
	@Inject
	public PersonResourceManagementOrganization(final Binder binder)
			throws Exception
	{
		super(binder);
	}

}
