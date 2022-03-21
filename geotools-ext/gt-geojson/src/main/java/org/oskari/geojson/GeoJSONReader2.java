package org.oskari.geojson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.store.EmptyFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * Convert Map<String, Object> (that follow GeoJSON spec) to
 *  - GeoTools SimpleFeatureCollection
 *  - JTS Geometries
 * For invalid input we throw IllegalArgumentExceptions
 */
public class GeoJSONReader2 {

    private static final GeometryFactory GF = new GeometryFactory();

    public static SimpleFeatureCollection toFeatureCollection(Map<String, Object> json, SimpleFeatureType schema) {
        try {
            return toFeatureCollection(json, schema, null);
        } catch (MismatchedDimensionException | TransformException ignore) {
            // These can't occur because we don't transform anything
            throw new RuntimeException("Something bad happened");
        }
    }

    public static SimpleFeatureCollection toFeatureCollection(Map<String, Object> json,
            SimpleFeatureType schema, MathTransform transform)
                    throws MismatchedDimensionException, TransformException {
        return toFeatureCollection(json, schema, null, Filter.INCLUDE);
    }

    public static SimpleFeatureCollection toFeatureCollection(
            Map<String, Object> json,
            SimpleFeatureType schema,
            MathTransform transform,
            Filter filter) throws MismatchedDimensionException, TransformException {
        if (filter == null) {
            filter = Filter.INCLUDE;
        }

        String type = GeoJSONUtil.getString(json, GeoJSON.TYPE);
        if (!GeoJSON.FEATURE_COLLECTION.equals(type)) {
            throw new IllegalArgumentException("type was not " + GeoJSON.FEATURE_COLLECTION);
        }

        List<Object> features = GeoJSONUtil.getList(json, GeoJSON.FEATURES);
        if (features.isEmpty()) {
            return new EmptyFeatureCollection(schema);
        }

        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
        List<SimpleFeature> fc = new ArrayList<>();
        for (Object f : features) {
            @SuppressWarnings("unchecked")
            Map<String, Object> _f = (Map<String, Object>) f;
            SimpleFeature feature = toFeature(_f, builder, transform);
            if (filter.evaluate(feature)) {
                fc.add(feature);
            }
        }

        return new GeoJSONFeatureCollection(fc, schema);
    }

    public static SimpleFeature toFeature(Map<String, Object> json, SimpleFeatureType schema) {
        try {
            return toFeature(json, new SimpleFeatureBuilder(schema), null);
        } catch (MismatchedDimensionException | TransformException ignore) {
            // These can't occur because we don't transform anything
            throw new RuntimeException("Something bad happened");
        }
    }

    public static SimpleFeature toFeature(Map<String, Object> json, SimpleFeatureType schema,
            MathTransform transform) throws MismatchedDimensionException, TransformException {
        return toFeature(json, new SimpleFeatureBuilder(schema), transform);
    }

    @SuppressWarnings("unchecked")
    private static SimpleFeature toFeature(Map<String, Object> json, SimpleFeatureBuilder builder,
            MathTransform transform) throws MismatchedDimensionException, TransformException {
        if (!GeoJSON.FEATURE.equals(GeoJSONUtil.getString(json, GeoJSON.TYPE))) {
            throw new IllegalArgumentException("type was not " + GeoJSON.FEATURE);
        }

        builder.reset();

        Object _id = json.get(GeoJSON.ID);
        // If id is null SimpleFeatureBuilder will create one
        String id = _id != null ? _id.toString() : null;

        Object geom = json.get(GeoJSON.GEOMETRY);
        if (geom != null) {
            if (geom instanceof Map) {
                geom = toGeometry((Map<String, Object>) geom);
            }
            if (transform != null) {
                geom = JTS.transform((Geometry) geom, transform);
            }
            builder.set(GeoJSONUtil.DEFAULT_GEOMETRY_ATTRIBUTE_NAME, geom);
        }

        Map<String, Object> properties = GeoJSONUtil.getMap(json, GeoJSON.PROPERTIES);
        if (properties != null) {
            SimpleFeatureType schema = builder.getFeatureType();
            for (int i = 0; i < schema.getAttributeCount(); i++) {
                AttributeDescriptor ad = schema.getDescriptor(i);
                String name = ad.getLocalName();
                if (GeoJSONUtil.DEFAULT_GEOMETRY_ATTRIBUTE_NAME.equals(name)) {
                    continue;
                }

                Object value = properties.get(name);
                if (value == null) {
                    continue;
                }

                Class<?> type = ad.getClass();
                if (type.isAssignableFrom(Geometry.class)) {
                    if (value instanceof Map) {
                        value = toGeometry((Map<String, Object>) value);
                    }
                    if (transform != null) {
                        value = JTS.transform((Geometry) value, transform);
                    }
                }

                // Trust GeoTools to convert the value to proper class
                builder.set(i, value);
            }
        }

        return builder.buildFeature(id);
    }

