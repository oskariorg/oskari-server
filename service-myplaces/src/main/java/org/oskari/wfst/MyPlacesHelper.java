package org.oskari.wfst;

import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import fi.nls.oskari.domain.map.MyPlace;

public class MyPlacesHelper extends WFSTHelper {

    private static final String TYPENAME_MY_PLACES = "feature:my_places";

    public static void insertMyPlaces(OutputStream out, MyPlace[] places)
            throws XMLStreamException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        writeStartTransaction(xsw);
        xsw.writeNamespace(PREFIX_OSKARI, OSKARI);
        for (MyPlace place : places) {
            insertMyPlace(xsw, place);
        }
        xsw.writeEndElement();
        xsw.writeEndDocument();
        xsw.close();
    }

    public static void deleteMyPlaces(OutputStream out, long[] ids)
            throws XMLStreamException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        writeStartTransaction(xsw);
        xsw.writeNamespace(PREFIX_OSKARI, OSKARI);
        for (long id : ids) {
            deleteMyPlace(xsw, id);
        }
        xsw.writeEndElement();
        xsw.writeEndDocument();
        xsw.close();
    }

    public static void updateMyPlaces(OutputStream out, MyPlace[] places)
            throws XMLStreamException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        writeStartTransaction(xsw);
        xsw.writeNamespace(PREFIX_OSKARI, OSKARI);
        for (MyPlace place : places) {
            updateMyPlace(xsw, place);
        }
        xsw.writeEndElement();
        xsw.writeEndDocument();
        xsw.close();
    }

    private static void insertMyPlace(XMLStreamWriter xsw, MyPlace place)
            throws XMLStreamException {
        xsw.writeStartElement(WFS, "Insert");
        xsw.writeAttribute("typeName", TYPENAME_MY_PLACES);
        xsw.writeStartElement(OSKARI, "my_places");
        xsw.writeStartElement(OSKARI, "geometry");
        GML2Writer.writeGeometry(xsw, place.getGeometry());
        xsw.writeEndElement();
        writeTextElement(xsw, OSKARI, "uuid", place.getUuid());
        writeTextElement(xsw, OSKARI, "category_id", place.getCategoryId());
        writeTextElement(xsw, OSKARI, "name", place.getName());
        writeTextElement(xsw, OSKARI, "attention_text", place.getAttentionText());
        writeTextElement(xsw, OSKARI, "place_desc", place.getDesc());
        writeTextElement(xsw, OSKARI, "link", place.getLink());
        writeTextElement(xsw, OSKARI, "image_url", place.getImageUrl());
        xsw.writeEndElement(); // Close <feature:my_places>
        xsw.writeEndElement(); // Close <wfs:Insert>
    }

    private static void updateMyPlace(XMLStreamWriter xsw, MyPlace place)
            throws XMLStreamException {
        xsw.writeStartElement(WFS, "Update");
        xsw.writeAttribute("typeName", TYPENAME_MY_PLACES);

        // Geometry
        xsw.writeStartElement(WFS, "Property");
        writeTextElement(xsw, WFS, "Name", "geometry");
        xsw.writeStartElement(WFS, "Value");
        GML2Writer.writeGeometry(xsw, place.getGeometry());
        xsw.writeEndElement();
        xsw.writeEndElement();

        writeProperty(xsw, "uuid", place.getUuid());
        writeProperty(xsw, "category_id", place.getCategoryId());
        writeProperty(xsw, "name", place.getName());
        writeProperty(xsw, "attention_text", place.getAttentionText());
        writeProperty(xsw, "place_desc", place.getDesc());
        writeProperty(xsw, "link", place.getLink());
        writeProperty(xsw, "image_url", place.getImageUrl());

        writeFeatureIdFilter(xsw, prefixId(place.getId()));

        xsw.writeEndElement(); // close <wfs:Update>
    }

    private static void deleteMyPlace(XMLStreamWriter xsw, long id)
            throws XMLStreamException {
        xsw.writeStartElement(WFS, "Delete");
        xsw.writeAttribute("typeName", TYPENAME_MY_PLACES);
        writeFeatureIdFilter(xsw, prefixId(id));
        xsw.writeEndElement();
    }

    private static String prefixId(long id) {
        return "my_places." + id;
    }

}
