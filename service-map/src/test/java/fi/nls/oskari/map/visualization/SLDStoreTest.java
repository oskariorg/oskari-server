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

        // fail cases -> return empty string
        assertEquals("Expect <null> to return empty SLD", "", SLDStore.getSLD(null));
        assertEquals("Expect 'test dummy random stuff' to return empty SLD", "", SLDStore.getSLD("test dummy random stuff"));
    }
}
