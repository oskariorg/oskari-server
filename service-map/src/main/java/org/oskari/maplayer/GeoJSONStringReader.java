package org.oskari.maplayer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.referencing.CRS;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.oskari.geojson.GeoJSONReader2;
import org.oskari.geojson.GeoJSONSchemaDetector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class GeoJSONStringReader {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final TypeReference<HashMap<String, Object>> TYPE_REF = new TypeReference<HashMap<String, Object>>() {};

    public static SimpleFeatureCollection readGeoJSON(InputStream in, CoordinateReferenceSystem crs) throws Exception {
        if (in == null) {
            throw new IllegalArgumentException("InputStream was null");
        }
        try (Reader utf8Reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            Map<String, Object> geojsonAsMap = loadJSONResource(utf8Reader);
            boolean ignoreGeometryProperties = true;
            SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(geojsonAsMap, crs, ignoreGeometryProperties);
            return GeoJSONReader2.toFeatureCollection(geojsonAsMap, schema);
        }
    }

    public static SimpleFeatureCollection readGeoJSON(String geojson, String srs) throws Exception{
        CoordinateReferenceSystem sourceCRS = CRS.decode(srs);
        return readGeoJSON(geojson, sourceCRS);
    }

    public static SimpleFeatureCollection readGeoJSON(String geojson, CoordinateReferenceSystem crs) throws Exception {
        Map<String, Object> geojsonAsMap = loadJSONResource(geojson);
        boolean ignoreGeometryProperties = true;
        try {
            SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(geojsonAsMap, crs, ignoreGeometryProperties);
            return GeoJSONReader2.toFeatureCollection(geojsonAsMap, schema);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Input was not GeoJSON");
        }
    }



    private static Map<String, Object> loadJSONResource(Reader utf8Reader) throws Exception {
        try {
            return OM.readValue(utf8Reader, TYPE_REF);
        } catch (MismatchedInputException e) {
            throw new IllegalArgumentException("Input couldn't be parsed as JSON Object");
        }
    }

    private static Map<String, Object> loadJSONResource(String geojson) throws Exception {
        try {
            return OM.readValue(geojson, TYPE_REF);
        } catch (MismatchedInputException e) {
            throw new IllegalArgumentException("Input couldn't be parsed as JSON Object");
        }
    }
}
