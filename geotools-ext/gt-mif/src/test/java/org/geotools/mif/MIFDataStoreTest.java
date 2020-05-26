package org.geotools.mif;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.geotools.data.DataStore;
import org.junit.Test;

public class MIFDataStoreTest {
    
    @Test
    public void testDataStore() throws URISyntaxException, IOException {
        File mif = new File(getClass().getResource("kenro_alue_maarajat.MIF").toURI());
        File mid = new File(getClass().getResource("kenro_alue_maarajat.MID").toURI());
        DataStore store = new MIFDataStore(mif, mid);
        String[] names = store.getTypeNames();
        assertEquals(1, names.length);
        assertEquals("kenro_alue_maarajat", names[0]);
    }

}
