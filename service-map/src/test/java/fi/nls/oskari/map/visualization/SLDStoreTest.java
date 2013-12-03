package fi.nls.oskari.map.visualization;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @author SMAKINEN
 */
public class SLDStoreTest {
    @Test
    public void testGetSLD() {
        // gtk_wfs should be present
        assertNotSame("Expect 'gtk_wfs' to return some content", "", SLDStore.getSLD("gtk_wfs"));

        final String defaultSLD = SLDStore.getSLD("default");
        // fail cases -> return default sld
        assertEquals("Expect <null> to return default SLD", defaultSLD, SLDStore.getSLD(null));
        assertEquals("Expect 'test dummy random stuff' to return default SLD", defaultSLD, SLDStore.getSLD("test dummy random stuff"));
    }
}
