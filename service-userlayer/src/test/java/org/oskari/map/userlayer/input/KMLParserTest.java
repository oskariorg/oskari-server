package org.oskari.map.userlayer.input;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.oskari.map.userlayer.input.KMLParser;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import fi.nls.oskari.service.ServiceException;

public class KMLParserTest {

    @Test
    public void testParseExtendedData() throws ServiceException, URISyntaxException {
        File file = new File(getClass().getResource("user_data.kml").toURI());
        KMLParser kml = new KMLParser();
        SimpleFeatureCollection fc = kml.parse(file, null, null);
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
                // point is parsed as lon -> y, lat -> x
                assertEquals(60.2063017, c.y, 1e-6);
                assertEquals(24.9021089, c.x, 1e-6);
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
    public void testParseSampleData() throws ServiceException, URISyntaxException {
        File file = new File(getClass().getResource("samples.kml").toURI());
        KMLParser kml = new KMLParser();
        SimpleFeatureCollection fc = kml.parse(file, null, null);
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
            }
        }
        assertEquals(true, foundHidden);
        assertEquals("Can't find The Pentagon. Has plane chrashed or just the test failed", true, foundPentagon);
    }
    //@Test PARSER DOESN'T SUPPORT TYPED EXTENDED DATA (types are defined in Schema element)
    public void testParseTypedUserData() throws ServiceException, URISyntaxException {
        // file is converted from shape
        File file = new File(getClass().getResource("hki.kml").toURI());
        KMLParser kml = new KMLParser();
        SimpleFeatureCollection fc = kml.parse(file, null, null);
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
