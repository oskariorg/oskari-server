package org.oskari.maplayer;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class GeoJSONStringReaderTest {

    @Test()
    public void testGeoJSONasNull() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            GeoJSONStringReader.readGeoJSON((String) null, "EPSG:4326");
        });
    }
    @Test ()
    public void testInputStreamasNull() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
            GeoJSONStringReader.readGeoJSON((InputStream) null, sourceCRS);
        });
    }

    @Test ()
    public void testGeoJSONasEmptyString() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            GeoJSONStringReader.readGeoJSON("", "EPSG:4326");
        });
    }
    @Test ()
    public void testGeoJSONasEmptyObject() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            GeoJSONStringReader.readGeoJSON("{}", "EPSG:4326");
        });
    }
    @Test ()
    public void testGeoJSONWithEmptyFeatures() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            SimpleFeatureCollection col = GeoJSONStringReader.readGeoJSON("{ \"type\": \"FeatureCollection\"}", "EPSG:4326");
            assertNotNull(col, "Parse should complete");
            assertTrue(col.isEmpty(), "Should get an empty collection");
        });
    }
}