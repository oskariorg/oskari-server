package fi.nls.oskari.log;

public class SystemLogger extends Logger {

    public static final String PROPERTY_LOG_LEVEL = "oskari.syslog.level";
    private String name = "anonymous";
    private enum Level {
        DEBUG("debug"),
        INFO("info"),
        WARN("warn"),
        ERROR("error");

        String level;

        Level(String level) {
            this.level = level;
        }
        public static Level fromString(String text) {
            for (Level b : Level.values()) {
                if (b.level.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return Level.DEBUG;
        }
        public boolean shouldLog(Level testFor) {
            return this.ordinal() <= testFor.ordinal();
        }
    }

    private Level level = Level.DEBUG;

    public SystemLogger(final String name) {
        level = Level.fromString(System.getProperty(PROPERTY_LOG_LEVEL));
        this.name = name;
    }

    public boolean isDebugEnabled() {
        return level.shouldLog(Level.DEBUG);
    }

    public void debug(Throwable t, final Object ... args) {
        if(!level.shouldLog(Level.DEBUG)) {
            return;
        }
        System.out.println("[DEBUG] " + name + ": " + getString(args) + ": " + t.getMessage());
        t.printStackTrace();
    }
    
    public void debug(final Object ... args) {
        if(!level.shouldLog(Level.DEBUG)) {
            return;
        }
        System.out.println("[DEBUG] " + name + ": " + getString(args));
    }

    public void info(Throwable t, final Object ... args) {
        if(!level.shouldLog(Level.INFO)) {
            return;
        }
        System.out.println("[INFO] " + name + ": " + getString(args) + ": " + t.getMessage());
        t.printStackTrace();
    }
    
    public void info(final Object ... args) {
        if(!level.shouldLog(Level.INFO)) {
            return;
        }
        System.out.println("[INFO] " + name + ": " + getString(args));
    }
    
    public void warn(Throwable t, final Object ... args) {
        if(!level.shouldLog(Level.WARN)) {
            return;
        }
        System.err.println("[WARN] " + name + ": " + getString(args) + ": " + t.getMessage());
        t.printStackTrace();
    }
    
    public void warn(final Object ... args) {
        if(!level.shouldLog(Level.WARN)) {
            return;
        }
        System.err.println("[WARN] " + name + ": " + getString(args));
    }
    
    public void error(Throwable t, final Object ... args) {
        if(!level.shouldLog(Level.ERROR)) {
            return;
        }
        System.err.println("[ERROR] " + name + ": " + getString(args) + ": " + t.getMessage());
        t.printStackTrace();
    }
    
    public void error(final Object ... args) {
        if(!level.shouldLog(Level.ERROR)) {
            return;
        }
        System.err.println("[ERROR] " + name + ": " + getString(args));
    }
}
