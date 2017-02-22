package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.view.modifier.ParamHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONException;
import org.json.JSONObject;

@OskariViewModifier("statsgridfilter")
public class StatsgridFilterParamHandler extends ParamHandler {

    private static final Logger log = LogFactory.getLogger(StatsgridFilterParamHandler.class);
    private static final String PARAM_REGIONS = "municipalities";
    private static final String PARAM_FILTERREGIONS = "filterRegions";
    private static final String PARAM_FILTERMETHOD = "filterMethod";
    private static final String PARAM_FILTERINPUT = "filterInput";

    public boolean handleParam(final ModifierParams params) throws ModifierException {
    	final String paramValues = params.getParamValue();
        if(paramValues == null) {
            return false;
        }
        log.debug("StatsgridFilterParamHandler got:", paramValues, "\n");

    	final String[] parts = paramValues.split("=");

    	// parse values
        try {
            // final JSONObject statsgridFilterState = new JSONObject();
            final JSONObject statsgridFilterState = getBundleState(params.getConfig(), BUNDLE_STATSGRID);
            if (parts.length > 0) {
                final String[] regions = parts[0].split(",");
                statsgridFilterState.put(PARAM_REGIONS, regions);
            }

            if (parts.length > 1) {
                final String[] filterRegion = parts[1].split(",");
                statsgridFilterState.put(PARAM_FILTERREGIONS, filterRegion);
            }

            if (parts.length > 3) {
                final String filterMethod = parts[2];
                final String[] filterInput = parts[3].split(",");
                statsgridFilterState.put(PARAM_FILTERMETHOD, filterMethod);
                statsgridFilterState.put(PARAM_FILTERINPUT, filterInput);
            }
        } catch (JSONException je) {
            throw new ModifierException("Could not replace statsgrid filter state!");
        }
        return false;
    }

}
