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

public class WMSCapsParser1_3_0 extends WMSCapsParser {

    public static final String ROOT_EL = "WMS_Capabilities";
    private static final Logger LOG = LogFactory.getLogger(WMSCapsParser1_3_0.class);

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
        value.setSrs(XmlHelper.getChildElements(layer, "CRS")
                .map(el -> el.getTextContent())
                .collect(Collectors.toSet()));
        boolean isQueryable = "1".equals(XmlHelper.getAttributeValue(layer, "queryable"));
        if (isQueryable) {
            value.setInfoFormats(infoformats);
        }
        value.setDescription(XmlHelper.getChildValue(layer, "Abstract"));
        value.setStyles(parseStyles(layer));
        value.setKeywords(getKeywords(layer));

        Element geoboxEl = XmlHelper.getFirstChild(layer, "EX_GeographicBoundingBox");
        BoundingBox latlonBox = parseGeoGraphicBbox(geoboxEl, "EPSG:4326");

        if (latlonBox == null) {
            List<BoundingBox> boxes = XmlHelper.getChildElements(layer, "BoundingBox")
                    .map(el -> parseBbox(el, "EPSG:4326"))
                    .collect(Collectors.toList());
            latlonBox = getBestMatch(boxes);
        }
        value.setBbox(latlonBox);

        value.setMetadataUrl(getMetadataUrl(layer));
        value.setMinScale(XmlHelper.getChildValue(layer, "MinScaleDenominator"));
        value.setMaxScale(XmlHelper.getChildValue(layer, "MaxScaleDenominator"));

        // recurse to child layers
        value.setLayers(parseLayers(layer, infoformats));
        // TODO: time dimension
        return value;
    }

    /*
  <EX_GeographicBoundingBox>
    <westBoundLongitude>15.608220469655935</westBoundLongitude>
    <eastBoundLongitude>33.107629330539034</eastBoundLongitude>
    <southBoundLatitude>59.36205414098515</southBoundLatitude>
    <northBoundLatitude>70.09468368748001</northBoundLatitude>
  </EX_GeographicBoundingBox>
            */
    protected static BoundingBox parseGeoGraphicBbox(Element boundingBox, String defaultSrs) {
        if (boundingBox == null) {
            return null;
        }
        double minX = Double.parseDouble(XmlHelper.getChildValue(boundingBox, "westBoundLongitude"));
        double minY = Double.parseDouble(XmlHelper.getChildValue(boundingBox, "southBoundLatitude"));
        double maxX = Double.parseDouble(XmlHelper.getChildValue(boundingBox, "eastBoundLongitude"));
        double maxY = Double.parseDouble(XmlHelper.getChildValue(boundingBox, "northBoundLatitude"));
        return new BoundingBox(minX, maxX, minY, maxY, defaultSrs);
    }

    private static String getMetadataUrl(Element layer) {
        Element metadataEl = XmlHelper.getFirstChild(layer, "MetadataURL");
        if (metadataEl == null) {
            return null;
        }
        Element onlineResource = XmlHelper.getFirstChild(metadataEl, "OnlineResource");
        if (onlineResource == null) {
            return null;
        }
        return XmlHelper.getAttributeValue(onlineResource, "href");
    }
}