    public static Geometry toGeometry(Map<String, Object> geometry) {
        String geomType = GeoJSONUtil.getString(geometry, GeoJSON.TYPE);
        if (geomType == null) {
            throw new IllegalArgumentException("Invalid geometry type");
        }
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
        }
        throw new IllegalArgumentException("Invalid geometry type");
    }

    public static Point toPoint(Map<String, Object> geometry) {
        return GF.createPoint(toCoordinate(
                GeoJSONUtil.getList(geometry, GeoJSON.COORDINATES)));
    }

    public static LineString toLineString(Map<String, Object> geometry) {
        Coordinate[] coordinates = toCoordinates(
                GeoJSONUtil.getList(geometry, GeoJSON.COORDINATES));
        return GF.createLineString(coordinates);
    }

    public static Polygon toPolygon(Map<String, Object> geometry) {
        return toPolygon(
                GeoJSONUtil.getList(geometry, GeoJSON.COORDINATES));
    }

    public static MultiPoint toMultiPoint(Map<String, Object> geometry) {
        Coordinate[] coordinates = toCoordinates(
                GeoJSONUtil.getList(geometry, GeoJSON.COORDINATES));
        return GF.createMultiPoint(coordinates);
    }

    public static MultiLineString toMultiLineString(Map<String, Object> geometry) {
        Coordinate[][] coordinates = toCoordinatesArray(
                GeoJSONUtil.getList(geometry, GeoJSON.COORDINATES));
        int n = coordinates.length;
        LineString[] lineStrings = new LineString[n];
        for (int i = 0; i < n; i++) {
            lineStrings[i] = GF.createLineString(coordinates[i]);
        }
        return GF.createMultiLineString(lineStrings);
    }

    public static MultiPolygon toMultiPolygon(Map<String, Object> geometry) {
        List<Object> arrayOfPolygons = GeoJSONUtil.getList(geometry, GeoJSON.COORDINATES);
        int n = arrayOfPolygons.size();
        Polygon[] polygons = new Polygon[n];
        for (int i = 0; i < n; i++) {
            polygons[i] = toPolygon(
                    GeoJSONUtil.getList(arrayOfPolygons, i));
        }
        return GF.createMultiPolygon(polygons);
    }

    public static GeometryCollection toGeometryCollection(Map<String, Object> geometry) {
        List<Object> geometryArray = GeoJSONUtil.getList(geometry, GeoJSON.GEOMETRIES);
        int n = geometryArray.size();
        Geometry[] geometries = new Geometry[n];
        for (int i = 0; i < n; i++) {
            geometries[i] = toGeometry(
                    GeoJSONUtil.getMap(geometryArray, i));
        }
        return GF.createGeometryCollection(geometries);
    }

    private static Coordinate toCoordinate(List<Object> coordinate) {
        return new Coordinate(
                GeoJSONUtil.getDouble(coordinate, 0),
                GeoJSONUtil.getDouble(coordinate, 1));
    }

    private static Coordinate[] toCoordinates(List<Object> arrayOfCoordinates) {
        int n = arrayOfCoordinates.size();
        Coordinate[] coordinates = new Coordinate[n];
        for (int i = 0; i < n; i++) {
            coordinates[i] = toCoordinate(GeoJSONUtil.getList(arrayOfCoordinates, i));
        }
        return coordinates;
    }

    private static Coordinate[][] toCoordinatesArray(List<Object> arrayOfArrayOfCoordinates) {
        int n = arrayOfArrayOfCoordinates.size();
        Coordinate[][] coordinates = new Coordinate[n][];
        for (int i = 0; i < n; i++) {
            coordinates[i] = toCoordinates(GeoJSONUtil.getList(arrayOfArrayOfCoordinates, i));
        }
        return coordinates;
    }

    private static Polygon toPolygon(List<Object> coordinatesArray) {
        Coordinate[][] coordinates = toCoordinatesArray(coordinatesArray);
        LinearRing exterior = GF.createLinearRing(coordinates[0]);
        LinearRing[] interiors = new LinearRing[coordinates.length - 1];
        for (int i = 1; i < coordinates.length; i++) {
            interiors[i - 1] = GF.createLinearRing(coordinates[i]);
        }
        return GF.createPolygon(exterior, interiors);
    }

}
