package fi.nls.oskari.utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Log4J logger implementation
 * @author SMAKINEN
 */
public class Log4JLogger extends fi.nls.oskari.log.Logger {

    private Logger log;

    public Log4JLogger(final String name) {
        log = LogManager.getLogger(name);
    }

    private void log(Level level, Object msg) {
        log.log(level, msg);
    }

    private void log(Level level, Object msg, Throwable t) {
        log.log(level, msg, t);
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
