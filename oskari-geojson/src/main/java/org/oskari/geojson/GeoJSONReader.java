package org.oskari.geojson;

import java.util.Iterator;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Convert org.json.JSONObjects (that follow GeoJSON spec) to
 *  - GeoTools SimpleFeature(Collection)s
 *  - JTS Geometries
 */
public class GeoJSONReader {

    private static final GeometryFactory GF = new GeometryFactory();
    private static final String GEOM_ATTRIBUTE = "geometry";

    public static SimpleFeatureCollection toFeatureCollection(JSONObject json)
            throws JSONException {
        return toFeatureCollection(json, null);
    }

    public static SimpleFeatureCollection toFeatureCollection(JSONObject json, SimpleFeatureBuilder builder)
            throws JSONException {
        String type = json.getString(GeoJSON.TYPE);
        if (!GeoJSON.FEATURE_COLLECTION.equals(type)) {
            throw new IllegalArgumentException("type was not " + GeoJSON.FEATURE_COLLECTION);
        }

        JSONArray features = json.getJSONArray(GeoJSON.FEATURES);
        final int n = features.length();
        if (n == 0) {
            return new DefaultFeatureCollection();
        }

        if (builder == null) {
            builder = getBuilder(features.getJSONObject(0));
        }

        DefaultFeatureCollection fc = new DefaultFeatureCollection();
        for (int i = 0; i < n; i++) {
            fc.add(toFeature(features.getJSONObject(i), builder));
        }

        return fc;
    }

    public static SimpleFeature toFeature(JSONObject json)
            throws JSONException {
        return toFeature(json, null);
    }

    public static SimpleFeature toFeature(JSONObject json, SimpleFeatureBuilder builder)
            throws JSONException {
        String type = json.getString(GeoJSON.TYPE);
        if (!GeoJSON.FEATURE.equals(type)) {
            throw new IllegalArgumentException("type was not " + GeoJSON.FEATURE);
        }

        if (builder == null) {
            builder = getBuilder(json);
        }

        builder.reset();

        JSONObject geometry = json.optJSONObject(GeoJSON.GEOMETRY);
        if (geometry != null) {
            builder.set(GEOM_ATTRIBUTE, toGeometry(geometry));
        }

        JSONObject properties = json.optJSONObject(GeoJSON.PROPERTIES);
        if (properties != null) {
            @SuppressWarnings("unchecked")
            Iterator<String> keys = properties.keys();
            while (keys.hasNext()) {
                String name = keys.next();
                Object value = properties.get(name);
                builder.set(name, value);
            }
        }

        String id = json.optString(GeoJSON.ID);
        return builder.buildFeature(id);
    }

    public static SimpleFeatureBuilder getBuilder(JSONObject json)
            throws JSONException {
        String type = json.getString(GeoJSON.TYPE);
        if (!GeoJSON.FEATURE.equals(type)) {
            throw new IllegalArgumentException("type was not " + GeoJSON.FEATURE);
        }

        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("temp");
        b.setNamespaceURI("http://oskari.org");

        JSONObject geom = json.optJSONObject(GeoJSON.GEOMETRY);
        if (geom != null) {
            b.setDefaultGeometry(GEOM_ATTRIBUTE);
            switch (geom.getString(GeoJSON.TYPE)) {
            case GeoJSON.POINT:
                b.add(GEOM_ATTRIBUTE, Point.class);
                break;
            case GeoJSON.LINESTRING:
                b.add(GEOM_ATTRIBUTE, LineString.class);
                break;
            case GeoJSON.POLYGON:
                b.add(GEOM_ATTRIBUTE, Polygon.class);
                break;
            case GeoJSON.MULTI_POINT:
                b.add(GEOM_ATTRIBUTE, MultiPoint.class);
                break;
            case GeoJSON.MULTI_LINESTRING:
                b.add(GEOM_ATTRIBUTE, MultiLineString.class);
                break;
            case GeoJSON.MULTI_POLYGON:
                b.add(GEOM_ATTRIBUTE, MultiPolygon.class);
                break;
            case GeoJSON.GEOMETRY_COLLECTION:
                b.add(GEOM_ATTRIBUTE, GeometryCollection.class);
                break;
            }
        }

        JSONObject properties = json.optJSONObject(GeoJSON.PROPERTIES);
        if (properties != null) {
            @SuppressWarnings("unchecked")
            Iterator<String> keys = properties.keys();
            while (keys.hasNext()) {
                String name = keys.next();
                Object value = properties.get(name);
                b.add(name, value.getClass());
            }
        }

        SimpleFeatureType t = b.buildFeatureType();
        return new SimpleFeatureBuilder(t);
    }

