package org.oskari.geojson;

import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;

import java.io.IOException;

public class GeoJSONTest {

    @Test
    public void testPoint() throws JSONException, IOException {
        JSONObject json = ResourceHelper.readJSONResource("point.json", this);
        SimpleFeature f = GeoJSONReader.toFeature(json);
        Assertions.assertEquals(2, f.getProperties().size());

        Point geom = (Point) f.getDefaultGeometry();
        Coordinate c = geom.getCoordinate();
        Assertions.assertEquals(125.6, c.x, 1e6);
        Assertions.assertEquals(10.1, c.y, 1e6);

        String name = (String) f.getAttribute("name");
        Assertions.assertEquals("Dinagat Islands", name);

        GeoJSONWriter w = new GeoJSONWriter();
        Assertions.assertTrue(JSONHelper.isEqual(json, w.writeFeature(f)));
    }

    @Test
    public void testLineString() throws IOException, JSONException {
        JSONObject json = ResourceHelper.readJSONResource("linestring.json", this);
        SimpleFeature f = GeoJSONReader.toFeature(json);
        Assertions.assertEquals(2, f.getProperties().size());

        LineString geom = (LineString) f.getDefaultGeometry();
        CoordinateSequence cs = geom.getCoordinateSequence();
        Assertions.assertEquals(2, cs.size());
        Coordinate c = cs.getCoordinate(0);
        Assertions.assertEquals(125.6, c.x, 1e6);
        Assertions.assertEquals(10.1, c.y, 1e6);
        c = cs.getCoordinate(1);
        Assertions.assertEquals(10.1, c.x, 1e6);
        Assertions.assertEquals(125.6, c.y, 1e6);

        String name = (String) f.getAttribute("name");
        Assertions.assertEquals("Dinagat Islands", name);

        GeoJSONWriter w = new GeoJSONWriter();
        Assertions.assertTrue(JSONHelper.isEqual(json, w.writeFeature(f)));
    }

    @Test
    public void testPolygon() throws IOException, JSONException {
        JSONObject json = ResourceHelper.readJSONResource("polygon.json", this);
        SimpleFeature f = GeoJSONReader.toFeature(json);
        Assertions.assertEquals(2, f.getProperties().size());

        Polygon geom = (Polygon) f.getDefaultGeometry();
        CoordinateSequence cs = geom.getExteriorRing().getCoordinateSequence();
        Assertions.assertEquals(5, cs.size());
        Coordinate c = cs.getCoordinate(0);
        Assertions.assertEquals(125.6, c.x, 1e6);
        Assertions.assertEquals(10.1, c.y, 1e6);
        c = cs.getCoordinate(4);
        Assertions.assertEquals(10.1, c.x, 1e6);
        Assertions.assertEquals(125.6, c.y, 1e6);
        Assertions.assertEquals(1, geom.getNumInteriorRing());

        String name = (String) f.getAttribute("name");
        Assertions.assertEquals("Dinagat Islands", name);

        GeoJSONWriter w = new GeoJSONWriter();
        Assertions.assertTrue(JSONHelper.isEqual(json, w.writeFeature(f)));
    }
    
    @Test
    public void testMultiPoint() throws IOException, JSONException {
        JSONObject json = ResourceHelper.readJSONResource("multipoint.json", this);
        MultiPoint geom = (MultiPoint) GeoJSONReader.toGeometry(json);

        Assertions.assertEquals(2, geom.getNumGeometries());
        Point p1 = (Point) geom.getGeometryN(0);
        Point p2 = (Point) geom.getGeometryN(1);
        
        Coordinate c1 = p1.getCoordinate();
        Coordinate c2 = p2.getCoordinate();
        
        Assertions.assertEquals(100.0, c1.x, 1e6);
        Assertions.assertEquals(0.0, c1.y, 1e6);
        
        Assertions.assertEquals(101.0, c2.x, 1e6);
        Assertions.assertEquals(1.0, c2.y, 1e6);
        
        GeoJSONWriter w = new GeoJSONWriter();
        Assertions.assertTrue(JSONHelper.isEqual(json, w.writeGeometry(geom)));
    }

