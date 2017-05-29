package fi.nls.oskari.control.layer;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.userlayer.service.UserLayerDataService;
import fi.nls.oskari.map.userlayer.service.UserLayerDbService;
import fi.nls.oskari.map.userlayer.service.UserLayerDbServiceMybatisImpl;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Returns users user data layers as JSON.
 */
@OskariActionRoute("GetUserLayers")
public class GetUserLayersHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetUserLayersHandler.class);
    private static final UserLayerDbService userLayerService = new UserLayerDbServiceMybatisImpl();
    private final UserLayerDataService userLayerDataService = new UserLayerDataService();

    private static final String JSKEY_USERLAYERS = "userlayers";
    private static final String USERLAYER_BASELAYER_ID = "userlayer.baselayer.id";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final JSONObject response = new JSONObject();
        final JSONArray layers = new JSONArray();
        JSONHelper.putValue(response, JSKEY_USERLAYERS, layers);

        final User user = params.getUser();
        if (!user.isGuest()) {
            final List<UserLayer> list = userLayerService.getUserLayerByUuid(user.getUuid());
            final OskariLayer baseLayer = userLayerDataService.getBaseLayer();
            for (UserLayer ul : list) {
                // Parse userlayer data to userlayer
                final JSONObject userLayer = userLayerDataService.parseUserLayer2JSON(ul, baseLayer);
                JSONObject permissions = OskariLayerWorker.getAllowedPermissions();
                JSONHelper.putValue(userLayer, "permissions", permissions);
                layers.put(userLayer);
            }
        }

        ResponseHelper.writeResponse(params, response);
    }
}
