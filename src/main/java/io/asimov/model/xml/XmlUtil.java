package io.asimov.model.xml;

import io.asimov.agent.scenario.Context;
import io.asimov.model.xml.moxy.MOXyXmlContextID;
import io.asimov.xml.BPMImport;
import io.asimov.xml.ListOfEvents;
import io.asimov.xml.ListOfProcesses;
import io.asimov.xml.ListOfRoles;
import io.asimov.xml.SimulationFile;
import io.asimov.xml.TDistribution;
import io.coala.log.LogUtil;
import io.coala.xml.XmlContextID;

import java.io.StringWriter;
import java.util.Date;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.jaxb.JAXBMarshaller;
import org.eclipse.persistence.jaxb.JAXBUnmarshaller;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import rx.Observable;
import rx.functions.Action0;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

//import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * {@link XmlUtil}
 * 
 * @date $Date: 2014-11-04 11:22:27 +0100 (di, 04 nov 2014) $
 * @version $Revision: 1107 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * @author <a href="mailto:Ludo@almende.org">Ludo</a>
 */
public class XmlUtil extends io.coala.xml.XmlUtil
{

	/** */
	private static final Logger LOG = LogUtil.getLogger(XmlUtil.class);

	/**
	 * Identifies the cached {@link JAXBContext} of {@link JAXBElement}s
	 * generated based on CIM schemata
	 */
	public static final XmlContextID<io.asimov.xml.ObjectFactory> CIM = MOXyXmlContextID
			.of(io.asimov.xml.ObjectFactory.class,
					SimulationFile.class, Context.class, BPMImport.class, ListOfEvents.class,
					ListOfRoles.class,
					ListOfProcesses.class, TDistribution.class);

	/**
	 * Identifies the cached {@link JAXBContext} of {@link JAXBElement}s
	 * generated based on CIM schemata
	 */
	public static final XmlContextID<io.asimov.xml.ObjectFactory> CONTEXT = MOXyXmlContextID
			.of(io.asimov.xml.ObjectFactory.class);

	/** */
	public static final String BIND_PROPERTY_NAME = "com.sun.xml.internal.bind.namespacePrefixMapper";

	/** */
	public static final String SUN_NAMESPACE_PREFIX_MAPPER = "com.sun.xml.bind.namespacePrefixMapper";

	/** */
	private static XPathFactory xPathFactory;

	public static XPathFactory getXPathFactory()
	{
		if (xPathFactory == null)
			xPathFactory = XPathFactory.newInstance();
		return xPathFactory;
	}

	/** */
	private static DocumentBuilderFactory domFactory;

