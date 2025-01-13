package fi.nls.oskari.control.feature;

import fi.nls.oskari.domain.map.Feature;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.util.GML3Writer;

import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.wfst.WFSTRequestBuilder;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.util.Map;

public class FeatureWFSTRequestBuilder extends WFSTRequestBuilder {

    private final static Logger LOG = LogFactory.getLogger(FeatureWFSTRequestBuilder.class);

    public static void updateFeature(OutputStream out, Feature feature)
            throws XMLStreamException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        writeStartTransaction(xsw, "1.1.0");

        xsw.writeStartElement(WFS, "Update");
        xsw.writeAttribute("typeName", feature.getLayerName());

        for (Map.Entry<String, String> property : feature.getProperties().entrySet()) {
            xsw.writeStartElement(WFS, "Property");

            xsw.writeStartElement(WFS, "Name");
            xsw.writeCharacters(property.getKey());
            xsw.writeEndElement();

            xsw.writeStartElement(WFS, "Value");
            xsw.writeCharacters(property.getValue());
            xsw.writeEndElement();

            xsw.writeEndElement();
        }

        writeGeometryProperty(xsw, feature);
        writeFeatureIdFilter(xsw, feature.getId());
        xsw.writeEndElement(); // close <wfs:Update>

        xsw.writeEndElement();
        xsw.writeEndDocument();
        xsw.close();
    }

    public static void insertFeature(OutputStream out, Feature feature)
            throws XMLStreamException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        writeStartTransaction(xsw, "1.1.0");

        xsw.writeStartElement(WFS, "Insert");
        xsw.writeStartElement(feature.getLayerName());

        for (Map.Entry<String, String> property : feature.getProperties().entrySet()) {
            xsw.writeStartElement(property.getKey());
            xsw.writeCharacters(property.getValue());
            xsw.writeEndElement();
        }

        if (feature.hasGeometry()) {
            xsw.writeStartElement(feature.getGMLGeometryProperty());
            writeGeometry(xsw, feature.getGeometry());
            xsw.writeEndElement();
        }

        xsw.writeEndElement();
        xsw.writeEndElement(); // close <wfs:Insert>

        xsw.writeEndElement();
        xsw.writeEndDocument();
        xsw.close();
    }

    public static void deleteFeature(OutputStream out, Feature feature)
            throws XMLStreamException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        writeStartTransaction(xsw, "1.1.0");

        xsw.writeStartElement(WFS, "Delete");
        xsw.writeAttribute("typeName", feature.getLayerName());
        writeFeatureIdFilter(xsw, feature.getId());
        xsw.writeEndElement(); // close <wfs:Delete>

        xsw.writeEndElement();
        xsw.writeEndDocument();
        xsw.close();
    }

    private static void writeGeometryProperty(XMLStreamWriter xsw, Feature feature) throws XMLStreamException {
        if(!feature.hasGeometry()) {
            return;
        }

        xsw.writeStartElement(WFS, "Property");

        xsw.writeStartElement(WFS, "Name");
        xsw.writeCharacters(feature.getGMLGeometryProperty());
        xsw.writeEndElement();

        xsw.writeStartElement(WFS, "Value");
        writeGeometry(xsw, feature.getGeometry());
        xsw.writeEndElement();

        xsw.writeEndElement();
    }

    private static void writeGeometry(XMLStreamWriter xsw, Geometry geometry) throws XMLStreamException {
        boolean xyOrder = true;
        if (geometry.getSRID() != 0) {
            String srsName = GML3Writer.getSrsName(geometry.getSRID());
            try {
                CoordinateReferenceSystem crs = CRS.decode(srsName);
                xyOrder = !ProjectionHelper.isFirstAxisNorth(crs);
                LOG.debug("srsName:", srsName, "xyOrder:", xyOrder);
            } catch (FactoryException e) {
                LOG.warn(e);
            }
        }
        GML3Writer.writeGeometry(xsw, geometry, xyOrder);
    }

}
