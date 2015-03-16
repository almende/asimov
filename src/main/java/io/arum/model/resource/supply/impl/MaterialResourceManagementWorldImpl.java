/* $Id: MaterialResourceManagementWorldImpl.java 1048 2014-09-01 09:53:05Z krevelen $
 * $URL: https://redmine.almende.com/svn/a4eesim/trunk/adapt4ee-newsim/src/main/java/eu/a4ee/model/resource/impl/MaterialResourceManagementWorldImpl.java $
 * 
 * Part of the EU project Adapt4EE, see http://www.adapt4ee.eu/
 * 
 * @license
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright (c) 2010-2014 Almende B.V. 
 */
package io.arum.model.resource.supply.impl;

import io.arum.model.events.MaterialEvent;
import io.arum.model.resource.ARUMResourceType;
import io.arum.model.resource.AbstractResourceManagementWorld;
import io.arum.model.resource.supply.Material;
import io.arum.model.resource.supply.MaterialResourceManagementWorld;
import io.asimov.db.Datasource;
import io.asimov.model.events.EventType;
import io.coala.bind.Binder;
import io.coala.log.InjectLogger;
import io.coala.model.ModelComponentIDFactory;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * {@link MaterialResourceManagementWorldImpl}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public class MaterialResourceManagementWorldImpl extends
		AbstractResourceManagementWorld<Material> implements
		MaterialResourceManagementWorld
{

	/** */
	private static final long serialVersionUID = 1L;

	@InjectLogger
	private Logger LOG;

	
	/** */
	private Subject<MaterialEvent, MaterialEvent> usage = PublishSubject
			.create();

	/**
	 * {@link MaterialResourceManagementWorldImpl} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected MaterialResourceManagementWorldImpl(final Binder binder)
	{
		super(binder);
	}

	@Override
	public void initialize() throws Exception
	{
		super.initialize();
		this.entityType = ARUMResourceType.MATERIAL;
		this.entity = getBinder().inject(Datasource.class).findMaterialByID(
				getOwnerID().getValue());
		setCurrentLocation(getOwnerID());
	}

	/** @see eu.a4ee.model.resource.MaterialResourceManagementWorld#onUsage() */
	@Override
	public Observable<MaterialEvent> onUsage()
	{
		return this.usage.asObservable();
	}

	/**
	 * @see eu.a4ee.model.resource.OccupantResourceManagementWorld#performUsageChange(java.lang.String,
	 *      java.lang.String, eu.a4ee.model.bean.EventType)
	 */
	@Override
	public void performUsageChange(final String processID,
			final String processInstanceID, final String activityName, final String activityInstanceId,
			final String equipmentName, final String occupantName, final String assemblyLineRef,
			final EventType eventType) throws Exception
	{
		LOG.info("fire 3!");
		fireAndForget(processID, processInstanceID, activityName, activityInstanceId, eventType,
				getBinder().inject(ModelComponentIDFactory.class)
						.createAgentID(occupantName), equipmentName,
				assemblyLineRef, this.usage);
	}


	

	

}
