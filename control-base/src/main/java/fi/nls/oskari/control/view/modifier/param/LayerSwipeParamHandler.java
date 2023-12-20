package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import fi.nls.oskari.view.modifier.ParamHandler;
import org.json.JSONException;
import org.json.JSONObject;

@OskariViewModifier("layerSwipe")
public class LayerSwipeParamHandler  extends ParamHandler {

    @Override
    public int getPriority() {
        return 1;
    }
    private static final Logger log = LogFactory.getLogger(LayerSwipeParamHandler.class);

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        if(params.getParamValue() == null) {
            return false;
        }

        try {
            final JSONObject state = getBundleState(params.getConfig(), BUNDLE_LAYERSWIPE);
            boolean layerSwipe = ConversionHelper.getBoolean(params.getParamValue(), false);
            state.put(KEY_LAYER_SWIPE, layerSwipe);
            return true;
        } catch (JSONException je) {
            throw new ModifierException("Could not set layerSwipe from URL param.");
        }
    }

}
