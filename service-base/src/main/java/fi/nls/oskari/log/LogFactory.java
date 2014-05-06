package fi.nls.oskari.log;

import fi.nls.oskari.util.PropertyUtil;

import java.lang.reflect.Constructor;

public class LogFactory {

    private LogFactory() {
        // use getLogger();
    }

    public static Logger getLogger(final Class<?> c) {
        return getLogger(c.getCanonicalName());
    }

    public static Logger getLogger(final String name) {

        final String className = PropertyUtil.getOptionalNonLocalized("oskari.logger");
        if(className == null) {
            //return new NullLogger(name);
            return new SystemLogger(name);
        }
        try {
            final Class c = Class.forName(className);
            final Constructor<?> cons = c.getConstructor(String.class);
            final Logger logger = (Logger)cons.newInstance(name);
            return logger;
        } catch (Exception e) {
            System.err.println("Couldn't initialize logger for name: " + name +
                    ". Check that a property 'oskari.logger' has value of a fully qualified name for class extending fi.nls.oskari.log.Logger");
        }
        return new NullLogger(name);
    }
}
