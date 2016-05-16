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
    private static final String KEY_DECIMAL_SEPARATOR = "decimalSeparator";
    private static final String KEY_SUPPORTED_LOCALES = "locales";

    public static JSONObject getEnvironmentJSON(ActionParameters params) {
        final JSONObject env = new JSONObject();
        JSONHelper.putValue(env, ActionConstants.PARAM_LANGUAGE, params.getLocale().getLanguage());

        JSONHelper.putValue(env, KEY_SUPPORTED_LOCALES, PropertyUtil.getSupportedLocales());
        final DecimalFormatSymbols dfs = new DecimalFormatSymbols(params.getLocale());
        JSONHelper.putValue(env, KEY_DECIMAL_SEPARATOR, Character.toString(dfs.getDecimalSeparator()));
        return env;
    }

}
