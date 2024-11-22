package org.oskari.maplayer;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.InputStream;

import static org.junit.Assert.*;

public class GeoJSONStringReaderTest {

    @Test (expected = IllegalArgumentException.class)
    public void testGeoJSONasNull() throws Exception {
        GeoJSONStringReader.readGeoJSON((String) null, "EPSG:4326");
    }
    @Test (expected = IllegalArgumentException.class)
    public void testInputStreamasNull() throws Exception {
        CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
        GeoJSONStringReader.readGeoJSON((InputStream) null, sourceCRS);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testGeoJSONasEmptyString() throws Exception {
        GeoJSONStringReader.readGeoJSON("", "EPSG:4326");
    }
    @Test (expected = IllegalArgumentException.class)
    public void testGeoJSONasEmptyObject() throws Exception {
        GeoJSONStringReader.readGeoJSON("{}", "EPSG:4326");
    }
    @Test (expected = IllegalArgumentException.class)
    public void testGeoJSONWithEmptyFeatures() throws Exception {
        SimpleFeatureCollection col = GeoJSONStringReader.readGeoJSON("{ \"type\": \"FeatureCollection\"}", "EPSG:4326");
        assertNotNull("Parse should complete", col);
        assertTrue("Should get an empty collection", col.isEmpty());
    }
}