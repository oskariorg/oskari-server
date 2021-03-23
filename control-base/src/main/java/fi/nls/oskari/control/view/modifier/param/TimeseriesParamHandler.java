package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import fi.nls.oskari.view.modifier.ParamHandler;
import org.json.JSONArray;
import org.json.JSONObject;

@OskariViewModifier("timeseries")
public class TimeseriesParamHandler extends ParamHandler {
    private static final String KEY_VALUE = "value";

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        String paramValue = params.getParamValue();
        if (paramValue == null) {
            return false;
        }
        final JSONObject bundleState = getBundleState(params.getConfig(), BUNDLE_TIMESERIES);
        final String[] timeProps = paramValue.split("/");
        if (timeProps.length != 2) {
            JSONHelper.putValue(bundleState, KEY_VALUE, Integer.parseInt(paramValue));
            return false;
        }
        JSONArray time = new JSONArray();
        time.put(Integer.parseInt(timeProps[0]));
        time.put(Integer.parseInt(timeProps[1]));
        JSONHelper.putValue(bundleState, KEY_VALUE, time);
        return false;
    }
}
