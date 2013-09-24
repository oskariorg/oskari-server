package fi.nls.oskari.log;

public class SystemLogger extends Logger {

    private String name = "anonymous";

    public SystemLogger(final String name) {
        this.name = name;
    }

    public boolean isDebugEnabled() {
        return true;
    }

    public void debug(Throwable t, final Object ... args) {
        System.out.println("[DEBUG] " + name + ": " + getString(args) + ": " + t.getMessage());
        t.printStackTrace();
    }
    
    public void debug(final Object ... args) {
        System.out.println("[DEBUG] " + name + ": " + getString(args));
    }

    public void info(Throwable t, final Object ... args) {
        System.out.println("[INFO] " + name + ": " + getString(args) + ": " + t.getMessage());
        t.printStackTrace();
    }
    
    public void info(final Object ... args) {
        System.out.println("[INFO] " + name + ": " + getString(args));
    }
    
    public void warn(Throwable t, final Object ... args) {
        System.err.println("[WARN] " + name + ": " + getString(args) + ": " + t.getMessage());
        t.printStackTrace();
    }
    
    public void warn(final Object ... args) {
        System.err.println("[WARN] " + name + ": " + getString(args));
    }
    
    public void error(Throwable t, final Object ... args) {
        System.err.println("[ERROR] " + name + ": " + getString(args) + ": " + t.getMessage());
        t.printStackTrace();
    }
    
    public void error(final Object ... args) {
        System.err.println("[ERROR] " + name + ": " + getString(args));
    }
}
