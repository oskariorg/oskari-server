package fi.nls.oskari.control.layer;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.analysis.AnalysisHelper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.map.analysis.service.AnalysisDbService;
import fi.nls.oskari.map.analysis.service.AnalysisDbServiceMybatisImpl;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

/**
 * Returns users own analysis layers as JSON.
 */
@OskariActionRoute("GetAnalysisLayers")
public class GetAnalysisLayersHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetAnalysisLayersHandler.class);
    private static final AnalysisDbService analysisService = new AnalysisDbServiceMybatisImpl();
    private static PermissionsService permissionsService = new PermissionsServiceIbatisImpl();

    private static final String JSKEY_ANALYSISLAYERS = "analysislayers";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final JSONObject response = new JSONObject();
        final JSONArray layers = new JSONArray();
        JSONHelper.putValue(response, JSKEY_ANALYSISLAYERS, layers);

        final User user = params.getUser();
        if (!user.isGuest()) {
            // FIXME: make a new method to permission service for a more specific search, this will blow up eventually
            final Set<String> permissionsList = permissionsService.getPublishPermissions(AnalysisLayer.TYPE);
            final Set<String> downloadPermissionsList = permissionsService.getDownloadPermissions(AnalysisLayer.TYPE);
            final Set<String> editAccessList = null;
            final List<Analysis> list = analysisService.getAnalysisByUid(user.getUuid());
            for(Analysis a: list) {
                // Parse analyse layer json out analysis
                final JSONObject analysisLayer = AnalysisHelper.getlayerJSON(a);
                final String permissionKey = "analysis+" + a.getId();
                JSONObject permissions = OskariLayerWorker.getPermissions(user, permissionKey, permissionsList, downloadPermissionsList, editAccessList);
                JSONHelper.putValue(analysisLayer, "permissions", permissions);
                layers.put(analysisLayer);
            }
        }

        ResponseHelper.writeResponse(params, response);
    }
}
