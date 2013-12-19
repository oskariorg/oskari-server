package fi.nls.oskari.scheduler;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;

import org.junit.Test;

public class TriggererTest {
    @Test
    public void test() {
        // scheduler and job initializing
        Triggerer triggerer = new Triggerer();
        triggerer.initSchemaCacheValidator();
        assertTrue(true);
    }
}
