package fi.nls.oskari.log;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Testing simple log level for SystemLogger via env-properties. Ignored so it doesn't
 */
@Ignore
public class SystemLoggerTest {

    @After
    public static void teardown() {
        System.clearProperty(SystemLogger.PROPERTY_LOG_LEVEL);
    }

    @Test
    public void testDebug()
            throws Exception {
        System.setProperty(SystemLogger.PROPERTY_LOG_LEVEL, "debug");
        SystemLogger logger = new SystemLogger("testlogger.debug");
        logger.debug("debug");
        logger.warn("warn");
    }

    @Test
    public void testWarn()
            throws Exception {
        System.setProperty(SystemLogger.PROPERTY_LOG_LEVEL, "warn");
        SystemLogger logger = new SystemLogger("testlogger.warn");
        logger.debug("debug");
        logger.warn("warn");
    }
}