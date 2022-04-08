package org.oskari.capabilities.ogc.wfs;

import org.oskari.capabilities.ogc.BoundingBox;
import org.oskari.capabilities.ogc.LayerCapabilitiesWFS;
import org.oskari.xml.XmlHelper;
import org.w3c.dom.Element;

import javax.xml.stream.XMLStreamException;
import java.util.*;
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
        Element outputFormatsParam = XmlHelper.getChildElements(getFeature, "Parameter")
                .filter(e -> "outputFormat".equals(XmlHelper.getAttributeValue(e, "name")))
                .findFirst().orElse(null);
        Element allowedValues = XmlHelper.getFirstChild(outputFormatsParam, "AllowedValues");
        if (allowedValues == null) {
            // 2.0.0 has values wrapped inside AllowedValues
            // 1.1.0 has values directly inside outputFormatsParam
            allowedValues = outputFormatsParam;
        }
        Set<String> outputFormats = XmlHelper.getChildElements(allowedValues, "Value")
                .map(Element::getTextContent)
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        /*
            <ows:Parameter name="outputFormat">
                <ows:AllowedValues>
                    <ows:Value>application/gml+xml; version=3.2</ows:Value>
                    <ows:Value>GML2</ows:Value>
                    <ows:Value>KML</ows:Value>
                    <ows:Value>SHAPE-ZIP</ows:Value>
                    <ows:Value>application/json</ows:Value>
                    <ows:Value>application/vnd.google-earth.kml xml</ows:Value>
                    <ows:Value>application/vnd.google-earth.kml+xml</ows:Value>
                    <ows:Value>csv</ows:Value>
                    <ows:Value>excel</ows:Value>
                    <ows:Value>excel2007</ows:Value>
                    <ows:Value>gml3</ows:Value>
                    <ows:Value>gml32</ows:Value>
                    <ows:Value>json</ows:Value>
                    <ows:Value>text/xml; subtype=gml/2.1.2</ows:Value>
                    <ows:Value>text/xml; subtype=gml/3.1.1</ows:Value>
                    <ows:Value>text/xml; subtype=gml/3.2</ows:Value>
                </ows:AllowedValues>
            </ows:Parameter>
        */

        Element featureTypeList = XmlHelper.getFirstChild(doc, "FeatureTypeList");
        String version = XmlHelper.getAttributeValue(doc, "version");
        return parseLayers(featureTypeList, version, outputFormats);
    }


    private static List<LayerCapabilitiesWFS> parseLayers(Element parent, String version, Set<String> outputFormats) {
        return XmlHelper.getChildElements(parent, "FeatureType")
                .map(e -> parseLayer(e, version, outputFormats))
                .collect(Collectors.toList());
    }

    private static LayerCapabilitiesWFS parseLayer(Element layer, String version, Set<String> outputFormats) {
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
        // TODO: LayerJSONFormatterWFS.createCapabilitiesJSON()
        value.setFormats(outputFormats);
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
/*
        boolean coversWholeWorld = minX <= -180 && minY <= -90 && maxX >= 180 && maxY >= 90;
        if (coversWholeWorld) {
            // no need to attach coverage if it covers the whole world as it's not useful info
            // TODO: should this be somewhere else?
            //  We might want to see what the service declares even if we shouldn't use the info
            return null;
        }
 */
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
