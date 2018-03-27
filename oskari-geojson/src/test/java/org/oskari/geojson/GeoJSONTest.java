package org.oskari.geojson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.ResourceHelper;

public class GeoJSONTest {

    @Test
    public void testPoint() throws JSONException, IOException {
        JSONObject json = ResourceHelper.readJSONResource("point.json", this);
        SimpleFeature f = GeoJSONReader.toFeature(json);
        assertEquals(2, f.getProperties().size());

        Point geom = (Point) f.getDefaultGeometry();
        Coordinate c = geom.getCoordinate();
        assertEquals(125.6, c.x, 1e6);
        assertEquals(10.1, c.y, 1e6);

        String name = (String) f.getAttribute("name");
        assertEquals("Dinagat Islands", name);

        GeoJSONWriter w = new GeoJSONWriter();
        assertTrue(JSONHelper.isEqual(json, w.writeFeature(f)));
    }

    @Test
    public void testLineString() throws IOException, JSONException {
        JSONObject json = ResourceHelper.readJSONResource("linestring.json", this);
        SimpleFeature f = GeoJSONReader.toFeature(json);
        assertEquals(2, f.getProperties().size());

        LineString geom = (LineString) f.getDefaultGeometry();
        CoordinateSequence cs = geom.getCoordinateSequence();
        assertEquals(2, cs.size());
        Coordinate c = cs.getCoordinate(0);
        assertEquals(125.6, c.x, 1e6);
        assertEquals(10.1, c.y, 1e6);
        c = cs.getCoordinate(1);
        assertEquals(10.1, c.x, 1e6);
        assertEquals(125.6, c.y, 1e6);

        String name = (String) f.getAttribute("name");
        assertEquals("Dinagat Islands", name);

        GeoJSONWriter w = new GeoJSONWriter();
        assertTrue(JSONHelper.isEqual(json, w.writeFeature(f)));
    }

    @Test
    public void testPolygon() throws IOException, JSONException {
        JSONObject json = ResourceHelper.readJSONResource("polygon.json", this);
        SimpleFeature f = GeoJSONReader.toFeature(json);
        assertEquals(2, f.getProperties().size());

        Polygon geom = (Polygon) f.getDefaultGeometry();
        CoordinateSequence cs = geom.getExteriorRing().getCoordinateSequence();
        assertEquals(5, cs.size());
        Coordinate c = cs.getCoordinate(0);
        assertEquals(125.6, c.x, 1e6);
        assertEquals(10.1, c.y, 1e6);
        c = cs.getCoordinate(4);
        assertEquals(10.1, c.x, 1e6);
        assertEquals(125.6, c.y, 1e6);
        assertEquals(1, geom.getNumInteriorRing());

        String name = (String) f.getAttribute("name");
        assertEquals("Dinagat Islands", name);

        GeoJSONWriter w = new GeoJSONWriter();
        assertTrue(JSONHelper.isEqual(json, w.writeFeature(f)));
    }
    
    @Test
    public void testMultiPoint() throws IOException, JSONException {
        JSONObject json = ResourceHelper.readJSONResource("multipoint.json", this);
        MultiPoint geom = (MultiPoint) GeoJSONReader.toGeometry(json);

        assertEquals(2, geom.getNumGeometries());
        Point p1 = (Point) geom.getGeometryN(0);
        Point p2 = (Point) geom.getGeometryN(1);
        
        Coordinate c1 = p1.getCoordinate();
        Coordinate c2 = p2.getCoordinate();
        
        assertEquals(100.0, c1.x, 1e6);
        assertEquals(0.0, c1.y, 1e6);
        
        assertEquals(101.0, c2.x, 1e6);
        assertEquals(1.0, c2.y, 1e6);
        
        GeoJSONWriter w = new GeoJSONWriter();
        assertTrue(JSONHelper.isEqual(json, w.writeGeometry(geom)));
    }

    @Test
    public void testMultiLineString() throws IOException, JSONException {
        JSONObject json = ResourceHelper.readJSONResource("multilinestring.json", this);
        MultiLineString geom = (MultiLineString) GeoJSONReader.toGeometry(json);

        assertEquals(2, geom.getNumGeometries());
        LineString ls1 = (LineString) geom.getGeometryN(0);
        LineString ls2 = (LineString) geom.getGeometryN(1);
        
        assertEquals(2, ls1.getNumPoints());
        assertEquals(2, ls2.getNumPoints());

        CoordinateSequence cs2 = ls2.getCoordinateSequence();
        Coordinate c2 = cs2.getCoordinate(1);
        assertEquals(103.0, c2.x, 1e6);
        assertEquals(3.0, c2.y, 1e6);
        
        GeoJSONWriter w = new GeoJSONWriter();
        assertTrue(JSONHelper.isEqual(json, w.writeGeometry(geom)));
    }
    
    @Test
    public void testMultiPolygon() throws IOException, JSONException {
        JSONObject json = ResourceHelper.readJSONResource("multipolygon.json", this);
        MultiPolygon geom = (MultiPolygon) GeoJSONReader.toGeometry(json);

        assertEquals(2, geom.getNumGeometries());
        Polygon p1 = (Polygon) geom.getGeometryN(0);
        Polygon p2 = (Polygon) geom.getGeometryN(1);
        
        assertEquals(0, p1.getNumInteriorRing());
        CoordinateSequence cs1 = p1.getExteriorRing().getCoordinateSequence();
        assertEquals(5, cs1.size());
        Coordinate c1 = cs1.getCoordinate(0);
        assertEquals(102.0, c1.x, 1e6);
        assertEquals(2.0, c1.y, 1e6);
        
        assertEquals(1, p2.getNumInteriorRing());
        CoordinateSequence cs2 = p2.getInteriorRingN(0).getCoordinateSequence();
        assertEquals(5, cs2.size());
        Coordinate c2 = cs2.getCoordinate(3);
        assertEquals(100.8, c2.x, 1e6);
        assertEquals(0.8, c2.y, 1e6);
        
        GeoJSONWriter w = new GeoJSONWriter();
        assertTrue(JSONHelper.isEqual(json, w.writeGeometry(geom)));
    }
    
    @Test
    public void testGeometryCollection() throws IOException, JSONException {
        JSONObject json = ResourceHelper.readJSONResource("geometrycollection.json", this);
        GeometryCollection geom = (GeometryCollection) GeoJSONReader.toGeometry(json);

        assertEquals(2, geom.getNumGeometries());
        
        Point p = (Point) geom.getGeometryN(0);
        Coordinate c = p.getCoordinate();
        assertEquals(100.0, c.x, 1e6);
        assertEquals(0.0, c.y, 1e6);
        
        LineString ls = (LineString) geom.getGeometryN(1);
        CoordinateSequence cs = ls.getCoordinateSequence();
        assertEquals(2, cs.size());
        c = cs.getCoordinate(0);
        assertEquals(101.0, c.x, 1e6);
        assertEquals(0.0, c.y, 1e6);
        c = cs.getCoordinate(1);
        assertEquals(102.0, c.x, 1e6);
        assertEquals(1.0, c.y, 1e6);
        
        GeoJSONWriter w = new GeoJSONWriter();
        assertTrue(JSONHelper.isEqual(json, w.writeGeometry(geom)));
    }

}
