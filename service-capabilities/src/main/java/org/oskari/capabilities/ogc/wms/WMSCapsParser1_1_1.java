package org.oskari.capabilities.ogc.wms;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import org.oskari.capabilities.ogc.BoundingBox;
import org.oskari.capabilities.ogc.LayerCapabilitiesWMS;
import org.oskari.xml.XmlHelper;
import org.w3c.dom.Element;

import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WMSCapsParser1_1_1 extends WMSCapsParser {

    public static final String ROOT_EL = "WMT_MS_Capabilities";
    private static final Logger LOG = LogFactory.getLogger(WMSCapsParser1_1_1.class);

    public static  List<LayerCapabilitiesWMS> parseCapabilities(Element doc)
            throws IllegalArgumentException, XMLStreamException {
        Element capability = XmlHelper.getFirstChild(doc, "Capability");
        if (capability == null) {
            throw new IllegalArgumentException("No Capability element");
        }
        Element request = XmlHelper.getFirstChild(capability, "Request");
        if (request == null) {
            throw new IllegalArgumentException("No Request element");
        }
        Element getMap = XmlHelper.getFirstChild(request, "GetMap");
        if (getMap == null) {
            throw new IllegalArgumentException("No GetMap element");
        }

        return parseLayers(capability, getInfoformats(request));
    }

    private static List<LayerCapabilitiesWMS> parseLayers(Element parent, Set<String> infoformats) {
        return XmlHelper.getChildElements(parent, "Layer")
                .map(e -> parseLayer(e, infoformats))
                .collect(Collectors.toList());
    }

    private static LayerCapabilitiesWMS parseLayer(Element layer, Set<String> infoformats) {
        String name = XmlHelper.getChildValue(layer, "Name");
        if (name == null) {
            // group layer
        }
        String title = XmlHelper.getChildValue(layer, "Title");
        LayerCapabilitiesWMS value = new LayerCapabilitiesWMS(name, title);
        value.setSrs(XmlHelper.getChildElements(layer, "SRS")
                .map(el -> el.getTextContent())
                .collect(Collectors.toSet()));
        boolean isQueryable = ConversionHelper.getBoolean(XmlHelper.getAttributeValue(layer, "queryable"), false);
        if (isQueryable) {
            value.setInfoFormats(infoformats);
        }
        value.setDescription(XmlHelper.getChildValue(layer, "Abstract"));
        value.setStyles(parseStyles(layer));
        value.setKeywords(getKeywords(layer));
        BoundingBox latlonBox = parseBbox(
                XmlHelper.getFirstChild(layer, "LatLonBoundingBox"), "EPSG:4326");

        if (latlonBox == null) {
            List<BoundingBox> boxes = XmlHelper.getChildElements(layer, "BoundingBox")
                    .map(el -> parseBbox(el, "EPSG:4326"))
                    .collect(Collectors.toList());
            latlonBox = getBestMatch(boxes);
        }
        value.setBbox(latlonBox);
        // recurse to child layers
        value.setLayers(parseLayers(layer, infoformats));
        // TODO: time dimension
        return value;
    }

}
