package org.oskari.map.userlayer.input;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.oskari.map.userlayer.input.KMLParser;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import fi.nls.oskari.service.ServiceException;

public class KMLParserTest {

    @Test
    public void testParseExtendedData() throws ServiceException, URISyntaxException, FactoryException {
        File file = new File(getClass().getResource("user_data.kml").toURI());
        KMLParser kml = new KMLParser();
        SimpleFeatureCollection fc = kml.parse(file, null, CRS.decode("EPSG:3067"));
        SimpleFeatureType schema = fc.getSchema();
        boolean found = false;
        // test schema
        assertEquals(5, schema.getAttributeCount());
        assertEquals(Geometry.class, schema.getDescriptor(KMLParser.KML_GEOM).getType().getBinding());
        assertEquals(String.class, schema.getDescriptor("Opening hours").getType().getBinding());
        // test feature count
        assertEquals(5, fc.size());
        // loop features
        SimpleFeatureIterator it = fc.features();
        while (it.hasNext()){
            SimpleFeature feature = it.next();
            assertEquals(5, feature.getAttributeCount());
            if ("Ruskeasuo".equals(feature.getAttribute(KMLParser.KML_NAME))){
                found = true;
                Point p = (Point) feature.getDefaultGeometry();
                Coordinate c = p.getCoordinate();
                assertEquals(383726.9520816681, c.x, 1e-6);
                assertEquals(6676234.520781213, c.y, 1e-6);
                assertEquals(0.0, c.z, 0.0);
                assertEquals("Floorball arena", feature.getAttribute(KMLParser.KML_DESC));
                assertEquals("9-20", feature.getAttribute("Opening hours"));
                // should be empty string instead of null, to get feature which matches schema
                assertEquals("", feature.getAttribute("Additional info"));
            } else if ("Route".equals(feature.getAttribute(KMLParser.KML_NAME))) {
                LineString line = (LineString) feature.getDefaultGeometry();
                assertEquals(157, line.getCoordinates().length);
                assertEquals("Route from the Arena center to the Codecorner", feature.getAttribute(KMLParser.KML_DESC));
            }
        }
        assertEquals("Failed to parse feature Ruskeasuo", true, found);
    }
    @Test
    public void testParseSampleData() throws ServiceException, URISyntaxException, FactoryException {
        File file = new File(getClass().getResource("samples.kml").toURI());
        KMLParser kml = new KMLParser();
        SimpleFeatureCollection fc = kml.parse(file, null, CRS.decode("EPSG:4326"));
        boolean foundHidden = false;
        boolean foundPentagon = false;
        // test schema
        SimpleFeatureType schema = fc.getSchema();
        assertEquals(3, schema.getAttributeCount());
        // test feature count
        assertEquals(20, fc.size());
        // loop features
        SimpleFeatureIterator it = fc.features();
        while (it.hasNext()){
            SimpleFeature feature = it.next();
            assertEquals(3, feature.getAttributeCount());
            // hidden placemark with no geometry
            if (feature.getDefaultGeometry() == null){
                foundHidden = true;
                assertEquals("Descriptive HTML", feature.getAttribute(KMLParser.KML_NAME));
            } else if ("The Pentagon".equals(feature.getAttribute(KMLParser.KML_NAME))) {
                foundPentagon = true;
                assertEquals("Shouldn't have kml LookAt attribute", null, feature.getAttribute("LookAt"));
                Polygon p = (Polygon) feature.getDefaultGeometry();
                assertEquals(1, p.getNumInteriorRing());
                LineString ring = p.getExteriorRing();
                assertEquals (6, ring.getNumPoints());
                Coordinate c = ring.getCoordinateN(0);
                // KML has always lonlat 4326, but 4326 has latlon ordering
                assertEquals(38.87253259892824, c.x, 1e-6);
                assertEquals(-77.05788457660967, c.y, 1e-6);
                assertEquals(100.0, c.z, 0);
                ring = p.getInteriorRingN(0);
                assertEquals (6, ring.getNumPoints());
                c = ring.getCoordinateN(0);
                assertEquals(38.87154239798456, c.x, 1e-6);
                assertEquals(-77.05668055019126, c.y, 1e-6);
                assertEquals(100.0, c.z, 0);
            }
        }
        assertEquals(true, foundHidden);
        assertEquals("Can't find The Pentagon. Has plane chrashed or just the test failed", true, foundPentagon);
    }
    //@Test PARSER DOESN'T SUPPORT TYPED EXTENDED DATA (types are defined in Schema element)
    public void testParseTypedUserData() throws ServiceException, URISyntaxException, FactoryException {
        // file is converted from shape
        File file = new File(getClass().getResource("hki.kml").toURI());
        KMLParser kml = new KMLParser();
        SimpleFeatureCollection fc = kml.parse(file, null, CRS.decode("EPSG:3067"));
        boolean found = false;
        // test schema
        SimpleFeatureType schema = fc.getSchema();
        assertEquals(5, schema.getAttributeCount());
        // test feature count
        assertEquals(2, fc.size());
        // loop features
        SimpleFeatureIterator it = fc.features();
        while (it.hasNext()){
            SimpleFeature feature = it.next();
            assertEquals(5, feature.getAttributeCount());
            if ("Kauniainen".equals(feature.getAttribute("NIMI"))) {
                found = true;
                assertEquals("505", feature.getAttribute("KUNTA"));
                assertEquals(Polygon.class, feature.getDefaultGeometry().getClass());
            }
        }
        assertEquals(true, found);
    }
}
