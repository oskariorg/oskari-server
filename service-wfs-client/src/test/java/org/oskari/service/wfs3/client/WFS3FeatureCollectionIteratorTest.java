package org.oskari.service.wfs3.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class WFS3FeatureCollectionIteratorTest {
    
    @Ignore("Works, don't run when debugging")
    @Test
    public void testReadLinks() throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("wfs3FeatureCollectionSimple.json");
                Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                WFS3FeatureCollectionIterator it = new WFS3FeatureCollectionIterator(reader, null)) {
            while (it.hasNext()) {
                it.next();
            }
            List<WFS3Link> links = it.getHandler().getLinks();
            assertEquals(1, links.size());
            WFS3Link link = links.get(0);
            assertEquals("https://beta-paikkatieto.maanmittauslaitos.fi/geographic-names/wfs3/v1/collections/places/items?bbox=21.350000%2C61.350000%2C21.400000%2C61.400000&limit=1000&lang=fin", link.getHref());
            assertEquals("self", link.getRel());
            assertEquals("application/geo+json", link.getType());
            assertEquals("This document", link.getTitle());
        }
    }
    
    @Ignore("Works, don't run when debugging")
    @Test
    public void testGeoTools() throws IOException, ParseException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("wfs3FeatureCollectionSimple.json");
                Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                FeatureIterator<SimpleFeature> it = new FeatureJSON().streamFeatureCollection(reader)) {
            SimpleFeature f1 = it.hasNext() ? it.next() : null;
            assertTrue(it.hasNext());
            it.next();
            assertFalse(it.hasNext());
            assertNotNull(f1);
            assertEquals("P_10000001", f1.getID());
            assertEquals(10000001L, f1.getAttribute("placeId"));
            assertEquals(3L, f1.getAttribute("placeVersionId"));
            assertEquals(1010110L, f1.getAttribute("placeType"));
            assertEquals("M3233D4", f1.getAttribute("tm35MapSheet"));
            assertEquals("2008-12-05T22:00:00Z", f1.getAttribute("placeCreationTime"));
            assertNull(f1.getAttribute("placeNameDeletionTime"));
            Geometry expected = new WKTReader().read("POINT (21.3587384 61.3939013)");
            assertEquals(expected, f1.getDefaultGeometry());
        }
    }
    
    @Ignore("Works, don't run when debugging")
    @Test
    @SuppressWarnings("unchecked")
    public void testDeeplyComplex() throws IOException, ParseException {
        // We delved too greedily and too deep
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("wfs3FeatureCollectionDeeplyComplex.json");
                Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                WFS3FeatureCollectionIterator it = new WFS3FeatureCollectionIterator(reader, null)) {
            assertTrue(it.hasNext()); 
            SimpleFeature f1 = it.next();
            assertFalse(it.hasNext());
            assertNotNull(f1);
            assertEquals("ABC_112", f1.getID());
            assertEquals("bar", f1.getAttribute("foo"));
            Map<String, Object> a = (Map<String, Object>) f1.getAttribute("a");
            List<Object> foo = (List<Object>) a.get("foo");
            Map<String, Object> fooThe3rd = (Map<String, Object>) foo.get(2);
            Map<String, Object> qux = (Map<String, Object>) fooThe3rd.get("qux");
            assertEquals("sure", qux.get("deep"));
            Geometry expectedGeometry = new WKTReader().read("POINT (21.3587384 61.3939013)");
            assertEquals(expectedGeometry, f1.getDefaultGeometry());
        }
    }
    
    @Ignore("Works, don't run when debugging")
    @Test
    public void testSimpleArrayComplexProperty() throws IOException, ParseException {
        // Simple arrays (arrays of numbers, Strings, booleans (not objects) work with GeoTools out of the box
        // So our extended version also has to work with those
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("wfs3FeatureCollectionSimpleArray.json");
                Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                WFS3FeatureCollectionIterator it = new WFS3FeatureCollectionIterator(reader, null)) {
            assertTrue(it.hasNext()); 
            SimpleFeature f1 = it.next();
            assertFalse(it.hasNext());
            assertNotNull(f1);
            assertEquals("ABC_123", f1.getID());
            assertEquals("bar", f1.getAttribute("foo"));
            List<Long> expectedSimpleArray = Arrays.asList(1L, 2L, 3L);
            assertEquals(expectedSimpleArray, f1.getAttribute("mySimpleIntArray"));
            Geometry expectedGeometry = new WKTReader().read("POINT (21.3743445 61.3764872)");
            assertEquals(expectedGeometry, f1.getDefaultGeometry());
        }
    }

    @Ignore("Works, don't run when debugging")
    @Test
    public void testComplexProperties() throws IOException, ParseException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("wfs3FeatureCollectionComplex.json");
                Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                WFS3FeatureCollectionIterator it = new WFS3FeatureCollectionIterator(reader, null)) {
            SimpleFeature f1 = it.hasNext() ? it.next() : null;
            SimpleFeature f2 = it.hasNext() ? it.next() : null;
            assertFalse(it.hasNext());

            assertNotNull(f1);
            assertNotNull(f2);
            assertEquals("P_10000001", f1.getID());
            assertEquals("P_10000002", f2.getID());
            
            assertEquals(10000001L, f1.getAttribute("placeId"));
            assertEquals(3L, f1.getAttribute("placeVersionId"));
            assertEquals(1010110L, f1.getAttribute("placeType"));
            assertEquals("M3233D4", f1.getAttribute("tm35MapSheet"));
            assertEquals("2008-12-05T22:00:00Z", f1.getAttribute("placeCreationTime"));
            assertNull(f1.getAttribute("placeNameDeletionTime"));
            
            Geometry expected = new WKTReader().read("POINT (21.3587384 61.3939013)");
            assertEquals(expected, f1.getDefaultGeometry());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> placenames = (List<Map<String, Object>>) f1.getAttribute("name");
            assertEquals(1, placenames.size());
            Map<String, Object> placename = placenames.get(0);
            assertEquals(40000001L, placename.get("placeNameId"));
            assertEquals(1L, placename.get("placeNameVersionId"));
            assertEquals("Isokloppa", placename.get("spelling"));
            assertEquals("fin", placename.get("language"));
        }
    }
    
    @Ignore("Doesn't work with GeoTools, don't fail")
    @Test
    public void testFeatureWithRegularGeometryAttributeReadAndGeometryAfterProperties() throws Exception {
        String geojson = ""
                + "{"
                + "  'type': 'Feature',"
                + "  'id': 'feature.0',"
                + "  'properties': {"
                + "    'otherGeometry': {"
                + "      'type': 'LineString',"
                + "      'coordinates': [[1.1, 1.2], [1.3, 1.4]]"
                + "    }"
                + "  },"
                + "  'geometry': {"
                + "    'type': 'Point',"
                + "    'coordinates': [0.1, 0.1]"
                + "  }"
                + "}";
        geojson = geojson.replace('\'', '"');
        SimpleFeature f = new FeatureJSON().readFeature(geojson);
        assertNotNull(f);
        assertEquals("feature.0", f.getID());
        WKTReader wkt = new WKTReader();
        assertEquals(wkt.read("LINESTRING (1.1 1.2, 1.3 1.4)"), f.getAttribute("otherGeometry"));
        assertEquals(wkt.read("POINT (0.1 0.1)"), f.getDefaultGeometry());
    }

    @Test
    public void testMultipleGeometries() throws IOException, ParseException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("wfs3FeatureCollectionMultipleGeometries.json");
                Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                WFS3FeatureCollectionIterator it = new WFS3FeatureCollectionIterator(reader, null)) {
            SimpleFeature f = it.hasNext() ? it.next() : null;
            assertFalse(it.hasNext());
            assertNotNull(f);
            assertEquals("feature.0", f.getID());
            WKTReader wkt = new WKTReader();
            assertEquals(wkt.read("LINESTRING (1.1 1.2, 1.3 1.4)"), f.getAttribute("otherGeometry"));
            assertEquals(wkt.read("POINT (0.1 0.1)"), f.getDefaultGeometry());
        }
    }

}
