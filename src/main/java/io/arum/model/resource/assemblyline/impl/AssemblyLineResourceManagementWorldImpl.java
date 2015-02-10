package io.arum.model.resource.assemblyline.impl;

import io.arum.model.resource.ARUMResourceType;
import io.arum.model.resource.AbstractResourceManagementWorld;
import io.arum.model.resource.assemblyline.AssemblyLine;
import io.arum.model.resource.assemblyline.AssemblyLineResourceManagementWorld;
import io.asimov.db.Datasource;
import io.coala.bind.Binder;
import io.coala.log.InjectLogger;

import javax.inject.Inject;

import org.apache.log4j.Logger;

/**
 * {@link AssemblyLineResourceManagementWorldImpl}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public class AssemblyLineResourceManagementWorldImpl extends
		AbstractResourceManagementWorld<AssemblyLine> implements
		AssemblyLineResourceManagementWorld
{

	/** */
	private static final long serialVersionUID = 1L;

	@InjectLogger
	private Logger LOG;

	

	
	/**
	 * {@link AssemblyLineResourceManagementWorldImpl} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected AssemblyLineResourceManagementWorldImpl(final Binder binder)
	{
		super(binder);
	}

	@Override
	public void initialize() throws Exception
	{
		super.initialize();
		this.entityType = ARUMResourceType.ASSEMBLY_LINE;
		this.entity = getBinder().inject(Datasource.class).findAssemblyLineByID(
				getOwnerID().getValue());
		setCurrentLocation(getOwnerID());
	}

	

	

	

	

}
