package org.oskari.capabilities.ogc.wfs;

import org.oskari.capabilities.ogc.BoundingBox;
import org.oskari.capabilities.ogc.CapabilitiesConstants;
import org.oskari.capabilities.ogc.LayerCapabilitiesWFS;
import org.oskari.xml.XmlHelper;
import org.w3c.dom.Element;

import javax.xml.stream.XMLStreamException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WFSCapsParser {

    public static List<LayerCapabilitiesWFS> parseCapabilities(String xml)
            throws IllegalArgumentException, XMLStreamException {
        Element doc = XmlHelper.parseXML(xml);
        if (doc == null) {
            throw new XMLStreamException("Failed to parse XML");
        }
        return parseCapabilities(doc);
    }

    public static List<LayerCapabilitiesWFS> parseCapabilities(Element doc)
            throws IllegalArgumentException, XMLStreamException {
        String rootEl = XmlHelper.getLocalName(doc);
        if (!"WFS_Capabilities".equals(rootEl)) {
            throw new IllegalArgumentException(XmlHelper.generateUnexpectedElementMessage(doc));
        }
        Element opsMetadata = XmlHelper.getFirstChild(doc, "OperationsMetadata");
        if (opsMetadata == null) {
            throw new IllegalArgumentException("No OperationsMetadata element");
        }
        // Check that GetFeature is supported
        Element getFeature = XmlHelper.getChildElements(opsMetadata, "Operation")
                .filter(e -> "GetFeature".equals(XmlHelper.getAttributeValue(e, "name")))
                .findFirst().orElse(null);

        if (getFeature == null) {
            throw new IllegalArgumentException("No GetFeature operation support");
        }
        Element featureTypeList = XmlHelper.getFirstChild(doc, "FeatureTypeList");
        String version = XmlHelper.getAttributeValue(doc, "version");
        return parseLayers(featureTypeList, version);
    }


    private static List<LayerCapabilitiesWFS> parseLayers(Element parent, String version) {
        return XmlHelper.getChildElements(parent, "FeatureType")
                .map(e -> parseLayer(e, version))
                .collect(Collectors.toList());
    }

    private static LayerCapabilitiesWFS parseLayer(Element layer, String version) {
        String name = XmlHelper.getChildValue(layer, "Name");
        String title = XmlHelper.getChildValue(layer, "Title");
        LayerCapabilitiesWFS value = new LayerCapabilitiesWFS(name, title);
        value.setVersion(version);
        value.setDescription(XmlHelper.getChildValue(layer, "Abstract"));

        value.setKeywords(getKeywords(layer));
        value.setBbox(parseGeoGraphicBbox(XmlHelper.getFirstChild(layer, "WGS84BoundingBox")));
        //value.setMaxFeatures( from service constraints);
        Set<String> srs = new HashSet<>(1);
        if ("2.0.0".equals(version)) {
            srs.add(XmlHelper.getChildValue(layer, "DefaultCRS"));
        } else {
            srs.add(XmlHelper.getChildValue(layer, "DefaultSRS"));
        }

        value.setSrs(srs);

        value.setMetadataUrl(getMetadataUrl(layer));
        return value;
    }

    protected static Set<String> getKeywords(Element featureType) {
        if (featureType == null) {
            return Collections.emptySet();
        }
        Element list = XmlHelper.getFirstChild(featureType, "Keywords");
        if (list == null) {
            return Collections.emptySet();
        }

        return XmlHelper.getChildElements(list, "Keyword")
                .map(Element::getTextContent)
                .collect(Collectors.toSet());
    }

    protected static BoundingBox parseGeoGraphicBbox(Element boundingBox) {
        if (boundingBox == null) {
            return null;
        }
        String low = XmlHelper.getChildValue(boundingBox, "LowerCorner");
        String up = XmlHelper.getChildValue(boundingBox, "UpperCorner");
        if (low == null || up == null) {
            return null;
        }
        String[] lowerCorner = low.split(" ");
        String[] upperCorner = up.split(" ");
        if (lowerCorner.length != 2 || upperCorner.length != 2) {
            return null;
        }
        double minX = Double.parseDouble(lowerCorner[0]);
        double minY = Double.parseDouble(lowerCorner[1]);
        double maxX = Double.parseDouble(upperCorner[0]);
        double maxY = Double.parseDouble(upperCorner[1]);
        return new BoundingBox(minX, maxX, minY, maxY, "EPSG:4326");
    }

    private static String getMetadataUrl(Element layer) {
        Element metadataEl = XmlHelper.getFirstChild(layer, "MetadataURL");
        if (metadataEl == null) {
            return null;
        }
        String href = XmlHelper.getAttributeValue(metadataEl, "href");
        if (href != null) {
            // wfs 2.0.0
            return href;
        }
        // 1.1.0
        return metadataEl.getTextContent().trim();
    }
}
