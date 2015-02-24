package fi.nls.oskari.work;

import org.junit.Test;

import static org.junit.Assert.*;

public class OWSMapLayerJobTest {

    @Test
    public void testType() {
        assertEquals("Type value should be lowercase", "highlight", OWSMapLayerJob.Type.HIGHLIGHT.toString());
    }

}