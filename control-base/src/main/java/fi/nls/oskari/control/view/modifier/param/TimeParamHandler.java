package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import fi.nls.oskari.view.modifier.ParamHandler;
import org.json.JSONException;
import org.json.JSONObject;

@OskariViewModifier("time")
public class TimeParamHandler extends ParamHandler {
    private static final String KEY_DATE = "date";
    private static final String KEY_DAY_TIME = "time";
    private static final String KEY_YEAR = "year";
    private static final String KEY_TIME = "timePoint";

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        String paramValue = params.getParamValue();
        if (paramValue == null) {
            return false;
        }
        final String[] timeProps = paramValue.split("_");
        if (timeProps.length < 2) {
            return false;
        }
        JSONObject time = new JSONObject();
        JSONHelper.putValue(time, KEY_DATE, timeProps[0]);
        JSONHelper.putValue(time, KEY_DAY_TIME, timeProps[1]);
        if (timeProps.length == 3) {
            try {
                int year = Integer.parseInt(timeProps[2]);
                JSONHelper.putValue(time, KEY_YEAR, year);
            } catch (NumberFormatException e) {
                // year is optional, do nothing
            }
        }
        try {
            final JSONObject mapfullState = getBundleState(params.getConfig(), BUNDLE_MAPFULL);
            mapfullState.put(KEY_TIME, time);
        } catch (JSONException je) {
            throw new ModifierException("Could not apply time parameters");
        }
        return false;
    }
}
