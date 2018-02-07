package fi.nls.oskari.control.layer;

import static fi.nls.oskari.control.ActionConstants.PARAM_LANGUAGE;
import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final String lang = params.getHttpParam(PARAM_LANGUAGE, params.getLocale().getLanguage());

        log.debug("Getting layer groups");
        OskariMapLayerGroupService groupService = new OskariMapLayerGroupServiceIbatisImpl();
        List<MaplayerGroup> layerGroups = groupService.findAll();

        // Get main groups
        List<MaplayerGroup> mainGroups = layerGroups.stream()
                .filter(g -> g.getParentId() == -1)
                .sorted(Comparator.comparing(MaplayerGroup::getOrderNumber, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        JSONArray json = new JSONArray();
        try{
            for(MaplayerGroup group : mainGroups) {
                List<Integer> intLayerIds = groupService.findMaplayersByGroup(group.getId());
                List<String> strLayerIds = new ArrayList<>();
                for(Integer current : intLayerIds){
                    strLayerIds.add(current.toString());
                }
                final JSONObject layers = OskariLayerWorker.getListOfMapLayersById(strLayerIds, params.getUser(), lang, params.getHttpParam(PARAM_SRS));
                JSONArray layerList = layers.optJSONArray(OskariLayerWorker.KEY_LAYERS);

                group.setLayers(layerList);
                JSONObject groupJson = group.getAsJSON();
                JSONArray subGroupsJSON = new JSONArray();

                // Get main group subgroups
                List<MaplayerGroup> subgroups = layerGroups.stream()
                        .filter(g -> g.getParentId() == group.getId())
                		.sorted(Comparator.comparing(MaplayerGroup::getOrderNumber, Comparator.nullsLast(Comparator.naturalOrder())))
                        .collect(Collectors.toList());
                for(MaplayerGroup subgroup: subgroups) {
                    List<Integer> intSubLayerIds = groupService.findMaplayersByGroup(subgroup.getId());
                    List<String> strSubLayerIds = new ArrayList<>();
                    for (Integer current : intSubLayerIds) {
                        strSubLayerIds.add(current.toString());
                    }
                    final JSONObject subLayers = OskariLayerWorker.getListOfMapLayersById(strSubLayerIds, params.getUser(), lang);
                    JSONArray subLayerList = subLayers.optJSONArray(OskariLayerWorker.KEY_LAYERS);

                    subgroup.setLayers(subLayerList);
                    JSONObject subgroupJson = subgroup.getAsJSON();


                    // Get subgroup subgroups
                    List<MaplayerGroup> subgroupSubgroups = layerGroups.stream()
                            .filter(g -> g.getParentId() == subgroup.getId())
                    		.sorted(Comparator.comparing(MaplayerGroup::getOrderNumber, Comparator.nullsLast(Comparator.naturalOrder())))
                            .collect(Collectors.toList());
                    JSONArray subgroupSubgroupsJSON = new JSONArray();
                    for(MaplayerGroup subgroupSubgroup: subgroupSubgroups) {
                        List<Integer> intSubgroupLayerIds = groupService.findMaplayersByGroup(subgroupSubgroup.getId());
                        List<String> strSubgroupLayerIds = new ArrayList<>();
                        for (Integer current : intSubgroupLayerIds) {
                            strSubgroupLayerIds.add(current.toString());
                        }
                        final JSONObject subgroupLayers = OskariLayerWorker.getListOfMapLayersById(strSubgroupLayerIds, params.getUser(), lang);
                        JSONArray subgroupLayerList = subgroupLayers.optJSONArray(OskariLayerWorker.KEY_LAYERS);

                        subgroupSubgroup.setLayers(subgroupLayerList);
                        subgroupSubgroupsJSON.put(subgroupSubgroup.getAsJSON());
                    }
                    subgroupJson.put(KEY_GROUPS, subgroupSubgroupsJSON);
                    subGroupsJSON.put(subgroupJson);
                }

                groupJson.put(KEY_GROUPS, subGroupsJSON);
                json.put(groupJson);
            }
        } catch(JSONException ex) {
            log.error("Group layerlist error", ex);
            throw new ActionException("Group layerlist error");
        }

        log.debug("Got layer groups");
        ResponseHelper.writeResponse(params, json);
    }
}
