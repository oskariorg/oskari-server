package org.oskari.util;

import java.io.IOException;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class GeometrySerializer extends JsonSerializer<Geometry> {

    private static final SerializableString TYPE = new SerializedString("type");
    private static final SerializableString COORDINATES = new SerializedString("coordinates");
    private static final SerializableString GEOMETRIES = new SerializedString("geometries");
    private static final SerializableString POINT = new SerializedString("Point");
    private static final SerializableString LINESTRING = new SerializedString("LineString");
    private static final SerializableString POLYGON = new SerializedString("Polygon");
    private static final SerializableString MULTIPOINT = new SerializedString("MultiPoint");
    private static final SerializableString MULTILINESTRING = new SerializedString("MultiLineString");
    private static final SerializableString MULTIPOLYGON = new SerializedString("MultiPolygon");
    private static final SerializableString GEOMETRYCOLLECTION = new SerializedString("GeometryCollection");

    @Override
    public void serialize(Geometry value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value instanceof Point p) {
            serializePoint(p, gen);
        } else if (value instanceof LineString ls) {
            serializeLineString(ls, gen);
        } else if (value instanceof Polygon po) {
            serializePolygon(po, gen);
        } else if (value instanceof MultiPoint mp) {
            serializeMultiPoint(mp, gen);
        } else if (value instanceof MultiLineString mls) {
            serializeMultiLineString(mls, gen);
        } else if (value instanceof MultiPolygon mpo) {
            serializeMultiPolygon(mpo, gen);
        } else if (value instanceof GeometryCollection c) {
            serializeGeometryCollection(c, gen, serializers);
        } else {
            throw new UnsupportedOperationException("Unknown type");
        }
    }

    private void serializeGeometryCollection(GeometryCollection c, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartObject();
        gen.writeFieldName(TYPE);
        gen.writeString(GEOMETRYCOLLECTION);
        gen.writeFieldName(GEOMETRIES);
        gen.writeStartArray();
        for (int i = 0; i < c.getNumGeometries(); i++) {
            serialize(c.getGeometryN(i), gen, serializers);
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

    private static void serializePoint(Point p, JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName(TYPE);
        gen.writeString(POINT);
        gen.writeFieldName(COORDINATES);
        serializeCoordinate(p.getCoordinateSequence(), 0, gen);
        gen.writeEndObject();
    }

    private static void serializeLineString(LineString ls, JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName(TYPE);
        gen.writeString(LINESTRING);
        gen.writeFieldName(COORDINATES);
        serializeCoordinateSequence(ls.getCoordinateSequence(), gen);
        gen.writeEndObject();
    }

    private static void serializePolygon(Polygon po, JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName(TYPE);
        gen.writeString(POLYGON);
        gen.writeFieldName(COORDINATES);
        gen.writeStartArray();
        serializeCoordinateSequence(po.getExteriorRing().getCoordinateSequence(), gen);
        for (int ringIdx = 0; ringIdx < po.getNumInteriorRing(); ringIdx++) {
            serializeCoordinateSequence(po.getInteriorRingN(ringIdx).getCoordinateSequence(), gen);
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

    private static void serializeMultiPoint(MultiPoint c, JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName(TYPE);
        gen.writeString(MULTIPOINT);
        gen.writeFieldName(COORDINATES);
        gen.writeStartArray();
        for (int geomIdx = 0; geomIdx < c.getNumGeometries(); geomIdx++) {
            Point g = (Point) c.getGeometryN(geomIdx);
            serializeCoordinate(g.getCoordinateSequence(), 0, gen);
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

    private static void serializeMultiLineString(MultiLineString c, JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName(TYPE);
        gen.writeString(MULTILINESTRING);
        gen.writeFieldName(COORDINATES);
        gen.writeStartArray();
        for (int geomIdx = 0; geomIdx < c.getNumGeometries(); geomIdx++) {
            LineString g = (LineString) c.getGeometryN(geomIdx);
            serializeCoordinateSequence(g.getCoordinateSequence(), gen);
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

    private static void serializeMultiPolygon(MultiPolygon c, JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName(TYPE);
        gen.writeString(MULTIPOLYGON);
        gen.writeFieldName(COORDINATES);
        gen.writeStartArray();
        for (int geomIdx = 0; geomIdx < c.getNumGeometries(); geomIdx++) {
            Polygon g = (Polygon) c.getGeometryN(geomIdx);
            gen.writeStartArray();
            serializeCoordinateSequence(g.getExteriorRing().getCoordinateSequence(), gen);
            for (int ringIdx = 0; ringIdx < g.getNumInteriorRing(); ringIdx++) {
                serializeCoordinateSequence(g.getInteriorRingN(ringIdx).getCoordinateSequence(), gen);
            }
            gen.writeEndArray();
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

    private static void serializeCoordinateSequence(CoordinateSequence seq, JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        for (int i = 0; i < seq.size(); i++) {
            serializeCoordinate(seq, i, gen);
        }
        gen.writeEndArray();
    }

    private static void serializeCoordinate(CoordinateSequence seq, int idx, JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        for (int ordinateIdx = 0; ordinateIdx < seq.getDimension(); ordinateIdx++) {
            gen.writeNumber(seq.getOrdinate(idx, ordinateIdx));
        }
        gen.writeEndArray();
    }

}
