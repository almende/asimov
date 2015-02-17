package io.asimov.util.extrapolator;

import io.arum.model.events.MaterialEvent;
import io.arum.model.events.MovementEvent;
import io.asimov.agent.scenario.Replication;
import io.asimov.db.Datasource;
import io.asimov.model.TraceService;
import io.asimov.model.events.ActivityEvent;
import io.asimov.model.events.EventType;
import io.coala.bind.Binder;
import io.coala.bind.BinderFactory;
import io.coala.capability.replicate.ReplicationConfig;
import io.coala.exception.CoalaException;
import io.coala.log.LogUtil;
import io.coala.model.ModelComponentIDFactory;
import io.coala.time.SimDuration;
import io.coala.time.SimTime;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.eaio.uuid.UUID;

/**
 * {@link EventExtrapolator}
 * 
 * @date $Date$
 * @version $Revision$
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public class EventExtrapolator
{
	
	private static final Logger LOG = LogUtil.getLogger(EventExtrapolator.class);
	
	private static Map<String,String> pInstanceExpMap = new HashMap<String,String>();
	
	public static void extrapolator(final String replicationID, SimTime offset, final SimDuration baseDuration, final SimDuration targetDuration) throws Exception {
		Binder extrapolator;
		if (offset == null)
			offset = SimTime.ZERO;
		try
		{
			extrapolator = BinderFactory.Builder.fromFile("asimov.properties").withProperty(ReplicationConfig.class, ReplicationConfig.MODEL_NAME_KEY, replicationID).build().create("extrapolator");
		} catch (CoalaException e)
		{
			LOG.error("Failed to create extrapolator", e);
			return;
		}
		LOG.info("Querying for events");
		for (MovementEvent e : extrapolator.inject(Datasource.class).findMovementEvents()) {
			LOG.info("Got event: "+e);
			if (e.getExecutionTime().isOnOrAfter(offset) && e.getExecutionTime().isBefore(offset.plus(baseDuration))) {
				LOG.info("Matched event: "+e);
				for (int f = 1; (e.getExecutionTime().plus(baseDuration)).minus(offset).isBefore(offset.plus(targetDuration)); f++) {
					SimTime newTime = e.getExecutionTime().plus(baseDuration);
					if (newTime.getMillis() < 0)
						throw new Exception("Something went wrong");
					if (e.getType().equals(EventType.ARIVE_AT_ASSEMBLY) || e.getType().equals(EventType.LEAVE_ASSEMBLY)) {
						e = (MovementEvent) TraceService.getInstance(replicationID).saveEvent(extrapolator.inject(Datasource.class), e.getProcessID(), mapProcessInstanceId(e.getProcessInstanceID(),f), e.getActivity(), e.getActivityInstanceId(), extrapolator.inject(ModelComponentIDFactory.class).createAgentID(e.getPerson().getName()), e.getType(), newTime, null, e.getAssemblyLine().getName());
						LOG.info("Extrapolated movement event: "+e);
					} else {
						LOG.warn("Unknown event type "+e.getClass().getName());
					}
					
				}
			}
		}
		for (ActivityEvent e : extrapolator.inject(Datasource.class).findActivityEvents()) {
			LOG.info("Got event: "+e);
			if (e.getExecutionTime().isOnOrAfter(offset) && e.getExecutionTime().isBefore(offset.plus(baseDuration))) {
				LOG.info("Matched event: "+e);
				for (int f = 1; (e.getExecutionTime().plus(baseDuration)).minus(offset).isBefore(offset.plus(targetDuration)); f++) {
					SimTime newTime = e.getExecutionTime().plus(baseDuration);
					if (newTime.getMillis() < 0)
						throw new Exception("Something went wrong");
					if (e.getType().equals(EventType.START_ACTIVITY) || e.getType().equals(EventType.STOP_ACTIVITY)) {
						//e.withExecutionTime(newTime).withProcessInstanceID(mapProcessInstanceId(e.getProcessInstanceID(),f));
						//extrapolator.inject(Datasource.class).save(new ActivityEvent().fromXML(((ActivityEvent) e).toXML(),TimeUnit.MILLIS,SimTime.ZERO.calcOffset()));
						e = (ActivityEvent) TraceService.getInstance(replicationID).saveEvent(extrapolator.inject(Datasource.class), e.getProcessID(), mapProcessInstanceId(e.getProcessInstanceID(),f), e.getActivity(), e.getActivityInstanceId(), extrapolator.inject(ModelComponentIDFactory.class).createAgentID(e.getPerson().getName()), e.getType(), newTime, null, e.getAssemblyLineName());
						LOG.info("Extrapolated activity event: "+e);
					} else {
						LOG.warn("Unknown event type "+e.getClass().getName());
					}
					
				}
			}
		}
		for (MaterialEvent e : extrapolator.inject(Datasource.class).findMaterialEvents()) {
			LOG.info("Got event: "+e);
			if (e.getExecutionTime().isOnOrAfter(offset) && e.getExecutionTime().isBefore(offset.plus(baseDuration))) {
				LOG.info("Matched event: "+e);
				for (int f = 1; (e.getExecutionTime().plus(baseDuration)).minus(offset).isBefore(offset.plus(targetDuration)); f++) {
					SimTime newTime = e.getExecutionTime().plus(baseDuration);
					if (newTime.getMillis() < 0)
						throw new Exception("Something went wrong");
					if (e.getType().equals(EventType.START_USE_MATERIAL) || e.getType().equals(EventType.STOP_USE_MATERIAL)) {
//						e.withExecutionTime(newTime).withProcessInstanceID(mapProcessInstanceId(e.getProcessInstanceID(),f));
//						extrapolator.inject(Datasource.class).save(new EquipmentEvent().fromXML(((EquipmentEvent) e).toXML(),TimeUnit.MILLIS,SimTime.ZERO.calcOffset()));
						e = (MaterialEvent) TraceService.getInstance(replicationID).saveEvent(extrapolator.inject(Datasource.class), e.getProcessID(), mapProcessInstanceId(e.getProcessInstanceID(),f), e.getActivity(), e.getActivityInstanceId(), extrapolator.inject(ModelComponentIDFactory.class).createAgentID(e.getPerson().getName()), e.getType(), newTime, e.getMaterial(), e.getAssemblyLine().getName());
						LOG.info("Extrapolated equipment event: "+e);
					} else {
						LOG.warn("Unknown event type "+e.getClass().getName());
					}
					
				}
			}
		}
		
		Replication r = extrapolator.inject(Datasource.class).findReplication();
		r.setDurationMS(targetDuration.getMillis());
		extrapolator.inject(Datasource.class).update(r);
		LOG.info("Extrapolated "+pInstanceExpMap.keySet().size()+" process instances");
	}

	/**
	 * @param processInstanceID
	 * @param f
	 * @return
	 */
	private static String mapProcessInstanceId(String processInstanceID, int f)
	{
		final String token = f+">>>"+processInstanceID;
		String result = pInstanceExpMap.get(token);
		if (result == null) {
			result = new UUID().toString();
			pInstanceExpMap.put(token, result);
		}
		return result;
	}
}
