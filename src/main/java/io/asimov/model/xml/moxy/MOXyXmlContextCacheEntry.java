package io.asimov.model.xml.moxy;

import io.asimov.model.xml.XmlNamespaceMapper;
import io.asimov.model.xml.XmlUtil;
import io.coala.exception.CoalaExceptionFactory;
import io.coala.xml.XmlContext;
import io.coala.xml.XmlContextID;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;

import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBMarshaller;
import org.eclipse.persistence.jaxb.JAXBUnmarshaller;

/**
 * {@link MOXyXmlContextCacheEntry} depends on the <a
 * href="http://www.eclipse.org/eclipselink/">EclipseLink</a> JAXB
 * implementation (the <a
 * href="http://www.eclipse.org/eclipselink/moxy.php">MOXy</a> extension)
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public class MOXyXmlContextCacheEntry<T> extends XmlContext<T>
{

	/** */
	private JAXBContext _Context = null;

	/** */
	private JAXBMarshaller _Marshaller = null;

	/** */
	private JAXBUnmarshaller _Unmarshaller = null;

	/**
	 * {@link MOXyXmlContextCacheEntry} constructor
	 * 
	 * @param contextID
	 * @param validationEventHandler
	 */
	protected MOXyXmlContextCacheEntry(final XmlContextID<?> contextID,
			final ValidationEventHandler validationEventHandler)
	{
		super(contextID, validationEventHandler);
	}

	/**
	 * @return the {@link JAXBContext}
	 */
	public synchronized JAXBContext getJAXBContext()
	{
		if (this._Context == null)
			try
			{
				this._Context = (getID().getObjectTypes() != null && getID()
						.getObjectTypes().length > 0) ? (JAXBContext) JAXBContextFactory
						.createContext(getID().getObjectTypes(), null)
						: (JAXBContext) JAXBContextFactory.createContext(
								getID().getObjectFactoryType().getPackage()
										.getName(), Thread.currentThread()
										.getContextClassLoader());
			} catch (final JAXBException e)
			{
				throw CoalaExceptionFactory.INCONSTRUCTIBLE.createRuntime(e,
						JAXBContext.class, null);
			}

		return this._Context;
	}

	/**
	 * @return the cached {@link Marshaller}
	 */
	public synchronized JAXBMarshaller getMarshaller()
	{
		if (this._Marshaller == null)
			try
			{
				this._Marshaller = (JAXBMarshaller) getJAXBContext()
						.createMarshaller();
				this._Marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
						true);
				this._Marshaller.setProperty(
						XmlUtil.SUN_NAMESPACE_PREFIX_MAPPER,
						new XmlNamespaceMapper());
			} catch (final JAXBException e)
			{
				throw CoalaExceptionFactory.INCONSTRUCTIBLE.createRuntime(e,
						Marshaller.class, null);
			}
		return this._Marshaller;
	}

	/**
	 * @return the cached {@link Unmarshaller}
	 */
	public synchronized JAXBUnmarshaller getUnmarshaller()
	{
		if (_Unmarshaller == null)
			try
			{
				_Unmarshaller = (JAXBUnmarshaller) getJAXBContext()
						.createUnmarshaller();
				_Unmarshaller.setEventHandler(getValidationEventHandler());
			} catch (final JAXBException e)
			{
				throw CoalaExceptionFactory.INCONSTRUCTIBLE.createRuntime(e,
						Marshaller.class, null);
			}

		return _Unmarshaller;
	}

	/**
	 * @param contextID
	 * @param validationEventHandler
	 * @return a new {@link MOXyXmlContextCacheEntry} instance
	 */
	public static <T> MOXyXmlContextCacheEntry<T> of(
			final XmlContextID<?> contextID,
			final ValidationEventHandler validationEventHandler)
	{
		return new MOXyXmlContextCacheEntry<T>(contextID,
				validationEventHandler);
	}

}