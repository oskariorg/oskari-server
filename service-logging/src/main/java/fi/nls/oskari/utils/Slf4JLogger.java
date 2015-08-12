package fi.nls.oskari.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

/**
 * Slf4J logger implementation
 * @author SMAKINEN
 */
public class Slf4JLogger extends fi.nls.oskari.log.Logger {

    private enum Level {
        DEBUG(LocationAwareLogger.DEBUG_INT),
        INFO(LocationAwareLogger.INFO_INT),
        WARN(LocationAwareLogger.WARN_INT),
        ERROR(LocationAwareLogger.ERROR_INT);

        private int level = 1;

        private Level(int l) {
            level = l;
        }
        public int getLevel() {
            return level;
        }
    }

    private Logger log = null;
    private static final String FQCN = Slf4JLogger.class.getName();

    public Slf4JLogger(final String name) {
        log = LoggerFactory.getLogger(name);
    }

    private void log(Level level, String msg) {
        log(level, msg, null);
    }

    private void log(Level level, String msg, Throwable t) {
        if (log instanceof LocationAwareLogger) {
            final LocationAwareLogger la = (LocationAwareLogger) log;
            la.log(null, FQCN, level.getLevel(), msg, null, t);
            return;
        }
        switch (level) {
            case DEBUG:
                log.debug(msg, t);
                break;
            case INFO:
                log.info(msg, t);
                break;
            case WARN:
                log.warn(msg, t);
                break;
            case ERROR:
                log.error(msg, t);
                break;
        }
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
