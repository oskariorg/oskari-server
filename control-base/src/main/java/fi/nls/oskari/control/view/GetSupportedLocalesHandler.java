package fi.nls.oskari.control.view;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * @deprecated GetAppSetup provides this information
 */
@Deprecated
@OskariActionRoute("GetSupportedLocales")
public class GetSupportedLocalesHandler extends ActionHandler {
    private static final String KEY_DECIMAL_SEPARATORS = "decimalSeparators";
    private static final String KEY_SUPPORTED_LOCALES = "supportedLocales";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        JSONObject ret = new JSONObject();
        String[] locales = PropertyUtil.getSupportedLocales();
        String[] localeParts;
        JSONObject separators = new JSONObject();

        Locale l;
        DecimalFormatSymbols dfs;

        for (int i = 0; i < locales.length; i++) {
            //try {
                localeParts = locales[i].split("_");
                if (localeParts.length > 1) {
                    l = new Locale(localeParts[0], localeParts[1]);
                } else {
                    l = new Locale(localeParts[0]);
                }
                dfs = new DecimalFormatSymbols(l);
                JSONHelper.putValue(
                        separators,
                        locales[i],
                        Character.toString(dfs.getDecimalSeparator())
                );
        }
        try {
            ret.put(
                    KEY_SUPPORTED_LOCALES,
                    locales
            );
            ret.put(
                    KEY_DECIMAL_SEPARATORS,
                    separators
            );
        } catch (JSONException je) {
            throw new ActionException("Couldn't build JSON from supported locales: ", je);
        }
        ResponseHelper.writeResponse(
                params,
                ret
        );
    }
}
