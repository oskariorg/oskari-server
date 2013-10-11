package fi.nls.oskari.log;

public class NullLogger extends Logger {

    public NullLogger(final String name) {
        // ignores all logging
    }

    public boolean isDebugEnabled() {
        return false;
    }

    public void debug(Throwable t, final Object ... args) {
    }
    
    public void debug(final Object ... args) {
    }

    public void info(Throwable t, final Object ... args) {
    }
    
    public void info(final Object ... args) {
    }
    
    public void warn(Throwable t, final Object ... args) {
    }
    
    public void warn(final Object ... args) {
    }
    
    public void error(Throwable t, final Object ... args) {
    }
    
    public void error(final Object ... args) {
    }

}
