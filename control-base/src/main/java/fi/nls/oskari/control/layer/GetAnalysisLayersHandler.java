package fi.nls.oskari.control.layer;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.map.analysis.service.AnalysisDataService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.domain.User;
import org.json.JSONArray;
import org.json.JSONObject;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.Set;


/**
 * Get WMS map layers
 */
@OskariActionRoute("GetAnalysisLayers")
public class GetAnalysisLayersHandler extends ActionHandler {
    private static final Logger log = LogFactory
            .getLogger(GetAnalysisLayersHandler.class);

    final static String LANGUAGE_ATTRIBUTE = "lang";

    private AnalysisDataService analysisDataService = new AnalysisDataService();
    private static PermissionsService permissionsService = new PermissionsServiceIbatisImpl();

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        try {

            final String lang = params.getHttpParam(LANGUAGE_ATTRIBUTE, params
                    .getLocale().getLanguage());

            User user = params.getUser();
            JSONObject layers = new JSONObject();
            if (!user.isGuest()) {
                layers = analysisDataService.getListOfAllAnalysisLayers(
                        user.getUuid(), lang);
                JSONArray analysisLayers = layers.getJSONArray("analysislayers");
                int nLayers = analysisLayers.length();
                for (int i=0; i < nLayers; i++) {
                    JSONObject analysisLayer =  analysisLayers.getJSONObject(i);
                    Set<String> permissionsList = permissionsService.getPublishPermissions(AnalysisLayer.TYPE);
                    Set<String> editAccessList = null;
                    String permissionKey = "analysis+"+analysisLayer.getString("id");
                    JSONObject permissions = OskariLayerWorker.getPermissions(params.getUser(), permissionKey, permissionsList, editAccessList);
                    JSONHelper.putValue(analysisLayer, "permissions", permissions);
                }


            }

            ResponseHelper.writeResponse(params, layers);

        } catch (Exception e) {
            throw new ActionException(
                    "Couldn't request Analysis data service - get analysis layers",
                    e);
        }
    }

}
