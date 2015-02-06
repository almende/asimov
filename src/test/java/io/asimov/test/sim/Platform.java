package io.asimov.test.sim;

import io.coala.bind.Binder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Platform {
	
	private static Platform THE_INSTANCE;

	/**
	 * {@link Platform} zero argument constructor
	 */
	protected Platform()
	{
	}
	
	public static synchronized Platform getInstance(){
		if (THE_INSTANCE == null)
			THE_INSTANCE = new Platform();
		return THE_INSTANCE;
	}
	
	public final Map<String,Binder> agents = Collections.synchronizedMap(new HashMap<String,Binder>());
}