package org.oskari.map.userlayer.input;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

import fi.nls.oskari.service.ServiceException;

public class GPXParserTest {

    @Test
    public void testSampleFile() throws URISyntaxException, NoSuchAuthorityCodeException, ServiceException, FactoryException {
        File file = new File(getClass().getResource("sample.gpx").toURI());
        GPXParser parser = new GPXParser();
        SimpleFeatureCollection fc = parser.parse(file, null, CRS.decode("EPSG:3067", true));
        assertEquals(1, fc.size());
        SimpleFeatureIterator it = fc.features();
        try {
            SimpleFeature f = it.next();
            assertEquals("trk name", f.getAttribute("name"));
            MultiLineString mls = (MultiLineString) f.getDefaultGeometry();
            assertEquals(1, mls.getNumGeometries());
            LineString ls = (LineString) mls.getGeometryN(0);
            assertEquals(4, ls.getNumPoints());
            Coordinate c;
            c = ls.getCoordinateN(0);
            assertEquals(500000, c.x, 1e-7);
            assertEquals(6751648.982188291, c.y, 1e-7);
            assertEquals(135.0, c.z, 0.0);
            c = ls.getCoordinateN(1);
            assertEquals(500000, c.x, 1e-7);
            assertEquals(6863040.132332872, c.y, 1e-7);
            assertEquals(135.0, c.z, 0.0);
            c = ls.getCoordinateN(2);
            assertEquals(500000, c.x, 1e-7);
            assertEquals(6974447.589629403, c.y, 1e-7);
            assertEquals(135.0, c.z, 0.0);
            c = ls.getCoordinateN(3);
            assertEquals(500000, c.x, 1e-7);
            assertEquals(7085870.966995355, c.y, 1e-7);
            assertEquals(152.0, c.z, 0.0);
        } finally {
            it.close();
        }
    }

    @Test
    public void testSample10File() throws URISyntaxException, NoSuchAuthorityCodeException, ServiceException, FactoryException {
        File file = new File(getClass().getResource("sample_10.gpx").toURI());
        GPXParser parser = new GPXParser();
        SimpleFeatureCollection fc = parser.parse(file, null, CRS.decode("EPSG:3067", true));
        assertEquals(1, fc.size());
        SimpleFeatureIterator it = fc.features();
        try {
            SimpleFeature f = it.next();
            assertEquals("trk name", f.getAttribute("name"));
            MultiLineString mls = (MultiLineString) f.getDefaultGeometry();
            assertEquals(1, mls.getNumGeometries());
            LineString ls = (LineString) mls.getGeometryN(0);
            assertEquals(4, ls.getNumPoints());
            Coordinate c;
            c = ls.getCoordinateN(0);
            assertEquals(500000, c.x, 1e-7);
            assertEquals(6751648.982188291, c.y, 1e-7);
            assertEquals(135.0, c.z, 0.0);
            c = ls.getCoordinateN(1);
            assertEquals(500000, c.x, 1e-7);
            assertEquals(6863040.132332872, c.y, 1e-7);
            assertEquals(135.0, c.z, 0.0);
            c = ls.getCoordinateN(2);
            assertEquals(500000, c.x, 1e-7);
            assertEquals(6974447.589629403, c.y, 1e-7);
            assertEquals(135.0, c.z, 0.0);
            c = ls.getCoordinateN(3);
            assertEquals(500000, c.x, 1e-7);
            assertEquals(7085870.966995355, c.y, 1e-7);
            assertEquals(152.0, c.z, 0.0);
        } finally {
            it.close();
        }
    }

    @Test
    public void testWaypoints() throws URISyntaxException, NoSuchAuthorityCodeException, ServiceException, FactoryException {
        File file = new File(getClass().getResource("waypoints.gpx").toURI());
        GPXParser parser = new GPXParser();
        SimpleFeatureCollection fc = parser.parse(file, null, DefaultGeographicCRS.WGS84);
        assertEquals(2, fc.size());
        SimpleFeatureIterator it = fc.features();
        try {
            int i = 1;
            while (it.hasNext()) {
                SimpleFeature f = it.next();
                assertEquals("name" + i, f.getAttribute("name"));
                assertEquals("cmt" + i, f.getAttribute("cmt"));
                assertEquals("desc" + i, f.getAttribute("desc"));
                assertEquals("src" + i, f.getAttribute("src"));
                assertEquals("http://li.nk/" + i, f.getAttribute("linkHref"));
                assertEquals("linktext" + i, f.getAttribute("linkText"));
                assertEquals("WayPoint", f.getAttribute("type"));
                i++;
            }
        } finally {
            it.close();
        }
    }

}
