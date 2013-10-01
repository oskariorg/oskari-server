package fi.nls.oskari.control.view;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: TMIKKOLAINEN
 * Date: 27.9.2013
 * Time: 11:17
 */
@OskariActionRoute("GetSupportedLocales")
public class GetSupportedLocalesHandler extends ActionHandler {
    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        try {
            ResponseHelper.writeResponse(
                    params,
                    new JSONObject().put(
                            "supportedLocales",
                            new JSONArray(PropertyUtil.getSupportedLocales())
                    )
            );
        } catch (JSONException je) {
            throw new ActionException("Couldn't build JSON from supported locales: ", je);
        }
    }
}
