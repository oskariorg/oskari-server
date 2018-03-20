package fi.nls.oskari.myplaces.service.wfst;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.geojson.GeoJSONReader;
import org.oskari.wfst.WFSTRequestBuilder;

import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.util.GML2Writer;
import fi.nls.oskari.util.JSONHelper;

public class MyPlacesFeaturesWFSTRequestBuilder extends WFSTRequestBuilder {

    private static final String TYPENAME_MY_PLACES = "feature:my_places";
    private static final String APPLICATION_JSON = "application/json";

    public static List<MyPlace> parseMyPlaces(String input, boolean shouldSetId)
            throws JSONException {
        JSONObject featureCollection = new JSONObject(input);
        // Expect custom key featureCollection.srsName to contain srid in pattern of 'EPSG:srid'
        // if that doesn't exist or if we fail to parse the srid part out of it use 0 (unknown)
        String srsName = JSONHelper.optString(featureCollection, "srsName");
        int srid = getSrid(srsName, 0);
        JSONArray features = featureCollection.getJSONArray("features");
        final int n = features.length();
        List<MyPlace> myPlaces = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            JSONObject feature = features.getJSONObject(i);
            myPlaces.add(parseMyPlace(feature, shouldSetId, srid));
        }
        return myPlaces;
    }

    private static int getSrid(String srsName, int defaultValue) {
        if (srsName != null) {
            int i = srsName.lastIndexOf(':');
            if (i > 0) {
                srsName = srsName.substring(i + 1);
            }
            try {
                return Integer.parseInt(srsName);
            } catch (NumberFormatException ignroe) {}
        }
        return defaultValue;
    }

    public static MyPlace parseMyPlace(JSONObject feature, boolean shouldSetId, int srid)
            throws JSONException {
        MyPlace myPlace = new MyPlace();

        if (shouldSetId) {
            myPlace.setId(feature.getLong("id"));
        }
        myPlace.setCategoryId(feature.getLong("category_id"));

        JSONObject geomJSON = feature.getJSONObject("geometry");
        Geometry geom = GeoJSONReader.toGeometry(geomJSON);
        geom.setSRID(srid);
        myPlace.setGeometry(geom);

        JSONObject properties = feature.getJSONObject("properties");
        myPlace.setName(JSONHelper.getString(properties, "name"));

        // Optional fields
        myPlace.setAttentionText(JSONHelper.optString(properties, "attention_text"));
        myPlace.setDesc(JSONHelper.optString(properties, "place_desc"));
        myPlace.setLink(JSONHelper.optString(properties, "link"));
        myPlace.setImageUrl(JSONHelper.optString(properties, "image_url"));

        return myPlace;
    }

    public static void getMyPlacesByCategoryId(OutputStream out, String crs,
            long categoryId) throws XMLStreamException {
        getMyPlacesByProperty(out, crs, "category_id", Long.toString(categoryId));
    }

    public static void getMyPlacesByUserId(OutputStream out, String crs,
            String uuid) throws XMLStreamException {
        getMyPlacesByProperty(out, crs, "uuid", uuid);
    }

    private static void getMyPlacesByProperty(OutputStream out, String crs,
            String property, String value) throws XMLStreamException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        // Use WFS 1.0.0 for MyPlacesFeatures
        writeGetFeature(xsw, "1.0.0");
        xsw.writeAttribute("outputFormat", APPLICATION_JSON);

        xsw.writeStartElement(WFS, "Query");
        xsw.writeAttribute("typeName", TYPENAME_MY_PLACES);
        xsw.writeAttribute("srsName", crs);

        xsw.writeStartElement(OGC, "Filter");
        xsw.writeStartElement(OGC, "PropertyIsEqualTo");
        writeTextElement(xsw, OGC, "PropertyName", property);
        writeTextElement(xsw, OGC, "Literal", value);
        xsw.writeEndElement();
        xsw.writeEndElement();

        xsw.writeEndElement(); // <wfs:Query>

        xsw.writeEndElement(); // <wfs:GetFeature>
        xsw.writeEndDocument();
        xsw.close();
    }

    public static void getMyPlacesById(OutputStream out, String crs,
            String[] ids) throws XMLStreamException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        // Use WFS 1.0.0 for MyPlacesFeatures
        writeGetFeature(xsw, "1.0.0");
        xsw.writeAttribute("outputFormat", APPLICATION_JSON);

        xsw.writeStartElement(WFS, "Query");
        xsw.writeAttribute("typeName", TYPENAME_MY_PLACES);
        xsw.writeAttribute("srsName", crs);

        writeFeatureIdFilter(xsw, ids);

        xsw.writeEndElement(); // <wfs:Query>

        xsw.writeEndElement(); // <wfs:GetFeature>
        xsw.writeEndDocument();
        xsw.close();
    }

    public static void insertMyPlaces(OutputStream out, List<MyPlace> places)
            throws XMLStreamException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        // Use WFS 1.0.0 for MyPlacesFeatures
        writeStartTransaction(xsw, "1.0.0");
        for (MyPlace place : places) {
            insertMyPlace(xsw, place);
        }
        xsw.writeEndElement();
        xsw.writeEndDocument();
        xsw.close();
    }

    public static void updateMyPlaces(OutputStream out, List<MyPlace> places)
            throws XMLStreamException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        // Use WFS 1.0.0 for MyPlacesFeatures
        writeStartTransaction(xsw, "1.0.0");
        for (MyPlace place : places) {
            updateMyPlace(xsw, place);
        }
        xsw.writeEndElement();
        xsw.writeEndDocument();
        xsw.close();
    }

    public static void deleteMyPlaces(OutputStream out, long[] ids)
            throws XMLStreamException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        // Use WFS 1.0.0 for MyPlacesFeatures
        writeStartTransaction(xsw, "1.0.0");
        for (long id : ids) {
            deleteMyPlace(xsw, id);
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

    public static String prefixId(long id) {
        return "my_places." + id;
    }

    public static String[] prefixIds(long[] ids) {
        final int n = ids.length;
        String[] prefixed = new String[n];
        for (int i = 0; i < n; i++) {
            prefixed[i] = prefixId(ids[i]);
        }
        return prefixed;
    }

    public static long removePrefixFromId(String prefixed) {
        return Long.parseLong(prefixed.substring("my_places.".length()));
    }

    public static long[] removePrefixFromIds(String[] prefixed) {
        final int n = prefixed.length;
        long[] ids = new long[n];
        for (int i = 0; i < n; i++) {
            ids[i] = removePrefixFromId(prefixed[i]);
        }
        return ids;
    }

}
