package fi.nls.oskari.util;

import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionParameters;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by SMAKINEN on 16.5.2016.
 */
public class EnvHelper {
    private static final String KEY_DECIMAL_SEPARATORS = "decimalSeparators";
    private static final String KEY_SUPPORTED_LOCALES = "locales";

    public static JSONObject getEnvironmentJSON(ActionParameters params) {
        final JSONObject env = new JSONObject();
        JSONHelper.putValue(env, ActionConstants.PARAM_LANGUAGE, params.getLocale().getLanguage());
        final JSONArray localesJSON = new JSONArray();
        final JSONObject separators = new JSONObject();
        JSONHelper.putValue(env, KEY_SUPPORTED_LOCALES, localesJSON);
        JSONHelper.putValue(env, KEY_DECIMAL_SEPARATORS, separators);

        final String[] locales = PropertyUtil.getSupportedLocales();
        for (String loc : locales) {
            final Locale l = getLocale(loc);
            final DecimalFormatSymbols dfs = new DecimalFormatSymbols(l);
            localesJSON.put(l.toString());
            JSONHelper.putValue(separators, l.toString(), Character.toString(dfs.getDecimalSeparator()));
        }
        return env;
    }

    private static Locale getLocale(String loc) {
        final String[] localeParts = loc.split("_");
        if (localeParts.length > 1) {
            return new Locale(localeParts[0], localeParts[1]);
        }
        return new Locale(localeParts[0]);
    }

}
