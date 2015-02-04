package io.asimov.model;

import io.coala.bind.Binder;
import io.coala.enterprise.fact.CoordinationFact;
import io.coala.enterprise.role.AbstractExecutor;

public class RouteInitiator extends AbstractExecutor<CoordinationFact> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5363461634459923368L;

	protected RouteInitiator(Binder binder) {
		super(binder);
	}

	@Override
	protected void onRequested(CoordinationFact request) {
		// TODO Auto-generated method stub
		throw new IllegalStateException("Not yet implemented, should schedule an at assemblyline event.");
	}

}
