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
import org.junit.Test;

public class MIFDataStoreFactoryTest {

    @Test
    public void testSPIWorks() throws URISyntaxException, IOException {
        URL url = getClass().getResource("kenro_alue_maarajat.MIF").toURI().toURL();
        Map<String, Serializable> params = new HashMap<>();
        params.put("url", url);

        DataStore store = DataStoreFinder.getDataStore(params);
        assertTrue(store instanceof MIFDataStore);
        MIFDataStore mifStore = (MIFDataStore) store;

        MIFHeader header = mifStore.readHeader();

        try (MIDReader mid = mifStore.openMID(header)) {
            assertTrue(mid.getClass().equals(MIDReader.class));
        }
    }

    @Test
    public void testSPIWorksWithNoMidFile() throws URISyntaxException, IOException {
        URL url = getClass().getResource("test_no_mid.MIF").toURI().toURL();
        Map<String, Serializable> params = new HashMap<>();
        params.put("url", url);

        DataStore store = DataStoreFinder.getDataStore(params);
        assertTrue(store instanceof MIFDataStore);
        MIFDataStore mifStore = (MIFDataStore) store;

        MIFHeader header = mifStore.readHeader();

        try (MIDReader mid = mifStore.openMID(header)) {
            assertTrue(mid.getClass().equals(NoMIDReader.class));
        }
    }

}
