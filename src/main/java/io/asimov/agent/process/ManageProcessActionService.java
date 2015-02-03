package io.asimov.agent.process;

import io.asimov.agent.resource.ActivityParticipation;
import io.coala.capability.CapabilityFactory;
import io.coala.enterprise.role.Executor;

/**
 * {@link ManageProcessActionService}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public interface ManageProcessActionService extends Executor<ProcessCompletion>
{

	/**
	 * {@link Factory}
	 *
	 * @version $Revision: 1048 $
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 */
	interface Factory extends CapabilityFactory<ManageProcessActionService>
	{
		// empty
	}

	/**
	 * @param cause the {@link ProcessCompletion.Request} that triggered this process management call/request
	 */
	void manageProcessInstanceForType(ProcessCompletion.Request cause);

	/**
	 * @param result
	 */
	void notifyActivityParticipationResult(ActivityParticipation.Result result);
	
}
