package org.oskari.capabilities.ogc.wms;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.oskari.capabilities.ogc.BoundingBox;
import org.oskari.capabilities.ogc.LayerCapabilitiesWMS;
import org.oskari.capabilities.ogc.LayerStyle;
import org.oskari.xml.XmlHelper;
import org.w3c.dom.Element;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class WMSCapsParser {

    private static final Logger LOG = LogFactory.getLogger(WMSCapsParser.class);

    public static List<LayerCapabilitiesWMS> parseCapabilities(String xml)
            throws IllegalArgumentException, XMLStreamException {
        Element doc = XmlHelper.parseXML(removeDocType(xml));
        if (doc == null) {
            throw new XMLStreamException("Failed to parse XML");
        }
        return parseCapabilities(doc);
    }
    // Removes doctype so parser processes it succesfully:
    // <!DOCTYPE WMT_MS_Capabilities SYSTEM "https://fake.address/inspire-wms/schemas/wms/1.1.1/WMS_MS_Capabilities.dtd">
    private static String removeDocType(String input) {
        if (input == null) {
            return input;
        }
        String upper = input.toUpperCase();
        int index = upper.indexOf("<!DOCTYPE");
        if (index == -1) {
            return input;
        }
        int endIndex = upper.indexOf(">", index) + 1;
        String start = input.substring(0, index);
        return start + input.substring(endIndex);
    }

    public static List<LayerCapabilitiesWMS> parseCapabilities(InputStream in)
            throws IllegalArgumentException, XMLStreamException {
        Element doc = XmlHelper.parseXML(in);
        return parseCapabilities(doc);
    }

    public static List<LayerCapabilitiesWMS> parseCapabilities(Element doc)
            throws IllegalArgumentException, XMLStreamException {
        String rootEl = XmlHelper.getLocalName(doc);
        if (WMSCapsParser1_1_1.ROOT_EL.equals(rootEl)) {
            return WMSCapsParser1_1_1.parseCapabilities(doc);
        } else if (WMSCapsParser1_3_0.ROOT_EL.equals(rootEl)) {
            return WMSCapsParser1_3_0.parseCapabilities(doc);
        }
        throw new IllegalArgumentException("Unrecognized root element:" + rootEl);
    }

    protected static Set<String> getTexts(Element parentEl, String childLocalName) {
        Set<String> texts =  XmlHelper.getChildElements(parentEl, childLocalName)
                .map(Element::getTextContent)
                .collect(Collectors.toSet());
        return texts;
    }
    protected static Set<String> getKeywords(Element layer) {
        if (layer == null) {
            return Collections.emptySet();
        }
        Element list = XmlHelper.getFirstChild(layer, "KeywordList");
        if (list == null) {
            return Collections.emptySet();
        }
        return getTexts(list, "Keyword");
    }

    protected static Set<String> getInfoformats(Element request) {
        if (request == null) {
            return Collections.emptySet();
        }
        Element getFeatureInfo = XmlHelper.getFirstChild(request, "GetFeatureInfo");
        if (getFeatureInfo == null) {
            return Collections.emptySet();
        }
        return getTexts(getFeatureInfo, "Format");
    }

    protected static BoundingBox getBestMatch(List<BoundingBox> boxes) {
        BoundingBox box = boxes.stream().filter(bbox -> bbox.isSrs("EPSG:4326")).findFirst().orElse(null);
        if (box != null) {
            return box;
        }
        return boxes.stream().filter(bbox -> bbox.isSrs("CRS:84")).findFirst().orElse(null);
    }

    protected static BoundingBox parseBbox(Element boundingBox, String defaultSrs) {
        if (boundingBox == null) {
            return null;
        }
        Map<String, String> attrs = XmlHelper.getAttributesAsMap(boundingBox);
        double minX = Double.parseDouble(attrs.get("minx"));
        double minY = Double.parseDouble(attrs.get("miny"));
        double maxX = Double.parseDouble(attrs.get("maxx"));
        double maxY = Double.parseDouble(attrs.get("maxy"));
        return new BoundingBox(minX, maxX, minY, maxY, attrs.getOrDefault("CRS", defaultSrs));
    }

    protected static List<LayerStyle> parseStyles(Element layer) {
        List<LayerStyle> styles =  XmlHelper.getChildElements(layer, "Style")
                .map(styleEl -> {
                    LayerStyle style = new LayerStyle();
                    style.setName(XmlHelper.getChildValue(styleEl, "Name"));
                    style.setTitle(XmlHelper.getChildValue(styleEl, "Title"));
                    Element legendEl = XmlHelper.getFirstChild(styleEl, "LegendURL");
                    if (legendEl != null) {
                        Element resource = XmlHelper.getFirstChild(legendEl, "OnlineResource");
                        style.setLegend(XmlHelper.getAttributeValue(resource, "href"));
                    }
                    return style;
                })
                .collect(Collectors.toList());
        return styles;
    }

}
