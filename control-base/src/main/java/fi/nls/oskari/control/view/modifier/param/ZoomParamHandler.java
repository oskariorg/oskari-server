package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.log.Logger;
import fi.nls.oskari.view.modifier.ModifierParams;

@OskariViewModifier("zoomLevel")
public class ZoomParamHandler extends ParamHandler {

    private static final Logger log = LogFactory.getLogger(ZoomParamHandler.class);
    private static final String PARAM_ZOOM = "zoomLevel";
    private static final String KEY_ZOOM = "zoom";

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        if(params.getParamValue() == null) {
            return false;
        }

        try {
            final JSONObject state = getBundleState(params.getConfig(), BUNDLE_MAPFULL);
            int zoom = ConversionHelper.getInt(params.getParamValue(), 0);
            if(params.getActionParams().getHttpParam("ver") == null) {
                if(zoom == 8) zoom = 7;
            }
            state.put(KEY_ZOOM, zoom);
            return true;
        } catch (JSONException je) {
            throw new ModifierException("Could not set zoom from URL param.");
        }
    }
    
}
