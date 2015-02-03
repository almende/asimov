package io.asimov.microservice.negotiation;

import io.asimov.microservice.negotiation.ResourceReadyNotification.ResourceReadyListener;


/**
 * {@link ResourceReadyListeningResource}
 * 
 * @date $Date: 2014-07-10 08:44:48 +0200 (do, 10 jul 2014) $
 * @version $Revision: 977 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public interface ResourceReadyListeningResource
{
	ResourceReadyListener getResourceReadyListener();
}
