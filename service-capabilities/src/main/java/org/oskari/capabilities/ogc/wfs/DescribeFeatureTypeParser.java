package org.oskari.capabilities.ogc.wfs;

import org.oskari.xml.XmlHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.stream.XMLStreamException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DescribeFeatureTypeParser {

    public static List<FeaturePropertyType> parseFeatureType(String xml, String featureType)
            throws IllegalArgumentException, XMLStreamException {
        Element doc = XmlHelper.parseXML(xml);
        if (doc == null) {
            throw new XMLStreamException("Failed to parse DescribeFeatureType XML");
        }
        return parseFeatureType(doc, featureType);
    }

    public static List<FeaturePropertyType> parseFeatureType(Element doc, String featureType)
            throws IllegalArgumentException, XMLStreamException {
        if (featureType == null) {
            throw new IllegalArgumentException("FeatureType param missing");
        }
        String rootEl = XmlHelper.getLocalName(doc);
        if (!"schema".equals(rootEl)) {
            throw new IllegalArgumentException(XmlHelper.generateUnexpectedElementMessage(doc));
        }
        String simpleType = getSimpleType(featureType);
        Element element = XmlHelper.getChildElements(doc, "element")
                .filter(e -> simpleType.equals(getSimpleType(XmlHelper.getAttributeValue(e, "name"))))
                .findFirst().orElse(null);
        if (element == null) {
            throw new IllegalArgumentException("No 'element' tag");
        }
        String type = getSimpleType(XmlHelper.getAttributeValue(element, "type"));

        Element typeElement = XmlHelper.getChildElements(doc, "complexType")
                .filter(e -> type.equals(getSimpleType(XmlHelper.getAttributeValue(e, "name"))))
                .findFirst().orElse(null);

        if (typeElement == null) {
            throw new IllegalArgumentException("No 'complexType' element");
        }
        Element complexContent = XmlHelper.getFirstChild(typeElement, "complexContent");
        if (complexContent == null) {
            throw new IllegalArgumentException("No 'complexContent' tag");
        }
        Element extension = XmlHelper.getFirstChild(complexContent, "extension");
        if (extension == null) {
            throw new IllegalArgumentException("No 'extension' tag");
        }
        Element sequence = XmlHelper.getFirstChild(extension, "sequence");
        if (sequence == null) {
            throw new IllegalArgumentException("No 'sequence' tag");
        }

        return XmlHelper.getChildElements(sequence, "element")
                .map(e -> {
                    FeaturePropertyType prop = new FeaturePropertyType();
                    Map<String, String> properties = XmlHelper.getAttributesAsMap(e);
                    prop.name = properties.get("name");
                    String propType = properties.get("type");
                    // prop.isNillable = properties.get("nillable");
                    if (propType != null) {
                        prop.type = propType;
                    } else {
                        parseComplexType(e, prop);
                    }
                    prop.type = getSimpleType(prop.type);
                    return prop;
                }).collect(Collectors.toList());
    }

    /*

        <xsd:element minOccurs="0" maxOccurs="1" name="VUOSI" nillable="true">
            <xsd:simpleType>
                <xsd:restriction base="xsd:string">
                    <xsd:maxLength value="2"/>
                </xsd:restriction>
            </xsd:simpleType>
        </xsd:element>
     */
    private static void parseComplexType(Element el, FeaturePropertyType prop) {
        Element simpleType = XmlHelper.getFirstChild(el, "simpleType");
        if (simpleType == null) {
            return;
        }
        Element restriction = XmlHelper.getFirstChild(simpleType, "restriction");
        if (restriction == null) {
            return;
        }
        prop.type = getSimpleType(XmlHelper.getAttributeValue(restriction, "base"));
        // Try parsing restrictions for field
        NodeList list = restriction.getChildNodes();
        for (int i = 0 ; i < list.getLength(); i++) {
            Node item = list.item(i);
            if (item.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) item;
            String localName = XmlHelper.getLocalName(element);
            String value = getSimpleType(XmlHelper.getAttributeValue(element, "value"));
            if (value != null && !value.trim().isEmpty()) {
                prop.restrictions.put(localName, value);
            }
        }
    }

    private static String getSimpleType(String ns) {
        if (ns == null || ns.isEmpty()) {
            return "";
        }
        String[] parts = ns.split("\\:");
        if (parts.length < 2) {
            return ns;
        }
        return parts[1];
    }
}
