package io.asimov.model.process;

import io.coala.agent.AgentID;
import io.coala.time.SimDuration;

import java.util.Map;
import java.util.Map.Entry;

/**
 * {@link IDistanceMatrix}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public interface IDistanceMatrix extends Map<Entry<AgentID, AgentID>, SimDuration>
{

}
