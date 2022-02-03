package org.oskari.xml;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.w3c.dom.*;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class XmlHelper {

    private static final Logger LOGGER = LogFactory.getLogger(XmlHelper.class);

    public static Element parseXML(final String xml) {
        if (xml == null) {
            return null;
        }
        try {
            return parseXML(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            String logged = xml.length() > 30 ? xml.substring(27) + "..." : xml;
            LOGGER.error("Couldnt't parse XML", LOGGER.getCauseMessages(e), logged);
            LOGGER.debug("Unparseable xml:", xml);
        }
        return null;
    }
    public static Element parseXML(final InputStream xml) {
        if (xml == null) {
            return null;
        }
        try {
            return newDocumentBuilderFactory().newDocumentBuilder().parse(xml).getDocumentElement();
        } catch (Exception e) {
            LOGGER.error("Couldnt't parse XML from inputstream", LOGGER.getCauseMessages(e));
        }
        return null;
    }

    public static Stream<Element> getChildElements(final Element elem, final String localName) {
        if (elem == null) {
            return Stream.empty();
        }
        Stream.Builder<Element> builder = Stream.builder();

        NodeList list = elem.getChildNodes(); //elem.getElementsByTagName(localName);
        for (int i = 0 ; i < list.getLength(); i++) {
            Node item = list.item(i);
            if (item.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) item;
            if (localName == null || localName.equals(getLocalName(element))) {
                builder.add(element);
            }
        }
        return builder.build();
    }

    // if namespace declarations are missing the localname isn't working and we need to split it manually
    private static String getLocalName(Element el) {
        String loc = el.getLocalName();
        if (loc != null) {
            return loc;
        }
        String fullName = el.getNodeName();
        String[] parts = fullName.split(":");
        if (parts.length < 2) {
            return fullName;
        }
        return parts[1];
    }

    /**
     * Checks Element direct children. Returns true if each child tag has given local name.
     *
     * @param root element to check
     * @param tag  localname to check against
     * @return false if params are null or there is a direct children with another name
     */
    public static boolean containsOnlyDirectChildrenOfName(final Element root, final String tag) {
        if (root == null || tag == null) {
            return false;
        }

        final NodeList nodes = root.getChildNodes();
        for (int i = 0 ; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (!tag.equals(node.getLocalName())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the text content of the first child element with the specified localName
     * @see Element#getNodeValue()

     * @param elem parent element
     * @param localName to search
     * @return the text content of the child element, null if not available
     * @throws XMLStreamException if there is more than one matching element
     */
    public static String getChildValue(final Element elem, final String localName) {
        Element e = getFirstChild(elem, localName);
        if (e == null) {
            return null;
        }
        return e.getTextContent();
    }

    /**
     * Returns the first child element with the specified localName
     * @param elem parent element
     * @param localName to search
     * @return the child element, null if not available
     * @throws XMLStreamException if there is more than one matching element
     */
    public static Element getFirstChild(final Element elem, final String localName) {
        if (elem == null || localName == null) {
           return null;
        }
        return getChildElements(elem, localName).findFirst().orElse(null);
    }


    public static Map<String, String> getAttributesAsMap(final Element elem) {
        final Map<String, String> attributes = new HashMap<>();
        if (elem == null) {
            return attributes;
        }
        NamedNodeMap attrs = elem.getAttributes();
        for (int i = 0 ; i < attrs.getLength(); i++) {
            Node node = attrs.item(i);
            attributes.put(node.getNodeName(), node.getNodeValue());
        }
        return attributes;
    }

    public static String getAttributeValue(final Element elem, final String attrLocalName) {
        return getAttributesAsMap(elem).get(attrLocalName);
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
            LOGGER.warn("Unable to enable security features for DocumentBuilderFactory", ex.getMessage());
        }
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    /**
     * Helper function mostly for debugging to serialize the element back to string
     * @param xml
     * @return
     */
    public static String toString(final Element xml) {
        try {
            DOMImplementationLS lsImpl = (DOMImplementationLS) xml.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
            LSSerializer serializer = lsImpl.createLSSerializer();
            serializer.getDomConfig().setParameter("xml-declaration", false);
            return serializer.writeToString(xml);
        } catch (Exception e) {
            LOGGER.error("Couldnt't serialize XML to String", LOGGER.getCauseMessages(e), xml);
        }
        return null;
    }
}
