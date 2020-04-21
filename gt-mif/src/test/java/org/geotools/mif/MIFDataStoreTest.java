package org.geotools.mif;

import java.io.File;

import org.junit.Test;

public class MIFDataStoreTest {
    
    @Test
    public void test() {
        File mif = new File(getClass().getResource("kenro_alue_maarajat.MIF").toURI());
        File mid = new File(getClass().getResource("kenro_alue_maarajat.MID").toURI());
        new MIFDataStoreSource(entry, query)
    }

}
