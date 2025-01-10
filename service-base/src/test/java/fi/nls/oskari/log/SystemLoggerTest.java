package fi.nls.oskari.log;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Testing simple log level for SystemLogger via env-properties. Ignored so it doesn't
 */
@Disabled
public class SystemLoggerTest {

    @AfterEach
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