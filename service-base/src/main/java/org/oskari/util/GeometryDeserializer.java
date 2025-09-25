package org.oskari.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class GeometryDeserializer extends JsonDeserializer<Geometry> {

    private final GeometryFactory gf;

    public GeometryDeserializer() {
        this(new GeometryFactory());
    }

    public GeometryDeserializer(GeometryFactory gf) {
        this.gf = gf;
    }

    @Override
    public Geometry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        if (!p.isExpectedStartObjectToken()) {
            ctxt.handleUnexpectedToken(Geometry.class, p);
        }

        GeometryType type = null;
        Object coordinates = null;
        List<Geometry> geometries = null;

        while (p.nextToken() != JsonToken.END_OBJECT) {
            if (p.currentToken() != JsonToken.FIELD_NAME) {
                continue;
            }
            switch (p.getText().toLowerCase()) {
                case "type":
                    type = parseType(p, ctxt);
                    break;
                case "coordinates":
                    coordinates = parseCoordinates(p, ctxt);
                    break;
                case "geometries":
                    JsonToken t = p.nextToken();
                    if (t == JsonToken.VALUE_NULL) {
                        break;
                    }
                    if (t != JsonToken.START_ARRAY) {
                        ctxt.handleUnexpectedToken(Geometry.class, p);
                    }
                    geometries = new ArrayList<>();
                    while (p.nextToken() != JsonToken.END_ARRAY) {
                        geometries.add(deserialize(p, ctxt));
                    }
                    break;
                default:
                    // Ignore everything else
                    p.nextToken();
                    p.skipChildren();
                    break;
            }
        }

        if (type == null) {
            throw new IllegalArgumentException("Type is required");
        }
        switch (type) {
            case Point:
                return toPoint(coordinates);
            case LineString:
                return toLineString(coordinates);
            case Polygon:
                return toPolygon(coordinates);
            case MultiPoint:
                return toMultiPoint(coordinates);
            case MultiLineString:
                return toMultiLineString(coordinates);
            case MultiPolygon:
                return toMultiPolygon(coordinates);
            case GeometryCollection:
                return gf.createGeometryCollection(geometries.toArray(n -> new Geometry[n]));
            default:
                throw new IllegalArgumentException("Unknown type");
        }
    }

    private enum GeometryType {
        Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon, GeometryCollection
    };

    private static GeometryType parseType(JsonParser p, DeserializationContext ctxt)
            throws IOException, JacksonException {
        JsonToken t = p.nextToken();
        if (t != JsonToken.VALUE_STRING) {
            ctxt.handleUnexpectedToken(Geometry.class, p);
        }
        return GeometryType.valueOf(p.getText());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object parseCoordinates(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        int depth = 0;
        List[] tree = new List[4];

        JsonToken t;
        do {
            t = p.nextToken();
            if (t == JsonToken.START_ARRAY) {
                tree[depth] = new ArrayList<>(8);
                if (depth > 0) {
                    tree[depth - 1].add(tree[depth]);
                }
                depth++;
            } else if (t == JsonToken.END_ARRAY) {
                depth--;
            } else if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
                if (depth == 0) {
                    ctxt.handleUnexpectedToken(Geometry.class, p);
                }
                tree[depth - 1].add(p.getDoubleValue());
            } else {
                ctxt.handleUnexpectedToken(Geometry.class, p);
            }
        } while (depth > 0);

        if (tree[0] == null) {
            ctxt.handleUnexpectedToken(Geometry.class, p);
        }

        return tree[0];
    }

    @SuppressWarnings("unchecked")
    private Geometry toPoint(Object coordinates) {
        List<Double> coords = (List<Double>) coordinates;
        if (coords == null || coords.isEmpty()) {
            return gf.createEmpty(0);
        }
        int d = coords.size();
        CoordinateSequence csq = gf.getCoordinateSequenceFactory().create(1, d);
        setCoordinate(csq, 0, coords, d);
        return gf.createPoint(csq);
    }

    @SuppressWarnings("unchecked")
    private Geometry toLineString(Object coordinates) {
        List<List<Double>> coords = (List<List<Double>>) coordinates;
        if (coords == null || coords.isEmpty()) {
            return gf.createEmpty(1);
        }
        return gf.createLineString(toCoordinateSequence(coords));
    }

    @SuppressWarnings("unchecked")
    private Geometry toPolygon(Object coordinates) {
        List<List<List<Double>>> coords = (List<List<List<Double>>>) coordinates;
        if (coords == null || coords.isEmpty()) {
            return gf.createEmpty(2);
        }
        int n = coords.size();
        if (n == 1) {
            return gf.createPolygon(toCoordinateSequence(coords.get(0)));
        }
        LinearRing exteriorRing = gf.createLinearRing(toCoordinateSequence(coords.get(0)));
        LinearRing[] interiorRings = new LinearRing[n - 1];
        for (int i = 0; i < interiorRings.length; i++) {
            interiorRings[i] = gf.createLinearRing(toCoordinateSequence(coords.get(i + 1)));
        }
        return gf.createPolygon(exteriorRing, interiorRings);
    }

    @SuppressWarnings("rawtypes")
    private Geometry toMultiPoint(Object coordinates) {
        List coords = (List) coordinates;
        if (coords == null || coords.isEmpty()) {
            return gf.createEmpty(-1);
        }
        int n = coords.size();
        Point[] arr = new Point[n];
        for (int i = 0; i < n; i++) {
            arr[i] = (Point) toPoint(coords.get(i));
        }
        return gf.createMultiPoint(arr);
    }

    @SuppressWarnings("rawtypes")
    private Geometry toMultiLineString(Object coordinates) {
        List coords = (List) coordinates;
        if (coords == null || coords.isEmpty()) {
            return gf.createEmpty(-1);
        }
        int n = coords.size();
        LineString[] arr = new LineString[n];
        for (int i = 0; i < n; i++) {
            arr[i] = (LineString) toLineString(coords.get(i));
        }
        return gf.createMultiLineString(arr);
    }

    @SuppressWarnings("rawtypes")
    private Geometry toMultiPolygon(Object coordinates) {
        List coords = (List) coordinates;
        if (coords == null || coords.isEmpty()) {
            return gf.createEmpty(-1);
        }
        int n = coords.size();
        Polygon[] arr = new Polygon[n];
        for (int i = 0; i < n; i++) {
            arr[i] = (Polygon) toPolygon(coords.get(i));
        }
        return gf.createMultiPolygon(arr);
    }

    private CoordinateSequence toCoordinateSequence(List<List<Double>> coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            return null;
        }
        int n = coordinates.size();
        int d = coordinates.stream().mapToInt(List::size).max().getAsInt();
        CoordinateSequence csq = gf.getCoordinateSequenceFactory().create(n, d);
        for (int i = 0; i < n; i++) {
            setCoordinate(csq, i, coordinates.get(i), d);
        }
        return csq;
    }

    private static void setCoordinate(CoordinateSequence csq, int i, List<Double> c, int d) {
        for (int j = 0; j < d; j++) {
            csq.setOrdinate(i, j, j < c.size() ? c.get(j) : 0.0);
        }
    }

}
