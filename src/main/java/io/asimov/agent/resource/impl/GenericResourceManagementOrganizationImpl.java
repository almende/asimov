package io.asimov.agent.resource.impl;

import io.asimov.agent.resource.GenericResourceManagementWorld;
import io.asimov.model.resource.ResourceManagementOrganization;
import io.coala.bind.Binder;
import io.coala.log.InjectLogger;

import javax.inject.Inject;

import org.apache.log4j.Logger;

/**
 * 
 * @author suki
 *
 */
public class GenericResourceManagementOrganizationImpl extends
		ResourceManagementOrganization<GenericResourceManagementWorld>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	@InjectLogger
	private Logger LOG;

	/**
	 * {@link GenericResourceManagementOrganizationImpl} constructor
	 * 
	 * @param binder
	 * @throws Exception
	 */
	@Inject
	public GenericResourceManagementOrganizationImpl(final Binder binder)
			throws Exception
	{
		super(binder);
	}

}
