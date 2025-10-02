package org.oskari.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class GeometryDeserializerTest {

    private static GeometryFactory gf;
    private static ObjectMapper om;

    @BeforeAll
    public static void init() {
        gf = new GeometryFactory();
        om = new ObjectMapper();
        SimpleModule jtsModule = new SimpleModule();
        jtsModule.addDeserializer(Geometry.class, new GeometryDeserializer(gf));
        jtsModule.addSerializer(Geometry.class, new GeometrySerializer());
        om.registerModule(jtsModule);
    }

    @Test
    public void testPoint() throws JsonMappingException, JsonProcessingException {
        String s = "{'type': 'Point','coordinates': [125.6, 10.1]}".replace('\'', '"');
        Geometry g = om.readValue(s, Geometry.class);
        Assertions.assertTrue(g instanceof Point);
        Point p = (Point) g;
        Assertions.assertEquals(125.6, p.getX(), 1e-10);
        Assertions.assertEquals(10.1, p.getY(), 1e-10);

        Geometry roundTrip = om.readValue(om.writeValueAsString(g), Geometry.class);
        Assertions.assertEquals(g, roundTrip);
    }

    @Test
    public void testLineString() throws JsonMappingException, JsonProcessingException {
        String s = "{'type': 'LineString', 'coordinates': [ [100.0, 0.0], [101.0, 1.0] ]}".replace('\'', '"');
        Geometry g = om.readValue(s, Geometry.class);
        Assertions.assertTrue(g instanceof LineString);
        LineString ls = (LineString) g;
        Assertions.assertEquals(2, ls.getNumPoints());
        Assertions.assertEquals(100.0, ls.getCoordinateN(0).getX(), 1e-10);
        Assertions.assertEquals(0.0, ls.getCoordinateN(0).getY(), 1e-10);
        Assertions.assertEquals(101.0, ls.getCoordinateN(1).getX(), 1e-10);
        Assertions.assertEquals(1.0, ls.getCoordinateN(1).getY(), 1e-10);

        Geometry roundTrip = om.readValue(om.writeValueAsString(g), Geometry.class);
        Assertions.assertEquals(g, roundTrip);
    }

    @Test
    public void testPolygonNoHoles() throws JsonMappingException, JsonProcessingException {
        String s = "{'type':'Polygon','coordinates':[[[100.0,0.0],[101.0,0.0],[101.0,1.0],[100.0,1.0],[100.0,0.0]]]}"
                .replace('\'', '"');
        Geometry g = om.readValue(s, Geometry.class);
        Assertions.assertTrue(g instanceof Polygon);
        Polygon p = (Polygon) g;
        Assertions.assertEquals(0, p.getNumInteriorRing());
        Assertions.assertEquals(5, p.getExteriorRing().getNumPoints());
        Assertions.assertEquals(100.0, p.getExteriorRing().getCoordinateN(0).getX(), 1e-10);
        Assertions.assertEquals(0.0, p.getExteriorRing().getCoordinateN(0).getY(), 1e-10);
        Assertions.assertEquals(101.0, p.getExteriorRing().getCoordinateN(1).getX(), 1e-10);
        Assertions.assertEquals(0.0, p.getExteriorRing().getCoordinateN(1).getY(), 1e-10);
        Assertions.assertEquals(101.0, p.getExteriorRing().getCoordinateN(2).getX(), 1e-10);
        Assertions.assertEquals(1.0, p.getExteriorRing().getCoordinateN(2).getY(), 1e-10);
        Assertions.assertEquals(100.0, p.getExteriorRing().getCoordinateN(3).getX(), 1e-10);
        Assertions.assertEquals(1.0, p.getExteriorRing().getCoordinateN(3).getY(), 1e-10);
        Assertions.assertEquals(100.0, p.getExteriorRing().getCoordinateN(4).getX(), 1e-10);
        Assertions.assertEquals(0.0, p.getExteriorRing().getCoordinateN(4).getY(), 1e-10);

        Geometry roundTrip = om.readValue(om.writeValueAsString(g), Geometry.class);
        Assertions.assertEquals(g, roundTrip);
    }

    @Test
    public void testPolygonWithHoles() throws JsonMappingException, JsonProcessingException {
        String s = "{'type':'Polygon','coordinates':[[[100.0,0.0],[101.0,0.0],[101.0,1.0],[100.0,1.0],[100.0,0.0]],[[100.8,0.8],[100.8,0.2],[100.2,0.2],[100.2,0.8],[100.8,0.8]]]}"
                .replace('\'', '"');
        Geometry g = om.readValue(s, Geometry.class);
        Assertions.assertTrue(g instanceof Polygon);
        Polygon p = (Polygon) g;

        Assertions.assertEquals(1, p.getNumInteriorRing());

        Assertions.assertEquals(5, p.getExteriorRing().getNumPoints());
        Assertions.assertEquals(100.0, p.getExteriorRing().getCoordinateN(0).getX(), 1e-10);
        Assertions.assertEquals(0.0, p.getExteriorRing().getCoordinateN(0).getY(), 1e-10);
        Assertions.assertEquals(101.0, p.getExteriorRing().getCoordinateN(1).getX(), 1e-10);
        Assertions.assertEquals(0.0, p.getExteriorRing().getCoordinateN(1).getY(), 1e-10);
        Assertions.assertEquals(101.0, p.getExteriorRing().getCoordinateN(2).getX(), 1e-10);
        Assertions.assertEquals(1.0, p.getExteriorRing().getCoordinateN(2).getY(), 1e-10);
        Assertions.assertEquals(100.0, p.getExteriorRing().getCoordinateN(3).getX(), 1e-10);
        Assertions.assertEquals(1.0, p.getExteriorRing().getCoordinateN(3).getY(), 1e-10);
        Assertions.assertEquals(100.0, p.getExteriorRing().getCoordinateN(4).getX(), 1e-10);
        Assertions.assertEquals(0.0, p.getExteriorRing().getCoordinateN(4).getY(), 1e-10);

        Assertions.assertEquals(5, p.getInteriorRingN(0).getNumPoints());
        Assertions.assertEquals(100.8, p.getInteriorRingN(0).getCoordinateN(0).getX(), 1e-10);
        Assertions.assertEquals(0.8, p.getInteriorRingN(0).getCoordinateN(0).getY(), 1e-10);
        Assertions.assertEquals(100.8, p.getInteriorRingN(0).getCoordinateN(1).getX(), 1e-10);
        Assertions.assertEquals(0.2, p.getInteriorRingN(0).getCoordinateN(1).getY(), 1e-10);
        Assertions.assertEquals(100.2, p.getInteriorRingN(0).getCoordinateN(2).getX(), 1e-10);
        Assertions.assertEquals(0.2, p.getInteriorRingN(0).getCoordinateN(2).getY(), 1e-10);
        Assertions.assertEquals(100.2, p.getInteriorRingN(0).getCoordinateN(3).getX(), 1e-10);
        Assertions.assertEquals(0.8, p.getInteriorRingN(0).getCoordinateN(3).getY(), 1e-10);
        Assertions.assertEquals(100.8, p.getInteriorRingN(0).getCoordinateN(4).getX(), 1e-10);
        Assertions.assertEquals(0.8, p.getInteriorRingN(0).getCoordinateN(4).getY(), 1e-10);

        Geometry roundTrip = om.readValue(om.writeValueAsString(g), Geometry.class);
        Assertions.assertEquals(g, roundTrip);
    }

    @Test
    public void testMultiPoint() throws JsonMappingException, JsonProcessingException {
        String s = "{'type':'MultiPoint','coordinates':[[100.0,0.0],[101.0,1.0]]}".replace('\'', '"');
        Geometry g = om.readValue(s, Geometry.class);
        Assertions.assertTrue(g instanceof MultiPoint);
        MultiPoint p = (MultiPoint) g;
        Assertions.assertEquals(2, p.getNumGeometries());
        Assertions.assertEquals(100.0, p.getCoordinates()[0].getX(), 1e-10);
        Assertions.assertEquals(0.0, p.getCoordinates()[0].getY(), 1e-10);
        Assertions.assertEquals(101.0, p.getCoordinates()[1].getX(), 1e-10);
        Assertions.assertEquals(1.0, p.getCoordinates()[1].getY(), 1e-10);

        Geometry roundTrip = om.readValue(om.writeValueAsString(g), Geometry.class);
        Assertions.assertEquals(g, roundTrip);
    }

    @Test
    public void testMultiLineString() throws JsonMappingException, JsonProcessingException {
        String s = "{'type':'MultiLineString','coordinates':[[[100.0,0.0],[101.0,1.0]],[[102.0,2.0],[103.0,3.0]]]}"
                .replace('\'', '"');
        Geometry g = om.readValue(s, Geometry.class);
        Assertions.assertTrue(g instanceof MultiLineString);
        MultiLineString p = (MultiLineString) g;

        Assertions.assertEquals(2, p.getNumGeometries());

        Assertions.assertEquals(100.0, p.getGeometryN(0).getCoordinates()[0].getX(), 1e-10);
        Assertions.assertEquals(0.0, p.getGeometryN(0).getCoordinates()[0].getY(), 1e-10);
        Assertions.assertEquals(101.0, p.getGeometryN(0).getCoordinates()[1].getX(), 1e-10);
        Assertions.assertEquals(1.0, p.getGeometryN(0).getCoordinates()[1].getY(), 1e-10);

        Assertions.assertEquals(102.0, p.getGeometryN(1).getCoordinates()[0].getX(), 1e-10);
        Assertions.assertEquals(2.0, p.getGeometryN(1).getCoordinates()[0].getY(), 1e-10);
        Assertions.assertEquals(103.0, p.getGeometryN(1).getCoordinates()[1].getX(), 1e-10);
        Assertions.assertEquals(3.0, p.getGeometryN(1).getCoordinates()[1].getY(), 1e-10);

        Geometry roundTrip = om.readValue(om.writeValueAsString(g), Geometry.class);
        Assertions.assertEquals(g, roundTrip);
    }

    @Test
    public void testMultiPolygon() throws JsonMappingException, JsonProcessingException {
        String s = "{'type':'MultiPolygon','coordinates':[[[[102.0,2.0],[103.0,2.0],[103.0,3.0],[102.0,3.0],[102.0,2.0]]],[[[100.0,0.0],[101.0,0.0],[101.0,1.0],[100.0,1.0],[100.0,0.0]],[[100.2,0.2],[100.2,0.8],[100.8,0.8],[100.8,0.2],[100.2,0.2]]]]}"
                .replace('\'', '"');
        Geometry g = om.readValue(s, Geometry.class);
        Assertions.assertTrue(g instanceof MultiPolygon);
        MultiPolygon p = (MultiPolygon) g;

        Assertions.assertEquals(2, p.getNumGeometries());

        Polygon p1 = (Polygon) p.getGeometryN(0);

        Assertions.assertEquals(5, p1.getExteriorRing().getNumPoints());
        Assertions.assertEquals(102.0, p1.getExteriorRing().getCoordinateN(0).getX(), 1e-10);
        Assertions.assertEquals(2.0, p1.getExteriorRing().getCoordinateN(0).getY(), 1e-10);
        Assertions.assertEquals(103.0, p1.getExteriorRing().getCoordinateN(1).getX(), 1e-10);
        Assertions.assertEquals(2.0, p1.getExteriorRing().getCoordinateN(1).getY(), 1e-10);
        Assertions.assertEquals(103.0, p1.getExteriorRing().getCoordinateN(2).getX(), 1e-10);
        Assertions.assertEquals(3.0, p1.getExteriorRing().getCoordinateN(2).getY(), 1e-10);
        Assertions.assertEquals(102.0, p1.getExteriorRing().getCoordinateN(3).getX(), 1e-10);
        Assertions.assertEquals(3.0, p1.getExteriorRing().getCoordinateN(3).getY(), 1e-10);
        Assertions.assertEquals(102.0, p1.getExteriorRing().getCoordinateN(4).getX(), 1e-10);
        Assertions.assertEquals(2.0, p1.getExteriorRing().getCoordinateN(4).getY(), 1e-10);

        Assertions.assertEquals(0, p1.getNumInteriorRing());

        Polygon p2 = (Polygon) p.getGeometryN(1);

        Assertions.assertEquals(5, p2.getExteriorRing().getNumPoints());
        Assertions.assertEquals(100.0, p2.getExteriorRing().getCoordinateN(0).getX(), 1e-10);
        Assertions.assertEquals(0.0, p2.getExteriorRing().getCoordinateN(0).getY(), 1e-10);
        Assertions.assertEquals(101.0, p2.getExteriorRing().getCoordinateN(1).getX(), 1e-10);
        Assertions.assertEquals(0.0, p2.getExteriorRing().getCoordinateN(1).getY(), 1e-10);
        Assertions.assertEquals(101.0, p2.getExteriorRing().getCoordinateN(2).getX(), 1e-10);
        Assertions.assertEquals(1.0, p2.getExteriorRing().getCoordinateN(2).getY(), 1e-10);
        Assertions.assertEquals(100.0, p2.getExteriorRing().getCoordinateN(3).getX(), 1e-10);
        Assertions.assertEquals(1.0, p2.getExteriorRing().getCoordinateN(3).getY(), 1e-10);
        Assertions.assertEquals(100.0, p2.getExteriorRing().getCoordinateN(4).getX(), 1e-10);
        Assertions.assertEquals(0.0, p2.getExteriorRing().getCoordinateN(4).getY(), 1e-10);

        Assertions.assertEquals(1, p2.getNumInteriorRing());

        Assertions.assertEquals(5, p2.getInteriorRingN(0).getNumPoints());
        Assertions.assertEquals(100.2, p2.getInteriorRingN(0).getCoordinateN(0).getX(), 1e-10);
        Assertions.assertEquals(0.2, p2.getInteriorRingN(0).getCoordinateN(0).getY(), 1e-10);
        Assertions.assertEquals(100.2, p2.getInteriorRingN(0).getCoordinateN(1).getX(), 1e-10);
        Assertions.assertEquals(0.8, p2.getInteriorRingN(0).getCoordinateN(1).getY(), 1e-10);
        Assertions.assertEquals(100.8, p2.getInteriorRingN(0).getCoordinateN(2).getX(), 1e-10);
        Assertions.assertEquals(0.8, p2.getInteriorRingN(0).getCoordinateN(2).getY(), 1e-10);
        Assertions.assertEquals(100.8, p2.getInteriorRingN(0).getCoordinateN(3).getX(), 1e-10);
        Assertions.assertEquals(0.2, p2.getInteriorRingN(0).getCoordinateN(3).getY(), 1e-10);
        Assertions.assertEquals(100.2, p2.getInteriorRingN(0).getCoordinateN(4).getX(), 1e-10);
        Assertions.assertEquals(0.2, p2.getInteriorRingN(0).getCoordinateN(4).getY(), 1e-10);

        String es = om.writeValueAsString(g);
        Geometry roundTrip = om.readValue(om.writeValueAsString(g), Geometry.class);
        Assertions.assertEquals(g, roundTrip);
    }

    @Test
    public void testGeometryCollection() throws JsonMappingException, JsonProcessingException {
        String s = "{'type':'GeometryCollection','geometries':[{'type':'Point','coordinates':[100.0,0.0]},{'type':'LineString','coordinates':[[101.0,0.0],[102.0,1.0]]}]}"
                .replace('\'', '"');
        Geometry g = om.readValue(s, Geometry.class);
        Assertions.assertTrue(g instanceof GeometryCollection);
        Assertions.assertTrue(g.getClass() == GeometryCollection.class);
        GeometryCollection collection = (GeometryCollection) g;

        Assertions.assertEquals(2, collection.getNumGeometries());

        Point p = (Point) collection.getGeometryN(0);
        Assertions.assertEquals(100.0, p.getX(), 1e-10);
        Assertions.assertEquals(0.0, p.getY(), 1e-10);

        LineString ls = (LineString) collection.getGeometryN(1);

        Assertions.assertEquals(2, ls.getNumPoints());
        Assertions.assertEquals(101.0, ls.getCoordinateN(0).getX(), 1e-10);
        Assertions.assertEquals(0.0, ls.getCoordinateN(0).getY(), 1e-10);
        Assertions.assertEquals(102.0, ls.getCoordinateN(1).getX(), 1e-10);
        Assertions.assertEquals(1.0, ls.getCoordinateN(1).getY(), 1e-10);

        Geometry roundTrip = om.readValue(om.writeValueAsString(g), Geometry.class);
        Assertions.assertEquals(g, roundTrip);
    }

    @Test
    public void testEmptyGeometry() throws JsonMappingException, JsonProcessingException {
        String s = "{'type': 'LineString', 'coordinates': []}".replace('\'', '"');
        Geometry g = om.readValue(s, Geometry.class);
        Assertions.assertTrue(g.isEmpty());
    }

    @Test
    public void moreEsoteric() throws JsonMappingException, JsonProcessingException {
        String s = "{'geometries': null, 'coordinates': [ [99999999999, -0], [9876543210.000000001, 1, 5.0] ], 'type': 'LineString'}".replace('\'', '"');
        Geometry g = om.readValue(s, Geometry.class);

        Assertions.assertTrue(g instanceof LineString);
        LineString ls = (LineString) g;
        Assertions.assertEquals(2, ls.getNumPoints());

        Assertions.assertEquals(99999999999L, ls.getCoordinateN(0).getX(), 1e-10);
        Assertions.assertEquals(-0.0, ls.getCoordinateN(0).getY(), 1e-10);
        // Mimic PostGIS behaviour on missing z's being converted to 0
        Assertions.assertEquals(0.0, ls.getCoordinateN(0).getZ(), 1e-10);

        Assertions.assertEquals(9876543210.000000001, ls.getCoordinateN(1).getX(), 1e-10);
        Assertions.assertEquals(1.0, ls.getCoordinateN(1).getY(), 1e-10);
        Assertions.assertEquals(5.0, ls.getCoordinateN(1).getZ(), 1e-10);

        Geometry roundTrip = om.readValue(om.writeValueAsString(g), Geometry.class);
        Assertions.assertEquals(g, roundTrip);
    }

}
