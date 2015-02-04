package io.asimov.model;

/**
 * {@link XMLConvertible}
 * 
 * @date $Date: 2013-07-31 14:20:55 +0200 (Wed, 31 Jul 2013) $
 * @version $Revision: 321 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public interface XMLConvertible<X extends Object, T extends XMLConvertible<?, ?>>
{

	/**
	 * @param xmlBean the XML bean to convert
	 * @return the parsed bean
	 */
	T fromXML(X xmlBean);

	/** @return the XML bean representation */
	X toXML();

}
