package fi.nls.oskari.utils;

import fi.nls.oskari.util.IOHelper;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.InputStream;
import java.util.Properties;

/**
 * Log4J logger implementation
 * @author SMAKINEN
 */
public class Log4JLogger extends fi.nls.oskari.log.Logger {

    private Logger log = null;
    private static boolean propertiesConfigured = false;
    private static final String FQCN = Log4JLogger.class.getName();

    public Log4JLogger(final String name) {
        configure();
        log = Logger.getLogger(name);
    }

    private void log(Level level, Object msg) {
        log(level, msg, null);
    }

    private void log(Level level, Object msg, Throwable t) {
        log.log(FQCN, level, msg, t);
    }

    private void configure() {
        if(propertiesConfigured) {
            return;
        }
        InputStream inStream = null;
        try
        {
            inStream = this.getClass().getClassLoader().getResourceAsStream("/log4j.properties");
            final Properties props = new Properties();
            props.load(inStream);
            PropertyConfigurator.configure(props);
            propertiesConfigured = true;
        }
        catch(Exception e)
        {
            if(inStream != null) {
                System.err.println("Error reading properties from 'log4j.properties': " + e.getMessage());
            }
        }
        finally {
            IOHelper.close(inStream);
        }
        /*
        // To configure with xml you need this:
        try {
            org.apache.log4j.xml.DOMConfigurator.configure("/log4j.xml");
        } catch (Exception ex) {
            System.err.println("Exception configuring with log4j.xml: " + ex.getMessage());
        }
         */
    }

    public boolean isDebugEnabled() {
        if(log == null) {
            return false;
        }
        return log.isDebugEnabled();
    }

    @Override
    public void debug(Throwable t, Object... args) {
        if(log == null || !log.isDebugEnabled()) {
            return;
        }
        log(Level.DEBUG, getString(args), t);
    }

    @Override
    public void debug(Object... args) {
        if(log == null || !log.isDebugEnabled()) {
            return;
        }
        log(Level.DEBUG, getString(args));
    }

    @Override
    public void info(Throwable t, Object... args) {
        if(log == null || !log.isInfoEnabled()) {
            return;
        }
        log(Level.INFO, getString(args), t);
    }

    @Override
    public void info(Object... args) {
        if(log == null || !log.isInfoEnabled()) {
            return;
        }
        log(Level.INFO, getString(args));
    }

    @Override
    public void warn(Throwable t, Object... args) {
        if(log == null) {
            return;
        }
        log(Level.WARN, getString(args), t);
    }

    @Override
    public void warn(Object... args) {
        if(log == null) {
            return;
        }
        log(Level.WARN, getString(args));
    }

    @Override
    public void error(Throwable t, Object... args) {
        if(log == null) {
            return;
        }
        log(Level.ERROR, getString(args), t);
    }

    @Override
    public void error(Object... args) {
        if(log == null) {
            return;
        }
        log(Level.ERROR, getString(args));
    }
}
