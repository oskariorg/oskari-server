package fi.nls.oskari.view.modifier;

/**
 * Generic "something went wrong" and we want additional debug info/stack trace.
 * @author SMAKINEN
 */
public class ModifierException extends Exception {
    
    public ModifierException(final String message, final Exception e) {
        super(message, e);
    }

    public ModifierException(final String message) {
        super(message);
    }

}
