package io.asimov.model.xml;

import io.coala.log.LogUtil;

import java.util.Map;

import org.aeonbits.owner.util.Collections;
import org.apache.log4j.Logger;
import org.eclipse.persistence.oxm.NamespacePrefixMapper;

/**
 * {@link XmlNamespaceMapper} inspired by <a href=
 * "http://blog.bdoughan.com/2011/11/jaxb-and-namespace-prefixes.html">this
 * blog post</a>
 * 
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public class XmlNamespaceMapper extends NamespacePrefixMapper
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger(XmlNamespaceMapper.class);
	
	/** GbXML name space prefix */
	private static final String GBXML_PREFIX = ""; // DEFAULT NAMESPACE

	/** GbXML name space */
	private static final String GBXML_URI = "http://www.gbxml.org/schema";

	/** */
	private static final String XSD_PREFIX = "xsd";

	/** */
	private static final String XSD_URI = "http://www.w3.org/2001/XMLSchema";

	/** */
	private static final String XSI_PREFIX = "xsi";

	/** */
	private static final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";

	/** */
	private static final String CIM_PREFIX = "cim";

	/** */
	private static final String CIM_URI = "http://www.adapt4ee.eu/2012/schema/cim/";

	/** */
	@SuppressWarnings("unchecked")
	private final Map<String, String> nsPrefixMap = Collections.map(
			Collections.entry(XSD_URI, XSD_PREFIX),
			Collections.entry(XSI_URI, XSI_PREFIX),
			Collections.entry(GBXML_URI, GBXML_PREFIX),
			Collections.entry(CIM_URI, CIM_PREFIX));

	@Override
	public String getPreferredPrefix(final String namespaceUri,
			final String suggestion, final boolean requirePrefix)
	{
		final String prefix = this.nsPrefixMap.get(namespaceUri);
		if (prefix != null)
			return prefix;
		LOG.warn("Unexpected namespace: " + namespaceUri
				+ ", using suggested prefix: " + suggestion);
		return suggestion;
	}

	@Override
	public String[] getPreDeclaredNamespaceUris()
	{
		return new String[] { CIM_URI, XSD_URI, XSI_URI };
	}
}