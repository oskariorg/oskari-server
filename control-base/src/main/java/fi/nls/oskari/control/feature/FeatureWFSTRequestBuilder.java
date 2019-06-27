package fi.nls.oskari.control.feature;

import fi.nls.oskari.domain.map.Feature;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.GML3Writer;
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

        addGeometries(xsw, feature, true);
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
        xsw.writeAttribute("xmlns:" + feature.getNamespace(), feature.getNamespaceURI());

        for (Map.Entry<String, String> property : feature.getProperties().entrySet()) {
            xsw.writeStartElement(property.getKey());
            xsw.writeCharacters(property.getValue());
            xsw.writeEndElement();
        }

        addGeometries(xsw, feature, false);
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

    private static void addGeometries(XMLStreamWriter xsw, Feature feature, boolean isUpdate) throws XMLStreamException {
        if(!feature.hasGeometry()) {
            return;
        }

        if(isUpdate == false) {
            xsw.writeStartElement(feature.getGMLGeometryProperty());
            GML3Writer.writeGeometry(xsw, feature.getGeometry());
            xsw.writeEndElement();
            return;
        }

        xsw.writeStartElement(WFS, "Property");

        xsw.writeStartElement(WFS, "Name");
        xsw.writeCharacters(feature.getGMLGeometryProperty());
        xsw.writeEndElement();

        xsw.writeStartElement(WFS, "Value");
        GML3Writer.writeGeometry(xsw, feature.getGeometry());
        xsw.writeEndElement();

        xsw.writeEndElement();
    }

}
