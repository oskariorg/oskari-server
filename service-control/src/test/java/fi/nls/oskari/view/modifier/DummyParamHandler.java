package fi.nls.oskari.view.modifier;

/**
 * Just for testing typing for ViewModifierManager
 */
public class DummyParamHandler extends ParamHandler {
    @Override
    public boolean handleParam(ModifierParams params) throws ModifierException {
        // no-op
        return false;
    }
}