    @Test
    public void testMultiLineString() throws IOException, JSONException {
        JSONObject json = ResourceHelper.readJSONResource("multilinestring.json", this);
        MultiLineString geom = (MultiLineString) GeoJSONReader.toGeometry(json);

        Assertions.assertEquals(2, geom.getNumGeometries());
        LineString ls1 = (LineString) geom.getGeometryN(0);
        LineString ls2 = (LineString) geom.getGeometryN(1);
        
        Assertions.assertEquals(2, ls1.getNumPoints());
        Assertions.assertEquals(2, ls2.getNumPoints());

        CoordinateSequence cs2 = ls2.getCoordinateSequence();
        Coordinate c2 = cs2.getCoordinate(1);
        Assertions.assertEquals(103.0, c2.x, 1e6);
        Assertions.assertEquals(3.0, c2.y, 1e6);
        
        GeoJSONWriter w = new GeoJSONWriter();
        Assertions.assertTrue(JSONHelper.isEqual(json, w.writeGeometry(geom)));
    }
    
    @Test
    public void testMultiPolygon() throws IOException, JSONException {
        JSONObject json = ResourceHelper.readJSONResource("multipolygon.json", this);
        MultiPolygon geom = (MultiPolygon) GeoJSONReader.toGeometry(json);

        Assertions.assertEquals(2, geom.getNumGeometries());
        Polygon p1 = (Polygon) geom.getGeometryN(0);
        Polygon p2 = (Polygon) geom.getGeometryN(1);
        
        Assertions.assertEquals(0, p1.getNumInteriorRing());
        CoordinateSequence cs1 = p1.getExteriorRing().getCoordinateSequence();
        Assertions.assertEquals(5, cs1.size());
        Coordinate c1 = cs1.getCoordinate(0);
        Assertions.assertEquals(102.0, c1.x, 1e6);
        Assertions.assertEquals(2.0, c1.y, 1e6);
        
        Assertions.assertEquals(1, p2.getNumInteriorRing());
        CoordinateSequence cs2 = p2.getInteriorRingN(0).getCoordinateSequence();
        Assertions.assertEquals(5, cs2.size());
        Coordinate c2 = cs2.getCoordinate(3);
        Assertions.assertEquals(100.8, c2.x, 1e6);
        Assertions.assertEquals(0.8, c2.y, 1e6);
        
        GeoJSONWriter w = new GeoJSONWriter();
        Assertions.assertTrue(JSONHelper.isEqual(json, w.writeGeometry(geom)));
    }
    
    @Test
    public void testGeometryCollection() throws IOException, JSONException {
        JSONObject json = ResourceHelper.readJSONResource("geometrycollection.json", this);
        GeometryCollection geom = (GeometryCollection) GeoJSONReader.toGeometry(json);

        Assertions.assertEquals(2, geom.getNumGeometries());
        
        Point p = (Point) geom.getGeometryN(0);
        Coordinate c = p.getCoordinate();
        Assertions.assertEquals(100.0, c.x, 1e6);
        Assertions.assertEquals(0.0, c.y, 1e6);
        
        LineString ls = (LineString) geom.getGeometryN(1);
        CoordinateSequence cs = ls.getCoordinateSequence();
        Assertions.assertEquals(2, cs.size());
        c = cs.getCoordinate(0);
        Assertions.assertEquals(101.0, c.x, 1e6);
        Assertions.assertEquals(0.0, c.y, 1e6);
        c = cs.getCoordinate(1);
        Assertions.assertEquals(102.0, c.x, 1e6);
        Assertions.assertEquals(1.0, c.y, 1e6);
        
        GeoJSONWriter w = new GeoJSONWriter();
        Assertions.assertTrue(JSONHelper.isEqual(json, w.writeGeometry(geom)));
    }

}
