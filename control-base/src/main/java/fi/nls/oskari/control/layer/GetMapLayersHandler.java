package fi.nls.oskari.control.layer;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import static fi.nls.oskari.control.ActionConstants.*;

/**
 * Get all map layers registered in Oskari database
 */
@OskariActionRoute("GetMapLayers")
public class GetMapLayersHandler extends ActionHandler {

    private static Logger log = LogFactory.getLogger(GetMapLayersHandler.class);

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final String lang = params.getHttpParam(PARAM_LANGUAGE, params.getLocale().getLanguage());

        log.debug("Getting layers");
        final JSONObject layers = OskariLayerWorker.getListOfAllMapLayers(params.getUser(), lang);
        JSONArray list = layers.optJSONArray(OskariLayerWorker.KEY_LAYERS);
        // transform WKT for layers now that we know SRS
        for(int i = 0; i < list.length(); ++i) {
            OskariLayerWorker.transformWKTGeom(list.optJSONObject(i), params.getHttpParam(PARAM_SRS));
        }
        log.debug("Got layers");
        ResponseHelper.writeResponse(params, layers);
    }
}
