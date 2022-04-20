package org.oskari.capabilities.ogc.wfs;

import org.oskari.xml.XmlHelper;
import org.w3c.dom.Element;

import javax.xml.stream.XMLStreamException;
import java.util.*;
import java.util.stream.Collectors;

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
                    prop.type = getSimpleType(properties.get("type"));
                    return prop;
                }).collect(Collectors.toList());
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
