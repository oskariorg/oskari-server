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
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XmlHelper {

    private static final Logger LOGGER = LogFactory.getLogger(XmlHelper.class);

    public static Element parseXML(final String xml) {
        return parseXML(xml, false);
    }
    public static Element parseXML(final String xml, boolean nsAware) {
        if (xml == null) {
            return null;
        }
        // Note! Tries forced removal of doctypes because:
        // factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        byte[] bytes = removeDocType(xml.trim()).getBytes(StandardCharsets.UTF_8);
        try (InputStream s = new ByteArrayInputStream(bytes)) {
            return parseXML(s, nsAware);
        } catch (Exception e) {
            String logged = xml.length() > 30 ? xml.substring(27) + "..." : xml;
            LOGGER.error("Couldn't parse XML", LOGGER.getCauseMessages(e), logged);
            LOGGER.debug("Unparseable xml:", xml);
        }
        return null;
    }

    public static Element parseXML(final InputStream xml, boolean nsAware) throws Exception {
        if (xml == null) {
            return null;
        }
        return newDocumentBuilderFactory(nsAware).newDocumentBuilder().parse(xml).getDocumentElement();
    }

    public static Element parseXML(final InputStream xml) throws Exception {
        return parseXML(xml, false);
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

    // if namespace declarations are missing the local name isn't working and we need to split it manually
    public static String getLocalName(Node el) {
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
            attributes.put(getLocalName(node), node.getNodeValue());
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

    // Removes doctype as it's not needed and allows parser to process it succesfully:
    // <!DOCTYPE WMT_MS_Capabilities SYSTEM "https://fake.address/inspire-wms/schemas/wms/1.1.1/WMS_MS_Capabilities.dtd">
    // <!DOCTYPE WMT_MS_Capabilities SYSTEM
    //    "http://schemas.opengis.net/wms/1.1.0/capabilities_1_1_0.dtd"[ <!ELEMENT VendorSpecificCapabilities EMPTY>]>
    public static String removeDocType(String input) {
        if (input == null) {
            return null;
        }
        String upper = input.toUpperCase();
        int index = upper.indexOf("<!DOCTYPE");
        if (index == -1) {
            return input;
        }
        int endIndex = upper.indexOf(">", index) + 1;
        int newStartIndex = upper.indexOf("<", index + 1);
        while (newStartIndex != -1 && newStartIndex < endIndex) {
            endIndex = upper.indexOf(">", endIndex) + 1;
            newStartIndex = upper.indexOf("<", newStartIndex + 1);
        }
        String start = input.substring(0, index);
        return start + input.substring(endIndex);
    }
    /**
     * Obtain a new instance of a DocumentBuilderFactory with security features enables.
     * This static method creates a new factory instance.
     *
     * @param nsAware enable namespace aware parsing
     * @return New instance of a DocumentBuilderFactory
     * @throws FactoryConfigurationError - in case of service configuration error or if
     * the implementation is not available or cannot be instantiated.
     */
    public static DocumentBuilderFactory newDocumentBuilderFactory(boolean nsAware) throws FactoryConfigurationError {
        DocumentBuilderFactory f = newDocumentBuilderFactory();
        f.setNamespaceAware(nsAware);
        return f;
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

        // Note that these settings are XML parser implementation specific
        // and may require adjustment in some environments. These are for Xerces 2
        toggleFeature(factory,XMLConstants.FEATURE_SECURE_PROCESSING, true);
        toggleFeature(factory,"http://apache.org/xml/features/disallow-doctype-decl", true);
        toggleFeature(factory,"http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        toggleFeature(factory,"http://xml.org/sax/features/external-general-entities", false);
        toggleFeature(factory,"http://xml.org/sax/features/external-parameter-entities", false);

        toggleFeature(factory,"http://xml.org/sax/features/namespaces", false);
        toggleFeature(factory,"http://xml.org/sax/features/validation", false);
        toggleFeature(factory,"http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        toggleFeature(factory,"http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        factory.setValidating(false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    private static void toggleFeature(DocumentBuilderFactory factory, String name, boolean value) {
        try {
            // Note that these settings are XML parser implementation specific
            // and may require adjustment in some environments. These are for Xerces 2
            factory.setFeature(name, value);
        } catch (ParserConfigurationException ex) {
            LOGGER.warn("Unable to set security features for XML-parsing:", name, ":", value, ex.getMessage());
        }
    }

    public static String generateUnexpectedElementMessage(Element doc) {
        String elName = XmlHelper.getLocalName(doc);
        String children = XmlHelper.getChildElements(doc, null)
                .map(n ->XmlHelper.getLocalName(n))
                .collect(Collectors.joining());
        return "Unexpected XML element: '" + elName + "' with children: " + children;
    }

    /**
     * Helper function mostly for debugging to serialize the element back to string
     * @param xml Element to serialize as string
     * @return serialized xml
     */
    public static String toString(final Element xml) {
        try {
            DOMImplementationLS lsImpl = (DOMImplementationLS) xml.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
            LSSerializer serializer = lsImpl.createLSSerializer();
            serializer.getDomConfig().setParameter("xml-declaration", false);
            return serializer.writeToString(xml);
        } catch (Exception e) {
            LOGGER.error("Couldn't serialize XML to String", LOGGER.getCauseMessages(e), xml);
        }
        return null;
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
            LOGGER.debug("Unable to enable feature for secure processing for TransformerFactory", ex.getMessage());
        }
        // Empty protocol String to disable access to external resources
        try {
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        } catch (Exception e) {
            // https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#JAXP_DocumentBuilderFactory.2C_SAXParserFactory_and_DOM4J
            // https://stackoverflow.com/questions/27128578/set-feature-accessexternaldtd-in-transformerfactory#29021326

            // Fox example Xalan is providing a custom TransformerFactory which doesn't support this so having it in the
            // classpath will give you this error and getting the actual impl class name is a huge win for debugging the reason.
            // You can check which dependency brings for example Xalan to classpath by running "mvn dependency:tree"
            LOGGER.debug("Unable to disable external DTD and stylesheets for XML parsing. Transformer class impl is",
                    transformerFactory.getClass().getCanonicalName(), ". Error was:", e.getMessage());
        }
        // Disable resolving of any kind of URIs, not sure if this is actually necessary
        transformerFactory.setURIResolver((String href, String base) -> null);
        return transformerFactory;
    }
}
