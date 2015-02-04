package io.asimov.model.xml;

import io.coala.log.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.log4j.Logger;
import org.codehaus.stax2.XMLInputFactory2;
import org.eclipse.persistence.jaxb.JAXBUnmarshaller;

import rx.Observer;

/**
 * {@link XMLStreamObserver}
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 * @param <T>
 */
public class XMLStreamObserver<T> implements ResponseHandler<Void>,
		Observer<InputStream>
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger(XMLStreamObserver.class);

	/** */
	// static final javolution.xml.stream.XMLInputFactory javolutionFactory

	/** */
	static final XMLInputFactory woodstoxFactory = XMLInputFactory2
			.newInstance();
	{
		woodstoxFactory.setProperty(
				XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
		woodstoxFactory.setProperty(
				XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
		woodstoxFactory.setProperty(XMLInputFactory.IS_COALESCING,
				Boolean.FALSE);
		((XMLInputFactory2) woodstoxFactory).configureForSpeed();
	}

	/** */
	private final Observer<T> result;

	/** */
	private final JAXBUnmarshaller jaxbUnmarshaller;

	/** */
	private final Class<T> jaxbElementType;

	/** */
	private final List<String> elemPath;

	/**
	 * {@link XMLStreamObserver} constructor
	 * 
	 * @param elemName TODO enable online matching of XPath queries etc.
	 * @param result
	 */
	public XMLStreamObserver(final Observer<T> result,
			final JAXBUnmarshaller jaxbUnmarshaller,
			final Class<T> jaxbElementType, final String... elemName)
	{
		this.result = result;
		this.jaxbUnmarshaller = jaxbUnmarshaller;
		this.jaxbElementType = jaxbElementType;
		this.elemPath = Arrays.asList(elemName);
	}

	@Override
	public Void handleResponse(final HttpResponse response) throws IOException
	{
		final StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() >= 300)
			throw new HttpResponseException(statusLine.getStatusCode(),
					statusLine.getReasonPhrase());

		final HttpEntity entity = response.getEntity();
		if (entity == null)
			throw new ClientProtocolException("Response contains no content");

		onNext(entity.getContent());
		onCompleted();
		return null;
	}

	/** @see Observer#onCompleted() */
	@Override
	public void onCompleted()
	{
		this.result.onCompleted();
	}

	/** @see Observer#onError(Throwable) */
	@Override
	public void onError(final Throwable e)
	{
		e.printStackTrace();
	}

	/** @see Observer#onNext(Object) */
	@Override
	public void onNext(final InputStream t)
	{
		XMLStreamReader woodStoxReader = null; // woodstox
		try
		{

			// javolutionReader = javolutionFactory
			// .createXMLStreamReader(entity.getContent());
			// while (javolutionReader.getEventType() !=
			// javolution.xml.stream.XMLStreamConstants.END_DOCUMENT)
			// {
			// switch (javolutionReader.next())
			// {
			// case
			// javolution.xml.stream.XMLStreamConstants.START_ELEMENT:
			// if (javolutionReader.getLocalName().equals(
			// elemName))
			// {
			// final TEquipmentUsedEvent event = XmlUtil
			// .getUnmarshaller().unmarshal(
			// javolutionReader,
			// TEquipmentUsedEvent.class);
			// events.onNext(event);
			// event = null;
			// }
			// break;
			// }
			// }

			// ContentType.getOrDefault(entity).getCharset().displayName()
			woodStoxReader = woodstoxFactory.createXMLStreamReader(t);
			final Deque<String> path = new LinkedList<>();
			while (woodStoxReader.getEventType() != XMLStreamConstants.END_DOCUMENT)
			{
				LOG.trace("xml stream event: "
						+ XMLStreamEventTypeEnum.values()[woodStoxReader
								.getEventType() - 1]
						+ (woodStoxReader.hasName() ? " "
								+ woodStoxReader.getLocalName() : "")
						+ ", path: " + path);
				switch (woodStoxReader.next())
				{
				case XMLStreamConstants.START_ELEMENT:
					path.offerLast(woodStoxReader.getLocalName());
					if (path.equals(this.elemPath))
					{
						@SuppressWarnings("unchecked")
						final JAXBElement<T> elem = (JAXBElement<T>) this.jaxbUnmarshaller
								.unmarshal(woodStoxReader, this.jaxbElementType);
						path.pollLast();
						this.result.onNext(elem.getValue());
					}
					break;
				case XMLStreamConstants.END_ELEMENT:
					path.pollLast();
				}
			}

		} catch (final Exception e)
		{
			this.result.onError(e);
		} finally
		{
			// if (javolutionReader != null)
			// try
			// {
			// javolutionReader.close();
			// } catch (final javolution.xml.stream.XMLStreamException
			// e)
			// {
			// e.printStackTrace();
			// }
			if (woodStoxReader != null)
				try
				{
					woodStoxReader.close();
				} catch (final javax.xml.stream.XMLStreamException e)
				{
					e.printStackTrace();
				}

			try
			{
				t.close();
			} catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}