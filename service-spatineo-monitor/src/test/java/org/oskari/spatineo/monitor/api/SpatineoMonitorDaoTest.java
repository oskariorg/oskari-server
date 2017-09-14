package org.oskari.spatineo.monitor.api;

import org.junit.Before;
import org.junit.Test;
import org.oskari.spatineo.monitor.api.model.Response;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class SpatineoMonitorDaoTest {

    SpatineoMonitorDao monitor;

    @Before
    public void init() {
        monitor = new SpatineoMonitorDao("foo", "bar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenEndPointIsNullThrowsException() {
        new SpatineoMonitorDao(null, "bar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenEndPointIsEmptyThrowsException() {
        new SpatineoMonitorDao("", "bar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAPIKeyIsNullThrowsException() {
        new SpatineoMonitorDao("foo", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAPIKeyIsEmptyThrowsException() {
        new SpatineoMonitorDao("foo", "");
    }

    @Test
    public void testParseResponseEmpty() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/responseEmpty.json")) {
            Response response = monitor.parse(in);
            assertEquals("1.0", response.getVersion());
            assertEquals("OK", response.getStatus());
            assertNull(response.getStatusMessage());
            assertNotNull(response.getResult());
            assertEquals(0, response.getResult().size());
        }
    }

    @Test
    public void testParseResponseError() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/responseError.json")) {
            Response response = monitor.parse(in);
            assertEquals("1.0", response.getVersion());
            assertEquals("ERROR", response.getStatus());
            assertEquals("No such API key", response.getStatusMessage());
            assertNull(response.getResult());
        }
    }

    @Test
    public void testParseResponseOk() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/responseOk.json")) {
            Response response = monitor.parse(in);
            assertEquals("1.0", response.getVersion());
            assertEquals("OK", response.getStatus());
            assertNotNull(response.getResult());
            assertEquals(81, response.getResult().size());
        }
    }

}
