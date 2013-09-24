package fi.nls.oskari.control.view.modifier.bundle;

import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import fi.nls.oskari.view.modifier.ViewModifier;

public abstract class BundleHandler extends ViewModifier {

    /**
     * Interface method for BundleHandlers altering a bundle
     * @param params
     * @return true if the modifier changed map location
     * @throws ModifierException is thrown if the bundle cannot be cannot be modified
     */
    public abstract boolean modifyBundle(final ModifierParams params) throws ModifierException;

}
