package org.oskari.map.userlayer.input;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.CoordinateSequence;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

import fi.nls.oskari.service.ServiceException;

public class GPXParserTest {

    @Test
    public void testSampleFile() throws URISyntaxException, NoSuchAuthorityCodeException, ServiceException, FactoryException {
        File file = new File(getClass().getResource("sample.gpx").toURI());
        GPXParser parser = new GPXParser();
        SimpleFeatureCollection fc = parser.parse(file, null, CRS.decode("EPSG:3067", true));
        Assertions.assertEquals(1, fc.size());
        SimpleFeatureIterator it = fc.features();
        try {
            SimpleFeature f = it.next();
            Assertions.assertEquals("trk name", f.getAttribute("name"));
            MultiLineString mls = (MultiLineString) f.getDefaultGeometry();
            Assertions.assertEquals(1, mls.getNumGeometries());
            LineString ls = (LineString) mls.getGeometryN(0);
            Assertions.assertEquals(4, ls.getNumPoints());
            Coordinate c;
            c = ls.getCoordinateN(0);
            Assertions.assertEquals(500000, c.x, 1e-7);
            Assertions.assertEquals(6751648.982188291, c.y, 1e-7);
            Assertions.assertEquals(135.0, c.z, 0.0);
            c = ls.getCoordinateN(1);
            Assertions.assertEquals(500000, c.x, 1e-7);
            Assertions.assertEquals(6863040.132332872, c.y, 1e-7);
            Assertions.assertEquals(135.0, c.z, 0.0);
            c = ls.getCoordinateN(2);
            Assertions.assertEquals(500000, c.x, 1e-7);
            Assertions.assertEquals(6974447.589629403, c.y, 1e-7);
            Assertions.assertEquals(135.0, c.z, 0.0);
            c = ls.getCoordinateN(3);
            Assertions.assertEquals(500000, c.x, 1e-7);
            Assertions.assertEquals(7085870.966995355, c.y, 1e-7);
            Assertions.assertEquals(152.0, c.z, 0.0);
        } finally {
            it.close();
        }
    }

    @Test
    public void testSample10File() throws URISyntaxException, NoSuchAuthorityCodeException, ServiceException, FactoryException {
        File file = new File(getClass().getResource("sample_10.gpx").toURI());
        GPXParser parser = new GPXParser();
        SimpleFeatureCollection fc = parser.parse(file, null, CRS.decode("EPSG:3067", true));
        Assertions.assertEquals(1, fc.size());
        SimpleFeatureIterator it = fc.features();
        try {
            SimpleFeature f = it.next();
            Assertions.assertEquals("trk name", f.getAttribute("name"));
            MultiLineString mls = (MultiLineString) f.getDefaultGeometry();
            Assertions.assertEquals(1, mls.getNumGeometries());
            LineString ls = (LineString) mls.getGeometryN(0);
            Assertions.assertEquals(4, ls.getNumPoints());
            Coordinate c;
            c = ls.getCoordinateN(0);
            Assertions.assertEquals(500000, c.x, 1e-7);
            Assertions.assertEquals(6751648.982188291, c.y, 1e-7);
            Assertions.assertEquals(135.0, c.z, 0.0);
            c = ls.getCoordinateN(1);
            Assertions.assertEquals(500000, c.x, 1e-7);
            Assertions.assertEquals(6863040.132332872, c.y, 1e-7);
            Assertions.assertEquals(135.0, c.z, 0.0);
            c = ls.getCoordinateN(2);
            Assertions.assertEquals(500000, c.x, 1e-7);
            Assertions.assertEquals(6974447.589629403, c.y, 1e-7);
            Assertions.assertEquals(135.0, c.z, 0.0);
            c = ls.getCoordinateN(3);
            Assertions.assertEquals(500000, c.x, 1e-7);
            Assertions.assertEquals(7085870.966995355, c.y, 1e-7);
            Assertions.assertEquals(152.0, c.z, 0.0);
        } finally {
            it.close();
        }
    }

    @Test
    public void testWaypoints() throws URISyntaxException, NoSuchAuthorityCodeException, ServiceException, FactoryException {
        File file = new File(getClass().getResource("waypoints.gpx").toURI());
        GPXParser parser = new GPXParser();
        SimpleFeatureCollection fc = parser.parse(file, null, DefaultGeographicCRS.WGS84);
        Assertions.assertEquals(2, fc.size());
        SimpleFeatureIterator it = fc.features();
        try {
            int i = 1;
            while (it.hasNext()) {
                SimpleFeature f = it.next();
                Assertions.assertEquals("name" + i, f.getAttribute("name"));
                Assertions.assertEquals("cmt" + i, f.getAttribute("cmt"));
                Assertions.assertEquals("desc" + i, f.getAttribute("desc"));
                Assertions.assertEquals("src" + i, f.getAttribute("src"));
                Assertions.assertEquals("http://li.nk/" + i, f.getAttribute("linkHref"));
                Assertions.assertEquals("linktext" + i, f.getAttribute("linkText"));
                Assertions.assertEquals("WayPoint", f.getAttribute("type"));
                i++;
            }
        } finally {
            it.close();
        }
    }

    @Test
    public void testTrack10() throws ServiceException, URISyntaxException {
        File file = new File(getClass().getResource("track_10.gpx").toURI());
        GPXParser parser = new GPXParser();
        SimpleFeatureCollection fc = parser.parse(file, null, DefaultGeographicCRS.WGS84);
        Assertions.assertEquals(1, fc.size());
        SimpleFeatureIterator it = fc.features();
        try {
            while (it.hasNext()) {
                SimpleFeature f = it.next();
                MultiLineString track = (MultiLineString) f.getDefaultGeometry();
                Assertions.assertEquals(1, track.getNumGeometries());
                LineString tracksegment = (LineString) track.getGeometryN(0);
                CoordinateSequence csq = tracksegment.getCoordinateSequence();
                Assertions.assertEquals(2, csq.size());

                Assertions.assertEquals(25.704601407051086, csq.getX(0), 1e-8);
                Assertions.assertEquals(62.494332790374756, csq.getY(0), 1e-8);
                Assertions.assertEquals(152.50732421875, csq.getOrdinate(0, 2), 1e-8);

                Assertions.assertEquals(25.704869627952576, csq.getX(1), 1e-8);
                Assertions.assertEquals(62.49429523944855, csq.getY(1), 1e-8);
                Assertions.assertEquals(149.7603759765625, csq.getOrdinate(1, 2), 1e-8);
            }
        } finally {
            it.close();
        }
    }

}