	public static DocumentBuilderFactory getDOMBuilderFactory()
	{
		if (domFactory == null)
		{
			domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true);
		}
		return domFactory;
	}

	public static JAXBContext getCIMContext() throws JAXBException
	{
		return (JAXBContext) getContextCache(CIM).getJAXBContext();
	}

	/**
	 * @return
	 * @throws JAXBException
	 * @throws javax.xml.bind.JAXBException
	 */
	public static JAXBMarshaller getCIMMarshaller() throws JAXBException
	{
		return (JAXBMarshaller) getContextCache(CIM).getMarshaller();
	}

	/**
	 * @return
	 * @throws JAXBException
	 */
	public static JAXBUnmarshaller getCIMUnmarshaller() throws JAXBException
	{
		return (JAXBUnmarshaller) getContextCache(CIM).getUnmarshaller();
	}

	public static Date toDate(final XMLGregorianCalendar date)
	{
		return toDateTime(date).toDate();
	}

	public static DateTime toDateTime(final XMLGregorianCalendar date)
	{
		final DateTimeZone timeZone = date.getTimezone() == DatatypeConstants.FIELD_UNDEFINED ? DateTimeZone
				.getDefault() : DateTimeZone
				.forOffsetMillis(date.getTimezone() * 60 * 1000);

		return new DateTime(
				date.getYear(),
				date.getMonth(),
				date.getDay(),
				date.getHour(),
				date.getMinute(),
				date.getSecond(),
				date.getMillisecond() >= 0 && date.getMillisecond() < 1000 ? date
						.getMillisecond() : 0, timeZone);
	}

	public static XMLGregorianCalendar toXML(final DateTime date)
			throws DatatypeConfigurationException
	{
		final XMLGregorianCalendar result = getDatatypeFactory()
				.newXMLGregorianCalendar();
		result.setYear(date.getYear());
		result.setMonth(date.getMonthOfYear());
		result.setDay(date.getDayOfMonth());
		result.setTime(date.getHourOfDay(), date.getMinuteOfHour(),
				date.getSecondOfMinute(), date.getMillisOfSecond());
		result.setTimezone(date.getZone().toTimeZone().getRawOffset() / 1000 / 60);
		// result.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
		return result;
	}

	/**
	 * @param duration
	 * @param startInstant
	 * @return the {@link Duration}
	 */
	public static Duration toDuration(
			final javax.xml.datatype.Duration duration,
			final XMLGregorianCalendar startInstant)
	{
		return toInterval(duration, startInstant).toDuration();
	}

	/**
	 * @param duration
	 * @param startInstant
	 * @return the {@link Interval}
	 */
	public static Interval toInterval(
			final javax.xml.datatype.Duration duration,
			final XMLGregorianCalendar startInstant)
	{
		return toInterval(duration, toDateTime(startInstant));
	}

	/**
	 * @param duration
	 * @param startInstant
	 * @return the {@link Interval}
	 */
	public static Interval toInterval(
			final javax.xml.datatype.Duration duration, final DateTime offset)
	{
		return new Interval(offset, offset.plus(duration.getTimeInMillis(offset
				.toDate())));

	}

	public static javax.xml.datatype.Duration durationFromLong(final long millis)
	{
		return getDatatypeFactory().newDuration(millis);
	}

	public static long gDurationToLong(javax.xml.datatype.Duration duration)
	{
		return duration.getTimeInMillis(new Date(0));
	}


	/**
	 * @param xml
	 * @throws Exception
	 */
	public static String toString(final Object xml) throws Exception
	{
		final StringWriter sw = new StringWriter();
		getCIMMarshaller().marshal(xml, sw);
		return sw.toString();

	}

	/**
	 * FIXME non-streaming evaluation based on the JDK's Xerces implementation
	 * for parsing DOM, SAX and SAX2. This currently only supports XPath 1.0,
	 * e.g. {@code "//person/name/text()"} but not XPath 2.0, e.g.
	 * {@code "//element(*, MyComplexType)"}
	 */
	public static Observable<Node> evaluate(final Resource xmlResource,
			final String xPathExpression)
	{
		final Subject<Node, Node> result = ReplaySubject.create();
		Schedulers.io().createWorker().schedule(new Action0()
		{
			@Override
			public void call()
			{
				try
				{
					final Document doc = getDOMBuilderFactory()
							.newDocumentBuilder().parse(
									xmlResource.asInputStream());

					final XPathExpression expr = getXPathFactory().newXPath()
							.compile(xPathExpression);

					final NodeList nodes = (NodeList) expr.evaluate(doc,
							XPathConstants.NODESET);

					LOG.info("Got DOM nodes: " + nodes.getLength());

					for (int i = 0; i < nodes.getLength(); i++)
						result.onNext(nodes.item(i));
				} catch (final Exception e)
				{
					result.onError(e);
				}
				result.onCompleted();
			}
		});
		return result.asObservable();
	}

	/**
	 * See also XSLT streaming transformation, e.g. with <a
	 * href="http://www.saxonica.com/">SAXON HE</a>
	 * 
	 * {@code
	 * <dependency>
	 * 	<groupId>net.sf.saxon</groupId>
	 * 	<artifactId>Saxon-HE</artifactId>
	 * 	<version>9.5.1-5</version>
	 * </dependency> }
	 * 
	 * @param restCall
	 * @param jaxbElementType
	 * @param xPath
	 * @return
	 * @throws Exception
	 */
	public static <T> Observable<T> getStream(final Resource resource,
			final JAXBUnmarshaller jaxbUnmarshaller,
			final Class<T> jaxbElementType, final String... xPath)
			throws Exception
	{
		// TODO consider the STX (stream transform for XML) for xPath

		final Subject<T, T> result = ReplaySubject.create();

		final XMLStreamObserver<T> obs = new XMLStreamObserver<T>(result,
				jaxbUnmarshaller, jaxbElementType, xPath);
		if (resource.isFile())
		{
			obs.onNext(resource.asInputStream());
			obs.onCompleted();
		} else
			HttpClients.createDefault().execute(new HttpGet(resource.getURI()),
					obs);

		return result.asObservable();
	}
}
