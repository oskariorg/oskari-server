package org.geotools.mif;

import org.geotools.api.data.DataStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class MIFDataStoreTest {
    
    @Test
    public void testDataStore() throws URISyntaxException, IOException {
        File mif = new File(getClass().getResource("kenro_alue_maarajat.MIF").toURI());
        File mid = new File(getClass().getResource("kenro_alue_maarajat.MID").toURI());
        DataStore store = new MIFDataStore(mif, mid);
        String[] names = store.getTypeNames();
        Assertions.assertEquals(1, names.length);
        Assertions.assertEquals("kenro_alue_maarajat", names[0]);
    }

}
