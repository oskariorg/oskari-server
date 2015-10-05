package fi.nls.oskari.control.layer;


import com.fasterxml.jackson.databind.ObjectMapper;
import fi.mml.portti.domain.permissions.WFSLayerPermissionsStore;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Checks which layers the user has permission to view and writes the layer ids to Redis as JSON.
 */
@OskariActionRoute("GetLayerIds")
public class GetLayerIds extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetLayerIds.class);

    private static final String LAYER_IDS = "layerIds";
    private static final String ID = "id";

    private static final List<Integer> extra_layers = new ArrayList<Integer>();

    private static PermissionsService permissionsService = new PermissionsServiceIbatisImpl();

    @Override
    public void init() {
        super.init();
        final String[] properties = {
                GetWFSLayerConfigurationHandler.ANALYSIS_BASELAYER_ID,
                GetWFSLayerConfigurationHandler.USERLAYER_BASELAYER_ID,
                GetWFSLayerConfigurationHandler.MYPLACES_BASELAYER_ID
        };
        for(String prop: properties) {
            final String property = PropertyUtil.getOptional(prop);
            int id = ConversionHelper.getInt(property, -1);
            if(id != -1) {
                extra_layers.add(id);
            }
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        String result = null;

        String jsessionid = params.getRequest().getSession().getId();
        log.debug("Getting layerIds for session:", jsessionid);

        // check cache
	    boolean cache = ConversionHelper.getBoolean(params.getHttpParam("no-cache"), false);
	    if(cache) {
	    	result = WFSLayerPermissionsStore.getCache(jsessionid);
	    	log.debug("permissions cache:", result);
	        if(result != null) {
	        	ResponseHelper.writeResponse(params, result);
	        	return;
	        }
	    }

        List<Map<String,Object>> listOfLayers = permissionsService.getListOfMaplayerIdsForViewPermissionByUser(params.getUser(), true);
        ObjectMapper mapper = new ObjectMapper();

        try {
            List<Integer> idList = new ArrayList<Integer>();
            for (Map<String,Object> entry : listOfLayers) {
                idList.add(Integer.parseInt(String.valueOf(entry.get(ID))));
            }
            idList.addAll(extra_layers);

            Map<String,List<Integer>> layerIds = new HashMap<String,List<Integer>>();
            layerIds.put(LAYER_IDS, idList);
            result = mapper.writeValueAsString(layerIds);

            // put to cache
            log.debug("saving session:", jsessionid);
	        WFSLayerPermissionsStore permissions = WFSLayerPermissionsStore.setJSON(result);
	        permissions.save(jsessionid);

        } catch (Exception e) {
            log.error(e);
            result = JSONHelper.createJSONObject("error", e.toString()).toString();
        }

        ResponseHelper.writeResponse(params, result);
    }
}
