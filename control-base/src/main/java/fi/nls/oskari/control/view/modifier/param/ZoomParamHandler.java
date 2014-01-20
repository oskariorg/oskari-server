package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ModifierException;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.log.Logger;
import fi.nls.oskari.view.modifier.ModifierParams;

@OskariViewModifier("zoomLevel")
public class ZoomParamHandler extends ParamHandler {

    private static final Logger log = LogFactory.getLogger(ZoomParamHandler.class);
    private static final String PARAM_ZOOM = "zoomLevel";
    private static final String PARAM_ZOOM_LEVELS = "zoomLevels";
    private static final String KEY_ZOOM = "zoom";

    private static final String PROP_ZOOM_ADJUST = "actionhandler.GetAppSetup.ZoomParamHandler.zoomAdjust";

    private static int zoomAdjust = 0;

    @Override
    public void init() {
        super.init();
        zoomAdjust = ConversionHelper.getInt(PropertyUtil.getOptional(PROP_ZOOM_ADJUST), 0);
    }

    
    public boolean handleParam(final ModifierParams params) throws ModifierException {
        if(params.getParamValue() == null) {
            return false;
        }

        try {
            final JSONObject state = getBundleState(params.getConfig(), BUNDLE_MAPFULL);
            int zoom = ConversionHelper.getInt(PropertyUtil.getOptional(params.getParamValue()), 0);

            final String legacyLinkFixParam = params.getActionParams().getHttpParam(PARAM_ZOOM_LEVELS);
            if(legacyLinkFixParam != null) {
                // old maplinks don't have zoomLevels parameter
                // adjust zoomlevel since the number of zoom levels have changed
                zoom = zoom + zoomAdjust;
            }

            state.put(KEY_ZOOM, zoom);
            return true;
        } catch (JSONException je) {
            throw new ModifierException("Could not set zoom from URL param.");
        }
    }
    
}
