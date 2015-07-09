package fi.nls.oskari.map.servlet;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.servlet.WebappHelper;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Initializes context for oskari-map servlet:
 * - Loads properties
 * - Checks database connections
 * <p/>
 * Prints status messages and tries to act nice even if runtime exception occurs.
 */
public class OskariContextInitializer implements ServletContextListener {

    private static Logger log = LogFactory.getLogger(OskariContextInitializer.class);

    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        WebappHelper.teardown();
        log.info("Context destroy");
    }

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        WebappHelper.loadProperties();
        // init logger after the properties so we get the correct logger impl
        log = LogFactory.getLogger(OskariContextInitializer.class);
        WebappHelper.init();
    }

}
