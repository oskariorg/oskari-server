package org.oskari.map.userlayer.input;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.junit.Test;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.oskari.map.userlayer.input.KMLParser;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import fi.nls.oskari.service.ServiceException;

public class KMLParserTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testParse() throws ServiceException, URISyntaxException {
        File file = new File(getClass().getResource("samples.kml").toURI());

        KMLParser kml = new KMLParser();

        SimpleFeatureCollection fc = kml.parse(file, null, null);

        SimpleFeatureIterator it = fc.features();
        SimpleFeature doc = it.next();
        assertEquals("document", doc.getFeatureType().getTypeName());
        it.close();

        List<SimpleFeature> folders = (List<SimpleFeature>) doc.getAttribute("Feature");
        assertEquals(6, folders.size());

        SimpleFeature folder = folders.get(0);
        assertEquals("Placemarks", folder.getAttribute("name"));
        assertEquals("These are just some of the different kinds of placemarks with\n" +
                "        which you can mark your favorite places", folder.getAttribute("description"));
        GeometryAttribute ga = folder.getDefaultGeometryProperty();
        assertEquals("LookAt", ga.getName().getLocalPart());
        Point lookAt = (Point) folder.getAttribute("LookAt");
        Coordinate c = lookAt.getCoordinate();
        // LookAt is parsed as lon -> y, lat -> x
        assertEquals(-122.0839597145766, c.y, 1e-6);
        assertEquals(37.42222904525232, c.x, 1e-6);
        assertEquals(0.0, c.z, 0.0);
    }

}
