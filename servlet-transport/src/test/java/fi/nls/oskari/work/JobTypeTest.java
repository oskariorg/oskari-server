package fi.nls.oskari.work;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JobTypeTest {

    @Test
    public void testType() {
        assertEquals("Type value should be lowercase", "highlight", JobType.HIGHLIGHT.toString());
    }

}