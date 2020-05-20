package fi.nls.oskari.control.layer;

import fi.nls.oskari.analysis.AnalysisHelper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.service.AnalysisDataService;
import fi.nls.oskari.map.analysis.service.AnalysisDbService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.PermissionType;
import org.oskari.permissions.model.ResourceType;

import java.util.List;
import java.util.Set;

/**
 * Returns users own analysis layers as JSON.
 */
@OskariActionRoute("GetAnalysisLayers")
public class GetAnalysisLayersHandler extends ActionHandler {

    private static final Logger LOG = LogFactory.getLogger(GetAnalysisLayersHandler.class);
    private AnalysisDbService analysisService;
    private PermissionService permissionsService;

    private static final String JSKEY_ANALYSISLAYERS = "analysislayers";

    @Override
    public void init() {
        super.init();
        permissionsService = OskariComponentManager.getComponentOfType(PermissionService.class);
        analysisService = OskariComponentManager.getComponentOfType(AnalysisDbService.class);
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();

        final JSONObject response = new JSONObject();
        final JSONArray layers = new JSONArray();
        JSONHelper.putValue(response, JSKEY_ANALYSISLAYERS, layers);

        final User user = params.getUser();
        final List<Analysis> list = analysisService.getAnalysisByUid(user.getUuid());
        Set<String> publishPermission = permissionsService.getResourcesWithGrantedPermissions(ResourceType.analysislayer, user, PermissionType.PUBLISH);
        Set<String> downloadPermission = permissionsService.getResourcesWithGrantedPermissions(ResourceType.analysislayer, user, PermissionType.DOWNLOAD);
        JSONObject baseOptions = AnalysisDataService.getBaseLayer().getOptions();
        for(Analysis a: list) {
            // Parse analyse layer json out analysis
            final JSONObject analysisLayer = AnalysisHelper.getlayerJSON(a, baseOptions);
            final String permissionKey = "analysis+" + a.getId();

            JSONHelper.putValue(analysisLayer, "permissions",
                    AnalysisHelper.getAnalysisPermissions(
                            publishPermission.contains(permissionKey),
                            downloadPermission.contains(permissionKey)));
            layers.put(analysisLayer);
        }

        ResponseHelper.writeResponse(params, response);
    }
}
