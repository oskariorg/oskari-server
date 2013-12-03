package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import fi.nls.oskari.view.modifier.ViewModifier;

public abstract class ParamHandler extends ViewModifier {

    /**
     * Modifies base view based on http parameters
     * @param params
     * @return true if location has been modified from default, false if not
     * @throws ModifierException if something went wrong while handling the parameter
     */
    public abstract boolean handleParam(final ModifierParams params) throws ModifierException;

}
