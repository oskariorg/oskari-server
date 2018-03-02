package fi.nls.oskari.control.layer;

import static fi.nls.oskari.control.ActionConstants.PARAM_LANGUAGE;
import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.util.ConversionHelper;
import org.json.JSONArray;
import org.json.JSONException;
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

	private static final String KEY_GROUPS = "groups";
    private OskariMapLayerGroupService oskariMapLayerGroupService;

    public void setOskariMapLayerGroupService(final OskariMapLayerGroupService service) {
        oskariMapLayerGroupService = service;
    }

    @Override
    public void init() {
        // setup service if it hasn't been initialized
        if(oskariMapLayerGroupService == null) {
            setOskariMapLayerGroupService(new OskariMapLayerGroupServiceIbatisImpl());
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        log.debug("Getting layer groups");
        JSONArray json = getGroupJSON(-1,params, 0);
        log.debug("Got layer groups");
        ResponseHelper.writeResponse(params, json);
    }


    /**
     * Get group json, max depth is 3
     * @param parentId parent id
     * @param params params
     * @param depth current depth
     * @return
     * @throws ActionException
     */
    private JSONArray getGroupJSON(int parentId, ActionParameters params, int depth) throws ActionException {
        final String lang = params.getHttpParam(PARAM_LANGUAGE, params.getLocale().getLanguage());
        List<MaplayerGroup> layerGroups = oskariMapLayerGroupService.findByParentId(parentId);
        JSONArray json = new JSONArray();
        depth++;
        try{
            // Loop groups and their subgroups (max depth is 3)
            for(MaplayerGroup group : layerGroups) {
                final JSONObject layers = OskariLayerWorker.getListOfMapLayersByIdList(oskariMapLayerGroupService.findMaplayersByGroup(group.getId()), params.getUser(), lang, params.getHttpParam(PARAM_SRS));
                JSONArray layerList = layers.optJSONArray(OskariLayerWorker.KEY_LAYERS);
                group.setLayers(layerList);

                if(depth<=3) {
                    JSONObject groupJson = group.getAsJSON();
                    JSONArray subGroupsJSON = getGroupJSON(group.getId(), params, depth);
                    groupJson.put(KEY_GROUPS, subGroupsJSON);
                    json.put(groupJson);
                }
            }
        } catch(JSONException ex) {
            throw new ActionException("Cannot get groupped layerlist", ex);
        }
        return json;
    }


}
