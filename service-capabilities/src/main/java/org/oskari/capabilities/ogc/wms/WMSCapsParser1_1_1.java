package org.oskari.capabilities.ogc.wms;

import org.oskari.capabilities.ogc.BoundingBox;
import org.oskari.capabilities.ogc.LayerCapabilitiesWMS;
import org.oskari.xml.XmlHelper;
import org.w3c.dom.Element;

import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WMSCapsParser1_1_1 extends WMSCapsParser {

    public static final String ROOT_EL = "WMT_MS_Capabilities";
    public static final String VERSION = "1.1.1";

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
        value.setSrs(XmlHelper.getChildElements(layer, "SRS")
                .map(el -> el.getTextContent())
                .collect(Collectors.toSet()));
        boolean isQueryable = "1".equals(XmlHelper.getAttributeValue(layer, "queryable"));
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

        // <ScaleHint min="1.0" max="60000.0"/>
        Map<String, String> scaleHints = XmlHelper.getAttributesAsMap(XmlHelper.getFirstChild(layer, "ScaleHint"));
        value.setMinScale(scaleHints.get("min"));
        value.setMaxScale(scaleHints.get("max"));
        // if there are other dimensions/extents than time this breaks...
        value.setTimes(parseTimes(XmlHelper.getFirstChild(layer, "Dimension"), XmlHelper.getFirstChild(layer, "Extent")));

        // recurse to child layers
        value.setLayers(parseLayers(layer, infoformats));
        return value;
    }

    /*
    <Dimension name="time" units="ISO8601"/>
    <Extent name="time" default="current">2002-07-01T00:00:00.000Z,2002-08-01T00:00:00.000Z,2002-09-01T00:00:00.000Z,2002-10-01T00:00:00.000Z,2002-11-01T00:00:00.000Z,2002-12-01T00:00:00.000Z,2003-01-01T00:00:00.000Z,2003-02-01T00:00:00.000Z,2003-03-01T00:00:00.000Z,2003-04-01T00:00:00.000Z,2003-05-01T00:00:00.000Z,2003-06-01T00:00:00.000Z,2003-07-01T00:00:00.000Z,2003-08-01T00:00:00.000Z,2003-09-01T00:00:00.000Z,2003-10-01T00:00:00.000Z,2003-11-01T00:00:00.000Z,2003-12-01T00:00:00.000Z,2004-01-01T00:00:00.000Z,2004-02-01T00:00:00.000Z,2004-03-01T00:00:00.000Z,2004-04-01T00:00:00.000Z,2004-05-01T00:00:00.000Z,2004-06-01T00:00:00.000Z,2004-07-01T00:00:00.000Z,2004-08-01T00:00:00.000Z,2004-09-01T00:00:00.000Z,2004-10-01T00:00:00.000Z,2004-11-01T00:00:00.000Z,2004-12-01T00:00:00.000Z,2005-01-01T00:00:00.000Z,2005-02-01T00:00:00.000Z,2005-03-01T00:00:00.000Z,2005-04-01T00:00:00.000Z,2005-05-01T00:00:00.000Z,2005-06-01T00:00:00.000Z,2005-07-01T00:00:00.000Z,2005-08-01T00:00:00.000Z,2005-09-01T00:00:00.000Z,2005-10-01T00:00:00.000Z,2005-11-01T00:00:00.000Z,2005-12-01T00:00:00.000Z,2006-01-01T00:00:00.000Z,2006-02-01T00:00:00.000Z,2006-03-01T00:00:00.000Z,2006-04-01T00:00:00.000Z,2006-05-01T00:00:00.000Z,2006-06-01T00:00:00.000Z,2006-07-01T00:00:00.000Z,2006-08-01T00:00:00.000Z,2006-09-01T00:00:00.000Z,2006-10-01T00:00:00.000Z,2006-11-01T00:00:00.000Z,2006-12-01T00:00:00.000Z,2007-01-01T00:00:00.000Z,2007-02-01T00:00:00.000Z,2007-03-01T00:00:00.000Z,2007-04-01T00:00:00.000Z,2007-05-01T00:00:00.000Z,2007-06-01T00:00:00.000Z,2007-07-01T00:00:00.000Z,2007-08-01T00:00:00.000Z,2007-09-01T00:00:00.000Z,2007-10-01T00:00:00.000Z,2007-11-01T00:00:00.000Z,2007-12-01T00:00:00.000Z,2008-01-01T00:00:00.000Z,2008-02-01T00:00:00.000Z,2008-03-01T00:00:00.000Z,2008-04-01T00:00:00.000Z,2008-05-01T00:00:00.000Z,2008-06-01T00:00:00.000Z,2008-07-01T00:00:00.000Z,2008-08-01T00:00:00.000Z,2008-09-01T00:00:00.000Z,2008-10-01T00:00:00.000Z,2008-11-01T00:00:00.000Z,2008-12-01T00:00:00.000Z,2009-01-01T00:00:00.000Z,2009-02-01T00:00:00.000Z,2009-03-01T00:00:00.000Z,2009-04-01T00:00:00.000Z,2009-05-01T00:00:00.000Z,2009-06-01T00:00:00.000Z,2009-07-01T00:00:00.000Z,2009-08-01T00:00:00.000Z,2009-09-01T00:00:00.000Z,2009-10-01T00:00:00.000Z,2009-11-01T00:00:00.000Z,2009-12-01T00:00:00.000Z,2010-01-01T00:00:00.000Z,2010-02-01T00:00:00.000Z,2010-03-01T00:00:00.000Z,2010-04-01T00:00:00.000Z,2010-05-01T00:00:00.000Z,2010-06-01T00:00:00.000Z,2010-07-01T00:00:00.000Z,2010-08-01T00:00:00.000Z,2010-09-01T00:00:00.000Z,2010-10-01T00:00:00.000Z,2010-11-01T00:00:00.000Z,2010-12-01T00:00:00.000Z,2011-01-01T00:00:00.000Z,2011-02-01T00:00:00.000Z,2011-03-01T00:00:00.000Z,2011-04-01T00:00:00.000Z,2011-05-01T00:00:00.000Z,2011-06-01T00:00:00.000Z,2011-07-01T00:00:00.000Z,2011-08-01T00:00:00.000Z,2011-09-01T00:00:00.000Z,2011-10-01T00:00:00.000Z,2011-11-01T00:00:00.000Z,2011-12-01T00:00:00.000Z,2012-01-01T00:00:00.000Z,2012-02-01T00:00:00.000Z,2012-03-01T00:00:00.000Z,2012-04-01T00:00:00.000Z,2012-05-01T00:00:00.000Z,2012-06-01T00:00:00.000Z,2012-07-01T00:00:00.000Z,2012-08-01T00:00:00.000Z,2012-09-01T00:00:00.000Z,2012-10-01T00:00:00.000Z,2012-11-01T00:00:00.000Z,2012-12-01T00:00:00.000Z,2013-01-01T00:00:00.000Z,2013-02-01T00:00:00.000Z,2013-03-01T00:00:00.000Z,2013-04-01T00:00:00.000Z,2013-05-01T00:00:00.000Z,2013-06-01T00:00:00.000Z,2013-07-01T00:00:00.000Z,2013-08-01T00:00:00.000Z,2013-09-01T00:00:00.000Z,2013-10-01T00:00:00.000Z,2013-11-01T00:00:00.000Z,2013-12-01T00:00:00.000Z</Extent>
     */
    private static String[] parseTimes(Element dimension, Element extent) {
        // TODO: parse start, end, step
        if (dimension == null || extent == null) {
            return null;
        }
        if (!"time".equals(XmlHelper.getAttributeValue(extent, "name"))) {
            return null;
        }
        if (!"ISO8601".equals(XmlHelper.getAttributeValue(dimension, "units"))) {
            return null;
        }
        String content = extent.getTextContent();
        if (content.trim().isEmpty()) {
            return null;
        }
        return content.split("\\s*,\\s*");
    }
}
