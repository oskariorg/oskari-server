package org.oskari.spatineo.monitor.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oskari.spatineo.monitor.api.model.Response;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
public class SpatineoMonitorDaoTest {

    SpatineoMonitorDao monitor;

    @BeforeEach
    public void init() {
        monitor = new SpatineoMonitorDao("foo", "bar");
    }

    @Test
    public void whenEndPointIsNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new SpatineoMonitorDao(null, "bar"), "");
    }

    @Test
    public void whenEndPointIsEmptyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new SpatineoMonitorDao("", "bar"), "");
    }

    @Test
    public void whenAPIKeyIsNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new SpatineoMonitorDao("foo", null), "");
    }

    @Test
    public void whenAPIKeyIsEmptyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new SpatineoMonitorDao("foo", ""), "");
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
