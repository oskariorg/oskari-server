package fi.nls.oskari.view.modifier;

public abstract class ParamHandler extends ViewModifier implements Comparable<ParamHandler> {

    public static final int DEFAULT_PRIORITY = 500;

    public int getPriority() {
        return DEFAULT_PRIORITY;
    }
    /**
     * Modifies base view based on http parameters
     * @param params
     * @return true if location has been modified from default, false if not
     * @throws ModifierException if something went wrong while handling the parameter
     */
    public abstract boolean handleParam(final ModifierParams params) throws ModifierException;

    @Override
    public int compareTo(ParamHandler o) {
        if(o != null) {
            return getPriority() - o.getPriority();
        }
        return 0;
    }
}