    public static Geometry toGeometry(JSONObject geometry)
            throws JSONException {
        String geomType = geometry.getString(GeoJSON.TYPE);
        switch (geomType) {
        case GeoJSON.POINT:
            return toPoint(geometry);
        case GeoJSON.LINESTRING:
            return toLineString(geometry);
        case GeoJSON.POLYGON:
            return toPolygon(geometry);
        case GeoJSON.MULTI_POINT:
            return toMultiPoint(geometry);
        case GeoJSON.MULTI_LINESTRING:
            return toMultiLineString(geometry);
        case GeoJSON.MULTI_POLYGON:
            return toMultiPolygon(geometry);
        case GeoJSON.GEOMETRY_COLLECTION:
            return toGeometryCollection(geometry);
        default:
            throw new IllegalArgumentException("Invalid geometry type");
        }
    }

    public static Point toPoint(JSONObject geometry)
            throws JSONException {
        return GF.createPoint(toCoordinate(geometry.getJSONArray(GeoJSON.COORDINATES)));
    }

    public static LineString toLineString(JSONObject geometry)
            throws JSONException {
        Coordinate[] coordinates = toCoordinates(geometry.getJSONArray(GeoJSON.COORDINATES));
        return GF.createLineString(coordinates);
    }

    public static Polygon toPolygon(JSONObject geometry)
            throws JSONException {
        return toPolygon(geometry.getJSONArray(GeoJSON.COORDINATES));
    }

    public static MultiPoint toMultiPoint(JSONObject geometry)
            throws JSONException {
        Coordinate[] coordinates = toCoordinates(geometry.getJSONArray(GeoJSON.COORDINATES));
        return GF.createMultiPoint(coordinates);
    }

    public static MultiLineString toMultiLineString(JSONObject geometry)
            throws JSONException {
        Coordinate[][] coordinates = toCoordinatesArray(geometry.getJSONArray(GeoJSON.COORDINATES));
        int n = coordinates.length;
        LineString[] lineStrings = new LineString[n];
        for (int i = 0; i < n; i++) {
            lineStrings[i] = GF.createLineString(coordinates[i]);
        }
        return GF.createMultiLineString(lineStrings);
    }

    public static MultiPolygon toMultiPolygon(JSONObject geometry)
            throws JSONException {
        JSONArray arrayOfPolygons = geometry.getJSONArray(GeoJSON.COORDINATES);
        int n = arrayOfPolygons.length();
        Polygon[] polygons = new Polygon[n];
        for (int i = 0; i < n; i++) {
            polygons[i] = toPolygon(arrayOfPolygons.getJSONArray(i));
        }
        return GF.createMultiPolygon(polygons);
    }

    public static GeometryCollection toGeometryCollection(JSONObject geometry)
            throws JSONException {
        JSONArray geometryArray = geometry.getJSONArray(GeoJSON.GEOMETRIES);
        int n = geometryArray.length();
        Geometry[] geometries = new Geometry[n];
        for (int i = 0; i < n; i++) {
            geometries[i] = toGeometry(geometryArray.getJSONObject(i));
        }
        return GF.createGeometryCollection(geometries);
    }

    private static Coordinate toCoordinate(JSONArray coordinate)
            throws JSONException {
        return new Coordinate(coordinate.getDouble(0), coordinate.getDouble(1));
    }

    private static Coordinate[] toCoordinates(JSONArray arrayOfCoordinates)
            throws JSONException {
        int n = arrayOfCoordinates.length();
        Coordinate[] coordinates = new Coordinate[n];
        for (int i = 0; i < n; i++) {
            coordinates[i] = toCoordinate(arrayOfCoordinates.getJSONArray(i));
        }
        return coordinates;
    }

    private static Coordinate[][] toCoordinatesArray(JSONArray arrayOfArrayOfCoordinates)
            throws JSONException {
        int n = arrayOfArrayOfCoordinates.length();
        Coordinate[][] coordinates = new Coordinate[n][];
        for (int i = 0; i < n; i++) {
            coordinates[i] = toCoordinates(arrayOfArrayOfCoordinates.getJSONArray(i));
        }
        return coordinates;
    }

    private static Polygon toPolygon(JSONArray coordinatesArray)
            throws JSONException {
        Coordinate[][] coordinates = toCoordinatesArray(coordinatesArray);
        LinearRing exterior = GF.createLinearRing(coordinates[0]);
        LinearRing[] interiors = new LinearRing[coordinates.length - 1];
        for (int i = 1; i < coordinates.length; i++) {
            interiors[i - 1] = GF.createLinearRing(coordinates[i]);
        }
        return GF.createPolygon(exterior, interiors);
    }

}
