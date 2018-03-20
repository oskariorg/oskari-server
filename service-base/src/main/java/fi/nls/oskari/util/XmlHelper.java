package fi.nls.oskari.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.util.StAXParserConfiguration;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.NamespaceContext;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

public class XmlHelper {

    private static final Logger log = LogFactory.getLogger(XmlHelper.class);

/*
    <wfs:Transaction xmlns:wfs="http://www.opengis.net/wfs" service="WFS" version="1.0.0" xsi:schemaLocation="http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.0.0/WFS-transaction.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <wfs:Insert>
    <feature:my_places xmlns:feature="http://www.oskari.org">
    <feature:geometry>
    <gml:MultiPoint xmlns:gml="http://www.opengis.net/gml" srsName="EPSG:3067">
    <gml:pointMember>
    <gml:Point>
    <gml:coordinates decimal="." cs="," ts=" ">395540,6706198.1514938</gml:coordinates>
    </gml:Point>
    </gml:pointMember>
    </gml:MultiPoint>
    </feature:geometry>
    <feature:name>poster test</feature:name>
    <feature:place_desc></feature:place_desc>
    <feature:attention_text></feature:attention_text>
    <feature:link></feature:link>
    <feature:image_url></feature:image_url>
    <feature:category_id>11588</feature:category_id>
    <feature:uuid>aaaaa-bbbbb-cccc-dddd-eeee</feature:uuid>
    </feature:my_places>
    </wfs:Insert>
    </wfs:Transaction>
    */

    public static OMElement parseXML(final String xml) {
        try {
            // Any external entity can be considered a serious security risk for at least
            // wmts-capabilities and myplaces payload where this is used
            // https://ws.apache.org/axiom/apidocs/org/apache/axiom/om/util/StAXParserConfiguration.html#STANDALONE
            return OMXMLBuilderFactory.createOMBuilder(OMAbstractFactory.getOMFactory(), StAXParserConfiguration.STANDALONE, new StringReader(xml)).getDocumentElement();
        } catch (Exception e) {
            log.error("Couldnt't parse XML", log.getCauseMessages(e), xml);
        }
        return null;
    }

    public static String toString(final OMElement xml) {
        try {
            StringWriter writer = new StringWriter();
            xml.serialize(writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Couldnt't serialize XML to String", log.getCauseMessages(e), xml);
        }
        return null;
    }

    /**
     * Checks OMElement direct children. Returns true if each child tag has given local name.
     *
     * @param root element to check
     * @param tag  localname to check against
     * @return false if params are null or there is a direct children with another name
     */
    public static boolean containsOnlyDirectChildrenOfName(final OMElement root, final String tag) {
        if (root == null || tag == null) {
            return false;
        }

        final Iterator<OMElement> it = root.getChildElements();
        while (it.hasNext()) {
            OMElement child = it.next();
            if (!tag.equals(child.getLocalName())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the text content of the first child element with the specified localName
     * @see OMElement#getText()

     * @param elem parent element
     * @param localName to search
     * @return the text content of the child element, null if not available
     * @throws XMLStreamException if there is more than one matching element
     */
    public static String getChildValue(final OMElement elem, final String localName)
            throws XMLStreamException {
        OMElement e = getChild(elem, localName);
        return e != null ? e.getText() : null;
    }

    /**
     * Returns the first child element with the specified localName
     * @param elem parent element
     * @param localName to search
     * @return the child element, null if not available
     * @throws XMLStreamException if there is more than one matching element
     */
    public static OMElement getChild(final OMElement elem, final String localName)
            throws XMLStreamException {
        if(elem == null || localName == null) {
           return null;
        }
        final Iterator<OMElement> it = elem.getChildrenWithLocalName(localName);
        if (!it.hasNext()) {
            return null;
        }
        final OMElement result = it.next();
        if(it.hasNext()) {
            throw new XMLStreamException("More than one element with localName " + localName);
       }
        return result;
    }


    public static Map<String, String> getAttributesAsMap(final OMElement elem) {
        final Map<String, String> attributes = new HashMap<String, String>();
        if (elem == null) {
            return attributes;
        }
        final Iterator<OMAttribute> attrs = elem.getAllAttributes();
        while (attrs.hasNext()) {
            final OMAttribute a = attrs.next();
            attributes.put(a.getLocalName(), a.getAttributeValue());
        }
        return attributes;
    }

    public static String getAttributeValue(final OMElement elem, final String attrLocalName) {
        return getAttributesAsMap(elem).get(attrLocalName);
    }

    public static AXIOMXPath buildXPath(final String str, final NamespaceContext ctx) {
        try {
            AXIOMXPath xpath = new AXIOMXPath(str);
            xpath.setNamespaceContext(ctx);
            return xpath;
        } catch (Exception ex) {
            log.error(ex, "Error creating xpath:", str);
        }
        return null;
    }

    /**
     * Strips away the XML prolog
     * @param xml containing the XML message
     * @return XML message without the prolog
     */
    public static String stripPrologFromXML(String xml) {
        if (xml.startsWith("<?xml")) {
            int i = xml.indexOf("?>", 5);
            if (i < 0) {
                throw new IllegalArgumentException("Invalid XML prolog!");
            }
            i = xml.indexOf('<', i + 2);
            if (i < 0) {
                throw new IllegalArgumentException("Can't find XML after prolog!");
            }
            return xml.substring(i);
        }
        return xml;
    }

    /**
     * Obtain a new instance of a DocumentBuilderFactory with security features enables.
     * This static method creates a new factory instance.
     *
     * @return New instance of a DocumentBuilderFactory
     * @throws FactoryConfigurationError - in case of service configuration error or if
     * the implementation is not available or cannot be instantiated.
     */
    public static DocumentBuilderFactory newDocumentBuilderFactory() throws FactoryConfigurationError {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            // Note that these settings are XML parser implementation specific
            // and may require adjustment in some environments. These are for Xerces 2
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (ParserConfigurationException ex) {
            log.warn("Unable to enable security features for DocumentBuilderFactory", ex.getMessage());
        }
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    /**
     * Obtain a new instance of a TransformerFactory with security features enabled.
     * This static method creates a new factory instance.
     *
     * @return new TransformerFactory instance, never null.
     * @throws TransformerFactoryConfigurationError - Thrown in case of service configuration error or if
     * the implementation is not available or cannot be instantiated.
     */
    public static TransformerFactory newTransformerFactory() throws TransformerFactoryConfigurationError {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (TransformerConfigurationException ex) {
            log.warn("Unable to enable feature for secure processing for TransformerFactory", ex.getMessage());
        }
        // Empty protocol String to disable access to external resources
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        // Disable resolving of any kind of URIs, not sure if this is actually necessary
        transformerFactory.setURIResolver((String href, String base) -> null);
        return transformerFactory;
    }
}
