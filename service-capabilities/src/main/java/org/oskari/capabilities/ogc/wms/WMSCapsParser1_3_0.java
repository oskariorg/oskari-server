package org.oskari.capabilities.ogc.wms;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.oskari.capabilities.TimeDimensionParser;
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
    public static final String VERSION = "1.3.0";
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
        value.setVersion(VERSION);
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
        value.setTimes(parseTimes(XmlHelper.getFirstChild(layer, "Dimension")));

        // recurse to child layers
        value.setLayers(parseLayers(layer, infoformats));
        return value;
    }

    // <Dimension name="time" default="current" units="ISO8601">2002-07-01T00:00:00.000Z,2002-08-01T00:00:00.000Z,2002-09-01T00:00:00.000Z,2002-10-01T00:00:00.000Z,2002-11-01T00:00:00.000Z,2002-12-01T00:00:00.000Z,2003-01-01T00:00:00.000Z,2003-02-01T00:00:00.000Z,2003-03-01T00:00:00.000Z,2003-04-01T00:00:00.000Z,2003-05-01T00:00:00.000Z,2003-06-01T00:00:00.000Z,2003-07-01T00:00:00.000Z,2003-08-01T00:00:00.000Z,2003-09-01T00:00:00.000Z,2003-10-01T00:00:00.000Z,2003-11-01T00:00:00.000Z,2003-12-01T00:00:00.000Z,2004-01-01T00:00:00.000Z,2004-02-01T00:00:00.000Z,2004-03-01T00:00:00.000Z,2004-04-01T00:00:00.000Z,2004-05-01T00:00:00.000Z,2004-06-01T00:00:00.000Z,2004-07-01T00:00:00.000Z,2004-08-01T00:00:00.000Z,2004-09-01T00:00:00.000Z,2004-10-01T00:00:00.000Z,2004-11-01T00:00:00.000Z,2004-12-01T00:00:00.000Z,2005-01-01T00:00:00.000Z,2005-02-01T00:00:00.000Z,2005-03-01T00:00:00.000Z,2005-04-01T00:00:00.000Z,2005-05-01T00:00:00.000Z,2005-06-01T00:00:00.000Z,2005-07-01T00:00:00.000Z,2005-08-01T00:00:00.000Z,2005-09-01T00:00:00.000Z,2005-10-01T00:00:00.000Z,2005-11-01T00:00:00.000Z,2005-12-01T00:00:00.000Z,2006-01-01T00:00:00.000Z,2006-02-01T00:00:00.000Z,2006-03-01T00:00:00.000Z,2006-04-01T00:00:00.000Z,2006-05-01T00:00:00.000Z,2006-06-01T00:00:00.000Z,2006-07-01T00:00:00.000Z,2006-08-01T00:00:00.000Z,2006-09-01T00:00:00.000Z,2006-10-01T00:00:00.000Z,2006-11-01T00:00:00.000Z,2006-12-01T00:00:00.000Z,2007-01-01T00:00:00.000Z,2007-02-01T00:00:00.000Z,2007-03-01T00:00:00.000Z,2007-04-01T00:00:00.000Z,2007-05-01T00:00:00.000Z,2007-06-01T00:00:00.000Z,2007-07-01T00:00:00.000Z,2007-08-01T00:00:00.000Z,2007-09-01T00:00:00.000Z,2007-10-01T00:00:00.000Z,2007-11-01T00:00:00.000Z,2007-12-01T00:00:00.000Z,2008-01-01T00:00:00.000Z,2008-02-01T00:00:00.000Z,2008-03-01T00:00:00.000Z,2008-04-01T00:00:00.000Z,2008-05-01T00:00:00.000Z,2008-06-01T00:00:00.000Z,2008-07-01T00:00:00.000Z,2008-08-01T00:00:00.000Z,2008-09-01T00:00:00.000Z,2008-10-01T00:00:00.000Z,2008-11-01T00:00:00.000Z,2008-12-01T00:00:00.000Z,2009-01-01T00:00:00.000Z,2009-02-01T00:00:00.000Z,2009-03-01T00:00:00.000Z,2009-04-01T00:00:00.000Z,2009-05-01T00:00:00.000Z,2009-06-01T00:00:00.000Z,2009-07-01T00:00:00.000Z,2009-08-01T00:00:00.000Z,2009-09-01T00:00:00.000Z,2009-10-01T00:00:00.000Z,2009-11-01T00:00:00.000Z,2009-12-01T00:00:00.000Z,2010-01-01T00:00:00.000Z,2010-02-01T00:00:00.000Z,2010-03-01T00:00:00.000Z,2010-04-01T00:00:00.000Z,2010-05-01T00:00:00.000Z,2010-06-01T00:00:00.000Z,2010-07-01T00:00:00.000Z,2010-08-01T00:00:00.000Z,2010-09-01T00:00:00.000Z,2010-10-01T00:00:00.000Z,2010-11-01T00:00:00.000Z,2010-12-01T00:00:00.000Z,2011-01-01T00:00:00.000Z,2011-02-01T00:00:00.000Z,2011-03-01T00:00:00.000Z,2011-04-01T00:00:00.000Z,2011-05-01T00:00:00.000Z,2011-06-01T00:00:00.000Z,2011-07-01T00:00:00.000Z,2011-08-01T00:00:00.000Z,2011-09-01T00:00:00.000Z,2011-10-01T00:00:00.000Z,2011-11-01T00:00:00.000Z,2011-12-01T00:00:00.000Z,2012-01-01T00:00:00.000Z,2012-02-01T00:00:00.000Z,2012-03-01T00:00:00.000Z,2012-04-01T00:00:00.000Z,2012-05-01T00:00:00.000Z,2012-06-01T00:00:00.000Z,2012-07-01T00:00:00.000Z,2012-08-01T00:00:00.000Z,2012-09-01T00:00:00.000Z,2012-10-01T00:00:00.000Z,2012-11-01T00:00:00.000Z,2012-12-01T00:00:00.000Z,2013-01-01T00:00:00.000Z,2013-02-01T00:00:00.000Z,2013-03-01T00:00:00.000Z,2013-04-01T00:00:00.000Z,2013-05-01T00:00:00.000Z,2013-06-01T00:00:00.000Z,2013-07-01T00:00:00.000Z</Dimension>
    private static String[] parseTimes(Element dimension) {
        // TODO: parse start, end, step
        if (dimension == null) {
            return null;
        }
        // All parameter names are case-insensitive
        if (!"time".equalsIgnoreCase(XmlHelper.getAttributeValue(dimension, "name"))) {
            return null;
        }
        if (!"ISO8601".equals(XmlHelper.getAttributeValue(dimension, "units"))) {
            return null;
        }
        String content = dimension.getTextContent();
        if (content.trim().isEmpty()) {
            return null;
        }
        String[] times = content.split("\\s*,\\s*");
        if (times.length == 1 && content.contains("/")) {
            // We get a single time that has the / separator? Might be an interval format
            // <Dimension name="time" units="ISO8601" default="2024-11-13T09:00:00Z" nearestValue="0">2024-11-13T00:00:00Z/2024-11-23T00:00:00Z/PT3H</Dimension>
            try {
                List<String> timeList = TimeDimensionParser.parseTimeDimensionAsStrings(content);
                return timeList.toArray(new String[0]);
            } catch (IllegalArgumentException e) {
                // Note, if we throw the exception, none of the layers will get parsed which is bad since failing just the time dimension is not critical
                // however in here we don't know what layer we are processing so it's not easy to track what the problematic layer was on logs
                LOG.warn("Error parsing time dimension as interval: ", e.getMessage());
            }
        }
        return times;
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
