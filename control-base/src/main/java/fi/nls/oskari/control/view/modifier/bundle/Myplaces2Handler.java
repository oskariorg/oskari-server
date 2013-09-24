package fi.nls.oskari.control.view.modifier.bundle;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.log.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.log.Logger;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;

@OskariViewModifier("myplaces2")
public class Myplaces2Handler extends BundleHandler {
    
    private static final Logger log = LogFactory.getLogger(Myplaces2Handler.class);

    /**
     * Updates the query URL from the myplaces2 configuration
     * @param params
     * @return
     * @throws ModifierException
     */
    public boolean modifyBundle(final ModifierParams params) throws ModifierException {
        final JSONObject myplacesConfig = getBundleConfig(params.getConfig());
        if (myplacesConfig == null || !myplacesConfig.has("queryUrl")) {
            return false;
        }
        final String ajaxUrl = params.getBaseAjaxUrl() + params.getAjaxRouteParamName() + "=MyPlaces";
        try {
            myplacesConfig.putOpt("queryUrl", ajaxUrl);
        } catch (JSONException ignored) {
            log.warn("Unable to replace", getName(), "conf.queryUrl to ",
                    ajaxUrl);
        }
        return false;
    }
}
