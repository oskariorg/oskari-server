package fi.nls.oskari.control.layer;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.mml.portti.domain.permissions.WFSLayerPermissionsStore;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks which layers the user has permission to view and writes the layer ids to Redis as JSON.
 */
@OskariActionRoute("GetLayerIds")
public class GetLayerIds extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetLayerIds.class);

    private static final String LAYER_IDS = "layerIds";

    private final List<Integer> extra_layers = new ArrayList<>();

    @Override
    public void init() {
        super.init();
        final String[] properties = {
                GetWFSLayerConfigurationHandler.ANALYSIS_BASELAYER_ID,
                GetWFSLayerConfigurationHandler.USERLAYER_BASELAYER_ID,
                GetWFSLayerConfigurationHandler.MYPLACES_BASELAYER_ID
        };
        for (String prop : properties) {
            final String property = PropertyUtil.getOptional(prop);
            int id = ConversionHelper.getInt(property, -1);
            if (id != -1) {
                extra_layers.add(id);
            }
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        String jsessionid = params.getRequest().getSession().getId();
        log.debug("Getting layerIds for session:", jsessionid);

        // check cache
        boolean cache = ConversionHelper.getBoolean(params.getHttpParam("no-cache"), false);
        if (cache) {
            String result = WFSLayerPermissionsStore.getCache(jsessionid);
            log.debug("permissions cache:", result);
            if (result != null) {
                ResponseHelper.writeResponse(params, result);
                return;
            }
        }

        List<OskariLayer> layers = OskariLayerWorker.getLayersForUser(params.getUser(), true);

        try {
            // get layerIds
            List<Integer> availableLayerIds = layers.stream()
                    .map(OskariLayer::getId).collect(Collectors.toList());
            // add user content (internal) base layers
            availableLayerIds.addAll(extra_layers);
            // put to cache
            log.debug("saving session:", jsessionid);
            WFSLayerPermissionsStore permissions = new WFSLayerPermissionsStore();
            permissions.setLayerIds(availableLayerIds);
            permissions.save(jsessionid);
            // transport assumes that the response can be given to WFSLayerPermissionsStore.setJSON()
            ResponseHelper.writeResponse(params, permissions.getAsJSON());

        } catch (Exception e) {
            log.error(e, "Error writing layer id list");
            ResponseHelper.writeResponse(params, JSONHelper.createJSONObject("error", e.toString()).toString());
        }

    }
}
