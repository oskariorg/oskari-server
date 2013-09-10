package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.GetWMSCapabilities;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;
import java.util.Iterator;

/**
 * Get WMS capabilites and return JSON
 */
@OskariActionRoute("GetWSCapabilities")
public class GetWSCapabilitiesHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetWSCapabilitiesHandler.class);
    private static final String PARM_KEY = "key";
    private static final String PARM_WMSURL = "wmsurl";

    private String[] permittedRoles = new String[0];

    @Override
    public void init() {
        super.init();
        permittedRoles = PropertyUtil.getCommaSeparatedList("actionhandler.GetWSCapabilitiesHandler.roles");
    }

    public void handleAction(ActionParameters params) throws ActionException {

        final String key = params.getHttpParam(PARM_KEY, "");
        final String wmsurl = params.getHttpParam(PARM_WMSURL, "");

        if (wmsurl.isEmpty()) {
            throw new ActionParamsException("Parameter 'wmsurl' missing");
        }
        if (!params.getUser().hasAnyRoleIn(permittedRoles)) {
            throw new ActionDeniedException("Unauthorized user tried to get wmsservices");
        }
        final String response = GetWMSCapabilities.getResponse(wmsurl);
        final JSONObject capabilities = GetWMSCapabilities.parseCapabilities(response);
        if (key != null && !key.isEmpty()) {
            // return a subset
            ResponseHelper.writeResponse(params, findsubJson(key, capabilities));
        } else {
            ResponseHelper.writeResponse(params, capabilities);
        }
    }
    
    private JSONObject findsubJson(String mykey, JSONObject js) {
        try {
            if (js == null)
                return null;
            Iterator<?> keys = js.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (js.get(key) instanceof JSONObject) {
                    JSONObject jssub = js.getJSONObject(key);
                    if (mykey.toUpperCase().equals(key.toUpperCase())) {
                        return jssub;
                    } else {
                        JSONObject jssub2 = findsubJson(mykey, jssub);
                        if (jssub2 != null)
                            return jssub2;
                    }

                }
            }
        } catch (JSONException e) {
            log.warn(e, "JSON parse failed");
        }
        return null;

    }
}
