package org.oskari.wfst;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import fi.nls.oskari.myplaces.MyPlaceWithGeometry;
import fi.nls.oskari.util.IOHelper;

public class MyPlacesHelper extends WFSTHelper {

    private static final String TYPENAME_MY_PLACES = "feature:my_places";

    public static void insertMyPlaces(OutputStream out, MyPlaceWithGeometry[] places)
            throws XMLStreamException, IOException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        writeStartTransaction(xsw);
        xsw.writeNamespace(PREFIX_OSKARI, OSKARI);
        for (MyPlaceWithGeometry place : places) {
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

    public static void updateMyPlaces(OutputStream out, MyPlaceWithGeometry[] places)
            throws XMLStreamException, IOException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        writeStartTransaction(xsw);
        xsw.writeNamespace(PREFIX_OSKARI, OSKARI);
        for (MyPlaceWithGeometry place : places) {
            updateMyPlace(xsw, place);
        }
        xsw.writeEndElement();
        xsw.writeEndDocument();
        xsw.close();
    }

    private static void insertMyPlace(XMLStreamWriter xsw, MyPlaceWithGeometry place)
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

    private static void updateMyPlace(XMLStreamWriter xsw, MyPlaceWithGeometry place)
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
        return "myplaces." + id;
    }

    public static void main(String[] args) throws XMLStreamException, IOException, SAXException, ParserConfigurationException {
        MyPlaceWithGeometry place = new MyPlaceWithGeometry();
        place.setName("foobar");
        place.setUuid(UUID.randomUUID().toString());
        place.setCategoryId(4);
        place.setDesc("My description");
        place.setAttentionText("My attentionText");
        place.setLink("My link");
        GeometryFactory gf = new GeometryFactory();
        place.setGeometry(gf.createPoint(new Coordinate(500000, 6822000)));
        MyPlaceWithGeometry[] places = { place };
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        insertMyPlaces(baos, places);
        byte[] req = baos.toByteArray();
        System.out.write(req);
        System.out.println();
        HttpURLConnection conn = IOHelper.post("http://localhost:6082/geoserver/wms", "application/xml", req);
        byte[] resp = IOHelper.readBytes(conn);
        System.out.write(resp);
        System.out.println();
        TransactionResponse_110 tr = TransactionResponseParser_110.parse(new ByteArrayInputStream(resp));

        String id = tr.getInsertedIds()[0];
        long idWithoutPrefix = Long.parseLong(id.substring(id.lastIndexOf('.') + 1));
        place.setId(idWithoutPrefix);
        place.setDesc("bazzzz");
        baos.reset();
        updateMyPlaces(baos, places);
        req = baos.toByteArray();
        System.out.write(req);
        System.out.println();
        conn = IOHelper.post("http://localhost:6082/geoserver/wms", "application/xml", req);
        resp = IOHelper.readBytes(conn);
        System.out.write(resp);
        System.out.println();
        tr = TransactionResponseParser_110.parse(new ByteArrayInputStream(resp));
    }

}
