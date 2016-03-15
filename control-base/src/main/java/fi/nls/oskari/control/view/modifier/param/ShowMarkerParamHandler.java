package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Creates a default marker on map center point for MarkersPlugin
 */
@OskariViewModifier("showMarker")
public class ShowMarkerParamHandler extends MarkersParamHandler {

    private static final Logger log = LogFactory.getLogger(ShowMarkerParamHandler.class);

    @Override
    public int getPriority() {
        return DEFAULT_PRIORITY * 10000;
    }

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        if(params.getParamValue() == null) {
            return false;
        }

        if("true".equals(params.getParamValue())) {
            final JSONObject mapfullState = getBundleState(params.getConfig(), BUNDLE_MAPFULL);
            if(mapfullState == null) {
                log.warn("Couldn't get mapfull state");
                return false;
            }
            final JSONObject centerMarker = getMarker(mapfullState.optDouble(KEY_EAST, -1),
                       mapfullState.optDouble(KEY_NORTH, -1));
            if(centerMarker == null) {
                log.warn("Couldn't create default marker for coordinates x:",
                        mapfullState.optDouble(KEY_EAST, -1), "y:", mapfullState.optDouble(KEY_NORTH, -1));
                return false;
            }

            // flag as transient so it isn't saved in state
            JSONHelper.putValue(centerMarker, KEY_TRANSIENT, true);
            final JSONObject markersPluginState = getMarkersPluginState(mapfullState);
            final JSONArray existing = getMarkersFromPluginState(markersPluginState);
            if(existing == null) {
                log.warn("Couldn't create markers array");
                return false;
            }
            existing.put(centerMarker);
        }
        return false;
    }
    
}
