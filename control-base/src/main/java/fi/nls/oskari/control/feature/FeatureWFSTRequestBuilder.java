package fi.nls.oskari.control.feature;

import fi.nls.oskari.domain.map.Feature;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.util.GML3Writer;

import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.wfst.WFSTRequestBuilder;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.util.Map;

public class FeatureWFSTRequestBuilder extends WFSTRequestBuilder {

    @Deprecated
    /**
     * @deprecated see updateFeature(OutputStream, Feature, CoordinateReferenceSystem)
     */
    public static void updateFeature(OutputStream out, Feature feature)
            throws XMLStreamException {
        updateFeature(out, feature, null);
    }

    public static void updateFeature(OutputStream out, Feature feature, CoordinateReferenceSystem crs)
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

        writeGeometryProperty(xsw, feature, crs);
        writeFeatureIdFilter(xsw, feature.getId());
        xsw.writeEndElement(); // close <wfs:Update>

        xsw.writeEndElement();
        xsw.writeEndDocument();
        xsw.close();
    }

    @Deprecated
    /**
     * @deprecated see insertFeature(OutputStream, Feature, CoordinateReferenceSystem)
     */
    public static void insertFeature(OutputStream out, Feature feature)
            throws XMLStreamException {
        insertFeature(out, feature, null);
    }

    public static void insertFeature(OutputStream out, Feature feature, CoordinateReferenceSystem crs)
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
            writeGeometry(xsw, feature.getGeometry(), crs);
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

    private static void writeGeometryProperty(XMLStreamWriter xsw, Feature feature, CoordinateReferenceSystem crs) throws XMLStreamException {
        if(!feature.hasGeometry()) {
            return;
        }

        xsw.writeStartElement(WFS, "Property");

        xsw.writeStartElement(WFS, "Name");
        xsw.writeCharacters(feature.getGMLGeometryProperty());
        xsw.writeEndElement();

        xsw.writeStartElement(WFS, "Value");
        writeGeometry(xsw, feature.getGeometry(), crs);
        xsw.writeEndElement();

        xsw.writeEndElement();
    }

    private static void writeGeometry(XMLStreamWriter xsw, Geometry geometry, CoordinateReferenceSystem crs) throws XMLStreamException {
        boolean xyOrder = true;
        if (crs != null) {
            xyOrder = ProjectionHelper.isFirstAxisNorth(crs);
        }
        GML3Writer.writeGeometry(xsw, geometry, xyOrder);
    }

}
