package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.view.modifier.ParamHandler;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONObject;

@OskariViewModifier("rotate")
public class MapRotatorParamHandler extends ParamHandler {

    @Override
    public int getPriority() {
        return 1;
    }

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        final JSONObject state = getBundleState(params.getConfig(), BUNDLE_MAPROTATOR);
        double degrees = ConversionHelper.getDouble(params.getParamValue(), 0);
        JSONHelper.putValue(state, "degrees", degrees);
        return true;
    }
    
}