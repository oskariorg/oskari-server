package org.oskari.geojson;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import fi.nls.test.util.ResourceHelper;

public class GeoJSONReader2Test {

    private Map<String, Object> loadJSONResource(String res) throws Exception {
        String s = ResourceHelper.readStringResource(res, this);
        ObjectMapper om = new ObjectMapper();
        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String,Object>>() {};
        return om.readValue(s, typeRef);
    }

    @Test
    public void testPointFeature() throws Exception {
        Map<String, Object> json = loadJSONResource("point.json");
        CoordinateReferenceSystem crs84 = CRS.decode("EPSG:4326", true);
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(json, crs84);

        SimpleFeature f = GeoJSONReader2.toFeature(json, schema);
        assertEquals(2, f.getProperties().size());

        Point geom = (Point) f.getDefaultGeometry();
        Coordinate c = geom.getCoordinate();
        assertEquals(125.6, c.x, 1e6);
        assertEquals(10.1, c.y, 1e6);

        String name = (String) f.getAttribute("name");
        assertEquals("Dinagat Islands", name);
    }

    @Test
    public void testLineString() throws Exception {
        Map<String, Object> json = loadJSONResource("linestring.json");
        CoordinateReferenceSystem crs84 = CRS.decode("EPSG:4326", true);
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(json, crs84);
        SimpleFeature f = GeoJSONReader2.toFeature(json, schema);
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
    }

    @Test
    public void testPolygon() throws Exception {
        Map<String, Object> json = loadJSONResource("polygon.json");
        CoordinateReferenceSystem crs84 = CRS.decode("EPSG:4326", true);
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(json, crs84);
        SimpleFeature f = GeoJSONReader2.toFeature(json, schema);
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
    }

    @Test
    public void testMultiPoint() throws Exception {
        Map<String, Object> json = loadJSONResource("multipoint.json");
        MultiPoint geom = (MultiPoint) GeoJSONReader2.toGeometry(json);

        assertEquals(2, geom.getNumGeometries());
        Point p1 = (Point) geom.getGeometryN(0);
        Point p2 = (Point) geom.getGeometryN(1);

        Coordinate c1 = p1.getCoordinate();
        Coordinate c2 = p2.getCoordinate();

        assertEquals(100.0, c1.x, 1e6);
        assertEquals(0.0, c1.y, 1e6);

        assertEquals(101.0, c2.x, 1e6);
        assertEquals(1.0, c2.y, 1e6);
    }

    @Test
    public void testMultiLineString() throws Exception {
        Map<String, Object> json = loadJSONResource("multilinestring.json");
        MultiLineString geom = (MultiLineString) GeoJSONReader2.toGeometry(json);

        assertEquals(2, geom.getNumGeometries());
        LineString ls1 = (LineString) geom.getGeometryN(0);
        LineString ls2 = (LineString) geom.getGeometryN(1);

        assertEquals(2, ls1.getNumPoints());
        assertEquals(2, ls2.getNumPoints());

        CoordinateSequence cs2 = ls2.getCoordinateSequence();
        Coordinate c2 = cs2.getCoordinate(1);
        assertEquals(103.0, c2.x, 1e6);
        assertEquals(3.0, c2.y, 1e6);
    }

    @Test
    public void testMultiPolygon() throws Exception {
        Map<String, Object> json = loadJSONResource("multipolygon.json");
        MultiPolygon geom = (MultiPolygon) GeoJSONReader2.toGeometry(json);

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
    }

    @Test
    public void testGeometryCollection() throws Exception {
        Map<String, Object> json = loadJSONResource("geometrycollection.json");
        GeometryCollection geom = (GeometryCollection) GeoJSONReader2.toGeometry(json);

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
    }

}
