package io.asimov.agent.process;

/**
 * This exception is thrown when a role is not yet initialized and a request for data has been made.
 * The way to handle this exception is to wait until it is initialized.
 * 
 * {@link NotYetInitializedException}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 *
 */
public class NotYetInitializedException extends Exception
{

	/** */
	private static final long serialVersionUID = 1L;
	

}
