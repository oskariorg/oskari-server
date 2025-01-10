package org.geotools.mif;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class MIFDataStoreFactoryTest {

    @Test
    public void testSPIWorks() throws URISyntaxException, IOException {
        URL url = getClass().getResource("kenro_alue_maarajat.MIF").toURI().toURL();
        Map<String, Serializable> params = new HashMap<>();
        params.put("url", url);

        DataStore store = DataStoreFinder.getDataStore(params);
        Assertions.assertTrue(store instanceof MIFDataStore);
        MIFDataStore mifStore = (MIFDataStore) store;

        MIFHeader header = mifStore.readHeader();

        try (MIDReader mid = mifStore.openMID(header)) {
            Assertions.assertTrue(mid.getClass().equals(MIDReader.class));
        }
    }

    @Test
    public void testSPIWorksWithNoMidFile() throws URISyntaxException, IOException {
        URL url = getClass().getResource("test_no_mid.MIF").toURI().toURL();
        Map<String, Serializable> params = new HashMap<>();
        params.put("url", url);

        DataStore store = DataStoreFinder.getDataStore(params);
        Assertions.assertTrue(store instanceof MIFDataStore);
        MIFDataStore mifStore = (MIFDataStore) store;

        MIFHeader header = mifStore.readHeader();

        try (MIDReader mid = mifStore.openMID(header)) {
            Assertions.assertTrue(mid.getClass().equals(NoMIDReader.class));
        }
    }

    @Test
    public void testSPIWorksWithCRS() throws URISyntaxException, IOException, NoSuchAuthorityCodeException, FactoryException {
        String epsg = "EPSG:2393";

        URL url = getClass().getResource("test_no_mid.MIF").toURI().toURL();
        Map<String, Serializable> params = new HashMap<>();
        params.put("url", url);
        params.put("crs", epsg);

        DataStore store = DataStoreFinder.getDataStore(params);
        Assertions.assertTrue(store instanceof MIFDataStore);
        MIFDataStore mifStore = (MIFDataStore) store;

        MIFHeader header = mifStore.readHeader();
        CoordinateReferenceSystem actual = header.getCoordSys();
        CoordinateReferenceSystem expected = CRS.decode("EPSG:2393", true);
        Assertions.assertTrue(CRS.equalsIgnoreMetadata(expected, actual));

        try (MIDReader mid = mifStore.openMID(header)) {
            Assertions.assertTrue(mid.getClass().equals(NoMIDReader.class));
        }
    }

}
