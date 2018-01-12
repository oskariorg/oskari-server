package fi.nls.oskari.control.layer;

import static fi.nls.oskari.control.ActionConstants.PARAM_LANGUAGE;
import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupServiceIbatisImpl;
import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ResponseHelper;

/**
 * Get all map layer groups registered in Oskari database
 */
@OskariActionRoute("GetHierarchicalMapLayerGroups")
public class GetMapLayerGroupsHandler extends ActionHandler {
	
	private static Logger log = LogFactory.getLogger(GetMapLayerGroupsHandler.class);

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final String lang = params.getHttpParam(PARAM_LANGUAGE, params.getLocale().getLanguage());

        log.debug("Getting layer groups");
        OskariMapLayerGroupService groupService = new OskariMapLayerGroupServiceIbatisImpl();
        List<MaplayerGroup> layerGroups = groupService.findAll();
        JSONArray json = new JSONArray();
        for(MaplayerGroup group : layerGroups) {
        	List<Integer> intLayerIds = groupService.findMaplayersByGroup(group.getId());
        	List<String> strLayerIds = new ArrayList<>();
			for(Integer current : intLayerIds){
				strLayerIds.add(current.toString());
			}
			final JSONObject layers = OskariLayerWorker.getListOfMapLayersById(strLayerIds, params.getUser(), lang);
			JSONArray list = layers.optJSONArray(OskariLayerWorker.KEY_LAYERS);
	        // transform WKT for layers now that we know SRS
//	        for(int i = 0; i < list.length(); ++i) {
//	            OskariLayerWorker.transformWKTGeom(list.optJSONObject(i), params.getHttpParam(PARAM_SRS));
//	        }
	        group.setLayers(list);
	        json.put(group.getAsJSON());
        }
        log.debug("Got layer groups");
        ResponseHelper.writeResponse(params, json);
    }
}
