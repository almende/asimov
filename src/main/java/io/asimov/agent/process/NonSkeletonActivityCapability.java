package io.asimov.agent.process;

import io.coala.agent.AgentID;
import io.coala.capability.CapabilityFactory;
import io.coala.capability.CapabilityID;
import io.coala.json.JSONConvertible;
import io.coala.json.JsonUtil;
import io.coala.model.ModelComponent;
import io.coala.time.SimTime;

import java.io.Serializable;

import rx.Observable;

import com.eaio.uuid.UUID;

/**
 * {@link NonSkeletonActivityCapability}
 * 
 * @date $Date$
 * @version $Revision$
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public interface NonSkeletonActivityCapability extends ModelComponent<CapabilityID>  
{
	
	public final static String LEAVE_SITE_WHEN_ON_IT = "LEAVE_SITE_WHEN_ON_IT";

	
	interface Factory extends CapabilityFactory<NonSkeletonActivityCapability>
	{
		// empty
	}
	
	/**
	 * 
	 * {@link NonSkeletonActivityCapabilityStatus} annotates the current status
	 * of the non-skeleton activity.
	 * 
	 * @date $Date$
	 * @version $Revision$
	 * @author <a href="mailto:suki@almende.org">suki</a>
	 *
	 */
	public enum NonSkeletonActivityCapabilityStatus {
		NonSkeletonActivity_pending,
		NonSkeletonActivity_traveling,
		NonSkeletonActivity_executing,
		NonSkeletonActivity_done,
		NonSkeletonActivity_failed,
		NonSkeletonActivity_cancelled;
		
		 @Override
        public String toString() {
                String value;
                switch (this) {
                        case NonSkeletonActivity_cancelled:
                                value = "cancelled";
                                break;
                        case NonSkeletonActivity_executing:
                                value = "executing";
                                break;
                        case NonSkeletonActivity_done:
                                value = "done";
                                break;
                        case NonSkeletonActivity_failed:
                                value = "failed";
                                break;
                        case NonSkeletonActivity_pending:
                                value = "pending";
                                break;
                        case NonSkeletonActivity_traveling:
                                value = "traveling";
                                break;
                        default : {
                                value = "unknown";
                        }
                }
                return value;
            }

	};
	
	public class NonSkeletonActivityState implements JSONConvertible<NonSkeletonActivityState>, Serializable{

		
		/** */
		private static final long serialVersionUID = 6510580984235314946L;
		
		final AgentID executor;
		final AgentID scenarioAgentID;
		final NonSkeletonActivityCapabilityStatus status;
		final String nonSkeletonActivityName;
		final String skeltetonActivityId;
		
		public NonSkeletonActivityState(final AgentID executor, 
				final NonSkeletonActivityCapabilityStatus status, 
				final String nonSkeletonActivityName,
				final AgentID scenarioAgentID) {
			this.executor = executor;
			this.scenarioAgentID = scenarioAgentID;
			this.status = status;
			this.nonSkeletonActivityName = nonSkeletonActivityName;
			this.skeltetonActivityId = new UUID().toString();
		}
		
		private NonSkeletonActivityState(final NonSkeletonActivityState previous,
				final NonSkeletonActivityCapabilityStatus status) {
			this.skeltetonActivityId = previous.skeltetonActivityId;
			this.scenarioAgentID = previous.scenarioAgentID;
			this.executor = previous.executor;
			this.status = status;
			this.nonSkeletonActivityName = previous.nonSkeletonActivityName;
		}
		
		public NonSkeletonActivityState transitToStatus(final NonSkeletonActivityCapabilityStatus status) {
			// TODO check if transition is allowed
			return new NonSkeletonActivityState(this, status);
		}
		
		/**
		 * @return the executor
		 */
		public AgentID getExecutor()
		{
			return this.executor;
		}

		/**
		 * @return the scenarioAgentID
		 */
		public AgentID getScenarioAgentID()
		{
			return this.scenarioAgentID;
		}

		/**
		 * @return the status
		 */
		public NonSkeletonActivityCapabilityStatus getStatus()
		{
			return this.status;
		}

		/**
		 * @return the nonSkeletonActivityName
		 */
		public String getNonSkeletonActivityName()
		{
			return this.nonSkeletonActivityName;
		}

		/**
		 * @return the skeltetonActivityId
		 */
		public String getSkeltetonActivityId()
		{
			return this.skeltetonActivityId;
		}


		/** @see io.coala.json.JSONConvertible#toJSON() */
		@Override
		public String toJSON()
		{
			return JsonUtil.toPrettyJSON(this);
		}

		/** @see io.coala.json.JSONConvertible#fromJSON(java.lang.String) */
		@Override
		public NonSkeletonActivityState fromJSON(String jsonValue)
		{
			throw new IllegalStateException("Unimplemented method: fromJSON");
		}
		
		@Override
       public String toString() 
	   {
               return getNonSkeletonActivityName()+"("+getSkeltetonActivityId()+")"+":"+getStatus();
       }

	}

	
	Observable<NonSkeletonActivityState> call(final String type, final String... arguments);

	SimTime getTime();

	/**
	 * @param state callback travel state
	 */
	void onLeftSite(NonSkeletonActivityState state);

	/**
	 * 
	 */
	void onLeftSite();

}
