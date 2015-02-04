package io.asimov.model.xml.moxy;

import io.coala.xml.XmlContextID;

import javax.xml.bind.ValidationEventHandler;

/**
 * {@link MOXyXmlContextID} depends on the <a
 * href="http://www.eclipse.org/eclipselink/">EclipseLink</a> JAXB
 * implementation (the <a
 * href="http://www.eclipse.org/eclipselink/moxy.php">MOXy</a> extension)
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
public class MOXyXmlContextID<T> extends XmlContextID<T>
{

	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link XmlContextIDImpl} constructor
	 * 
	 * @param objectFactoryType the type of ObjectFactory for this context (if
	 *            any, {@code null} otherwise)
	 * @param objectTypes the element object types to include in the context (if
	 *            any)
	 */
	protected MOXyXmlContextID(final Class<T> objectFactoryType,
			final Class<?>... objectTypes)
	{
		super(objectFactoryType, objectTypes);
	}

	@Override
	public MOXyXmlContextCacheEntry<T> newContextCacheEntry(
			final ValidationEventHandler validationEventHandler)
	{
		return MOXyXmlContextCacheEntry.of(this, validationEventHandler);
	}

	/**
	 * @param clazz the type of ObjectFactory
	 * @return the new {@link MOXyXmlContextID}
	 */
	public static <T> MOXyXmlContextID<T> of(final Class<T> clazz,
			final Class<?>... objectTypes)
	{
		return new MOXyXmlContextID<T>(clazz, objectTypes);
	}
}
