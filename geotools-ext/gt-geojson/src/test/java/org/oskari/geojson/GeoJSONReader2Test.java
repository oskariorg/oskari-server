package org.oskari.geojson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;

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
        Assertions.assertEquals(2, f.getProperties().size());

        Point geom = (Point) f.getDefaultGeometry();
        Coordinate c = geom.getCoordinate();
        Assertions.assertEquals(125.6, c.x, 1e6);
        Assertions.assertEquals(10.1, c.y, 1e6);

        String name = (String) f.getAttribute("name");
        Assertions.assertEquals("Dinagat Islands", name);
    }

    @Test
    public void testLineString() throws Exception {
        Map<String, Object> json = loadJSONResource("linestring.json");
        CoordinateReferenceSystem crs84 = CRS.decode("EPSG:4326", true);
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(json, crs84);
        SimpleFeature f = GeoJSONReader2.toFeature(json, schema);
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
    }

    @Test
    public void testPolygon() throws Exception {
        Map<String, Object> json = loadJSONResource("polygon.json");
        CoordinateReferenceSystem crs84 = CRS.decode("EPSG:4326", true);
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(json, crs84);
        SimpleFeature f = GeoJSONReader2.toFeature(json, schema);
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
    }

    @Test
    public void testMultiPoint() throws Exception {
        Map<String, Object> json = loadJSONResource("multipoint.json");
        MultiPoint geom = (MultiPoint) GeoJSONReader2.toGeometry(json);

        Assertions.assertEquals(2, geom.getNumGeometries());
        Point p1 = (Point) geom.getGeometryN(0);
        Point p2 = (Point) geom.getGeometryN(1);

        Coordinate c1 = p1.getCoordinate();
        Coordinate c2 = p2.getCoordinate();

        Assertions.assertEquals(100.0, c1.x, 1e6);
        Assertions.assertEquals(0.0, c1.y, 1e6);

        Assertions.assertEquals(101.0, c2.x, 1e6);
        Assertions.assertEquals(1.0, c2.y, 1e6);
    }

    @Test
    public void testMultiLineString() throws Exception {
        Map<String, Object> json = loadJSONResource("multilinestring.json");
        MultiLineString geom = (MultiLineString) GeoJSONReader2.toGeometry(json);

        Assertions.assertEquals(2, geom.getNumGeometries());
        LineString ls1 = (LineString) geom.getGeometryN(0);
        LineString ls2 = (LineString) geom.getGeometryN(1);

        Assertions.assertEquals(2, ls1.getNumPoints());
        Assertions.assertEquals(2, ls2.getNumPoints());

        CoordinateSequence cs2 = ls2.getCoordinateSequence();
        Coordinate c2 = cs2.getCoordinate(1);
        Assertions.assertEquals(103.0, c2.x, 1e6);
        Assertions.assertEquals(3.0, c2.y, 1e6);
    }

    @Test
    public void testMultiPolygon() throws Exception {
        Map<String, Object> json = loadJSONResource("multipolygon.json");
        MultiPolygon geom = (MultiPolygon) GeoJSONReader2.toGeometry(json);

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
    }

    @Test
    public void testGeometryCollection() throws Exception {
        Map<String, Object> json = loadJSONResource("geometrycollection.json");
        GeometryCollection geom = (GeometryCollection) GeoJSONReader2.toGeometry(json);

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
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDeeplyComplex() throws Exception {
        // We delved too greedily and too deep
        Map<String, Object> json = loadJSONResource("featureCollectionDeeplyComplex.json");
        CoordinateReferenceSystem crs84 = CRS.decode("EPSG:4326", true);
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(json, crs84);
        SimpleFeatureCollection fc = GeoJSONReader2.toFeatureCollection(json, schema);
        try (SimpleFeatureIterator it = fc.features()) {
            Assertions.assertTrue(it.hasNext());
            SimpleFeature f1 = it.next();
            Assertions.assertFalse(it.hasNext());
            Assertions.assertNotNull(f1);
            Assertions.assertEquals("ABC_112", f1.getID());
            Assertions.assertEquals("bar", f1.getAttribute("foo"));
            Map<String, Object> a = (Map<String, Object>) f1.getAttribute("a");
            List<Object> foo = (List<Object>) a.get("foo");
            Map<String, Object> fooThe3rd = (Map<String, Object>) foo.get(2);
            Map<String, Object> qux = (Map<String, Object>) fooThe3rd.get("qux");
            Assertions.assertEquals("sure", qux.get("deep"));
            Geometry expectedGeometry = new WKTReader().read("POINT (21.3587384 61.3939013)");
            Assertions.assertEquals(expectedGeometry, f1.getDefaultGeometry());
        }
    }

    @Test
    public void testSimpleArrayComplexProperty() throws Exception {
        // Simple arrays (arrays of numbers, Strings, booleans (not objects)
        Map<String, Object> json = loadJSONResource("featureCollectionSimpleArray.json");
        CoordinateReferenceSystem crs84 = CRS.decode("EPSG:4326", true);
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(json, crs84);
        SimpleFeatureCollection fc = GeoJSONReader2.toFeatureCollection(json, schema);
        try (SimpleFeatureIterator it = fc.features()) {
            Assertions.assertTrue(it.hasNext());
            SimpleFeature f1 = it.next();
            Assertions.assertFalse(it.hasNext());
            Assertions.assertNotNull(f1);
            Assertions.assertEquals("ABC_123", f1.getID());
            Assertions.assertEquals("bar", f1.getAttribute("foo"));
            List<Integer> expectedSimpleArray = Arrays.asList(1, 2, 3);
            MatcherAssert.assertThat(expectedSimpleArray, Is.is(f1.getAttribute("mySimpleIntArray")));
            Geometry expectedGeometry = new WKTReader().read("POINT (21.3743445 61.3764872)");
            Assertions.assertEquals(expectedGeometry, f1.getDefaultGeometry());
        }
    }

    @Test
    public void testComplexProperties() throws Exception {
        Map<String, Object> json = loadJSONResource("featureCollectionComplex.json");
        CoordinateReferenceSystem crs84 = CRS.decode("EPSG:4326", true);
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(json, crs84);
        SimpleFeatureCollection fc = GeoJSONReader2.toFeatureCollection(json, schema);
        try (SimpleFeatureIterator it = fc.features()) {
            SimpleFeature f1 = it.hasNext() ? it.next() : null;
            SimpleFeature f2 = it.hasNext() ? it.next() : null;
            Assertions.assertFalse(it.hasNext());

            Assertions.assertNotNull(f1);
            Assertions.assertNotNull(f2);
            Assertions.assertEquals("P_10000001", f1.getID());
            Assertions.assertEquals("P_10000002", f2.getID());

            
            Assertions.assertEquals(10000001, f1.getAttribute("placeId"));
            Assertions.assertEquals(3, f1.getAttribute("placeVersionId"));
            Assertions.assertEquals(1010110, f1.getAttribute("placeType"));
            Assertions.assertEquals("M3233D4", f1.getAttribute("tm35MapSheet"));
            Assertions.assertEquals("2008-12-05T22:00:00Z", f1.getAttribute("placeCreationTime"));
            Assertions.assertNull(f1.getAttribute("placeNameDeletionTime"));

            Geometry expected = new WKTReader().read("POINT (21.3587384 61.3939013)");
            Assertions.assertEquals(expected, f1.getDefaultGeometry());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> placenames = (List<Map<String, Object>>) f1.getAttribute("name");
            Assertions.assertEquals(1, placenames.size());
            Map<String, Object> placename = placenames.get(0);
            Assertions.assertEquals(40000001, placename.get("placeNameId"));
            Assertions.assertEquals(1, placename.get("placeNameVersionId"));
            Assertions.assertEquals("Isokloppa", placename.get("spelling"));
            Assertions.assertEquals("fin", placename.get("language"));
        }
    }

    @Test
    public void testMultipleGeometries() throws Exception {
        Map<String, Object> json = loadJSONResource("featureCollectionMultipleGeometries.json");
        CoordinateReferenceSystem crs84 = CRS.decode("EPSG:4326", true);
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(json, crs84);
        SimpleFeatureCollection fc = GeoJSONReader2.toFeatureCollection(json, schema);
        try (SimpleFeatureIterator it = fc.features()) {
            SimpleFeature f = it.hasNext() ? it.next() : null;
            Assertions.assertFalse(it.hasNext());
            Assertions.assertNotNull(f);
            Assertions.assertEquals("feature.0", f.getID());
            WKTReader wkt = new WKTReader();
            Assertions.assertEquals(wkt.read("LINESTRING (1.1 1.2, 1.3 1.4)"), f.getAttribute("otherGeometry"));
            Assertions.assertEquals(wkt.read("POINT (0.1 0.1)"), f.getDefaultGeometry());
        }
    }

    @Test
    public void testEmptyFeatureCollection() throws Exception {
        Map<String, Object> json = loadJSONResource("featureCollectionEmpty.json");
        CoordinateReferenceSystem crs84 = CRS.decode("EPSG:4326", true);
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(json, crs84);
        Assertions.assertNull(schema);
        SimpleFeatureCollection fc = GeoJSONReader2.toFeatureCollection(json, schema);
        try (SimpleFeatureIterator it = fc.features()) {
            Assertions.assertFalse(it.hasNext());
        }
    }

    @Test
    public void testDifferentIdTypes() throws Exception {
        Map<String, Object> json = loadJSONResource("featureCollectionDifferentId.json");
        CoordinateReferenceSystem crs84 = CRS.decode("EPSG:4326", true);
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(json, crs84);
        Assertions.assertNotNull(schema);
        SimpleFeatureCollection fc = GeoJSONReader2.toFeatureCollection(json, schema);
        try (SimpleFeatureIterator it = fc.features()) {
            it.hasNext();
            SimpleFeature f1 = it.next();
            it.hasNext();
            SimpleFeature f2 = it.next();
            it.hasNext();
            SimpleFeature f3 = it.next();
            it.hasNext();
            SimpleFeature f4 = it.next();

            Assertions.assertNotNull(f1.getID());
            Assertions.assertEquals(10000001, f1.getAttribute("placeId"));
            Assertions.assertNotNull(f2.getID());
            Assertions.assertEquals(10000002, f2.getAttribute("placeId"));
            Assertions.assertEquals("123", f3.getID());
            Assertions.assertEquals(10000003, f3.getAttribute("placeId"));
            Assertions.assertEquals("ABC_321", f4.getID());
            Assertions.assertEquals(10000004, f4.getAttribute("placeId"));
        }
    }

}
