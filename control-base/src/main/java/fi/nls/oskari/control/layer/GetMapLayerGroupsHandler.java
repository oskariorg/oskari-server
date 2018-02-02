package fi.nls.oskari.control.layer;

import static fi.nls.oskari.control.ActionConstants.PARAM_LANGUAGE;
import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

import javax.xml.stream.XMLStreamException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupServiceIbatisImpl;
import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheServiceMybatisImpl;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilitiesHelper;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.wmts.WMTSCapabilitiesParser;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;

/**
 * Get all map layer groups registered in Oskari database
 */
@OskariActionRoute("GetHierarchicalMapLayerGroups")
public class GetMapLayerGroupsHandler extends ActionHandler {
	
	private static Logger log = LogFactory.getLogger(GetMapLayerGroupsHandler.class);

	private final static String KEY_GROUPS = "groups";
	private final static String KEY_PARENT_ID = "parentId";
	private final static String KEY_ID = "id";
	
	private final OskariLayerService layerService = new OskariLayerServiceIbatisImpl();
    private final CapabilitiesCacheService capabilitiesCacheService = new CapabilitiesCacheServiceMybatisImpl();


    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final String lang = params.getHttpParam(PARAM_LANGUAGE, params.getLocale().getLanguage());

        log.debug("Getting layer groups");
        OskariMapLayerGroupService groupService = new OskariMapLayerGroupServiceIbatisImpl();
        List<MaplayerGroup> layerGroups = groupService.findAll();

        // Get main groups
        List<MaplayerGroup> mainGroups = layerGroups.stream()
                .filter(g -> g.getParentId() == -1).collect(Collectors.toList());

        JSONArray json = new JSONArray();
        try{
//        	layerService.findAll().stream()
//            .filter(layer -> canUpdate(layer.getType()))
//            .filter(layer -> shouldUpdate(layer))
//            .collect(groupingBy(layer -> new UrlTypeVersion(layer)))
//            .forEach((utv, layers) -> updateCapabilities(utv, layers));
            for(MaplayerGroup group : mainGroups) {
                List<Integer> intLayerIds = groupService.findMaplayersByGroup(group.getId());
                List<String> strLayerIds = new ArrayList<>();
                for(Integer current : intLayerIds){
                    strLayerIds.add(current.toString());
                }
                final JSONObject layers = OskariLayerWorker.getListOfMapLayersById(strLayerIds, params.getUser(), lang, params.getHttpParam(PARAM_SRS));
                JSONArray layerList = layers.optJSONArray(OskariLayerWorker.KEY_LAYERS);
                // transform WKT for layers now that we know SRS
//                for(int i = 0; i < layerList.length(); ++i) {
//                	System.out.println(layerList.optJSONObject(i).toString(4));
//                    OskariLayerWorker.transformWKTGeom(layerList.optJSONObject(i), params.getHttpParam(PARAM_SRS));
//                }

                group.setLayers(layerList);
                JSONObject groupJson = group.getAsJSON();
                JSONArray subGroupsJSON = new JSONArray();

                // Get main group subgroups
                List<MaplayerGroup> subgroups = layerGroups.stream()
                        .filter(g -> g.getParentId() == group.getId()).collect(Collectors.toList());
                for(MaplayerGroup subgroup: subgroups) {
                    List<Integer> intSubLayerIds = groupService.findMaplayersByGroup(subgroup.getId());
                    List<String> strSubLayerIds = new ArrayList<>();
                    for (Integer current : intSubLayerIds) {
                        strSubLayerIds.add(current.toString());
                    }
                    final JSONObject subLayers = OskariLayerWorker.getListOfMapLayersById(strSubLayerIds, params.getUser(), lang);
                    JSONArray subLayerList = subLayers.optJSONArray(OskariLayerWorker.KEY_LAYERS);
                    // transform WKT for layers now that we know SRS
//                    for (int i = 0; i < subLayerList.length(); ++i) {
//                    	System.out.println(subLayerList.optJSONObject(i).toString(4));
//                        OskariLayerWorker.transformWKTGeom(subLayerList.optJSONObject(i), params.getHttpParam(PARAM_SRS));
//                    }

                    subgroup.setLayers(subLayerList);
                    JSONObject subgroupJson = subgroup.getAsJSON();


                    // Get subgroup subgroups
                    List<MaplayerGroup> subgroupSubgroups = layerGroups.stream()
                            .filter(g -> g.getParentId() == subgroup.getId()).collect(Collectors.toList());
                    JSONArray subgroupSubgroupsJSON = new JSONArray();
                    for(MaplayerGroup subgroupSubgroup: subgroupSubgroups) {
                        List<Integer> intSubgroupLayerIds = groupService.findMaplayersByGroup(subgroupSubgroup.getId());
                        List<String> strSubgroupLayerIds = new ArrayList<>();
                        for (Integer current : intSubgroupLayerIds) {
                            strSubgroupLayerIds.add(current.toString());
                        }
                        final JSONObject subgroupLayers = OskariLayerWorker.getListOfMapLayersById(strSubgroupLayerIds, params.getUser(), lang);
                        JSONArray subgroupLayerList = subgroupLayers.optJSONArray(OskariLayerWorker.KEY_LAYERS);
                        // transform WKT for layers now that we know SRS
                        for (int i = 0; i < subgroupLayerList.length(); ++i) {
                            OskariLayerWorker.transformWKTGeom(subgroupLayerList.optJSONObject(i), params.getHttpParam(PARAM_SRS));
                        }

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

    protected static boolean canUpdate(String type) {
        switch (type) {
        case OskariLayer.TYPE_WMS:
        case OskariLayer.TYPE_WMTS:
            return true;
        default:
            return false;
        }
    }

    protected static boolean shouldUpdate(OskariLayer layer) {
        Date lastUpdated = layer.getCapabilitiesLastUpdated();
        if (lastUpdated == null) {
            log.debug("Should update layer:", layer.getId(), "last updated unknown");
            return true;
        }
        int rate = layer.getCapabilitiesUpdateRateSec();
        long nextUpdate = lastUpdated.getTime() + TimeUnit.SECONDS.toMillis(rate);
        long now = System.currentTimeMillis();
        log.debug("Layer:", layer.getId(), "next scheduled update is:", nextUpdate, "now is", now);
        if (nextUpdate <= now) {
            return true;
        }
        log.debug("Skipping layerId:", layer.getId(), "as recently updated");
        return false;
    }

    private void updateCapabilities(UrlTypeVersion utv, List<OskariLayer> layers) {
        final String url = utv.url;
        final String type = utv.type;
        final String version = utv.version;
        final String user = layers.get(0).getUsername();
        final String pass = layers.get(0).getPassword();

        int[] ids = layers.stream().mapToInt(OskariLayer::getId).toArray();
        log.debug("Updating Capabilities for a group of layers - url:", url,
                "type:", type, "version:", version, "ids:", Arrays.toString(ids));

        final String data;
        try {
            data = capabilitiesCacheService.getCapabilities(url, type, user, pass, version, true).getData();
        } catch (ServiceException e) {
        	log.warn(e, "Could not find get Capabilities, url:", url,
                    "type:", type, "version:", version, "ids:", Arrays.toString(ids));
            return;
        }

        switch (type) {
        case OskariLayer.TYPE_WMS:
            updateWMSLayers(layers, data);
            break;
        case OskariLayer.TYPE_WMTS:
            updateWMTSLayers(layers, data);
            break;
        }
    }

    private void updateWMSLayers(List<OskariLayer> layers, String data) {
        for (OskariLayer layer : layers) {
            WebMapService wms = OskariLayerCapabilitiesHelper.parseWMSCapabilities(data, layer);
            if (wms == null) {
            	log.warn("Failed to parse Capabilities for layerId:", layer.getId());
                continue;
            }
            OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWMS(wms, layer);
            layerService.update(layer);
        }
    }

    private void updateWMTSLayers(List<OskariLayer> layers, String data) {
        final WMTSCapabilities wmts;
        try {
            wmts = WMTSCapabilitiesParser.parseCapabilities(data);
        } catch (XMLStreamException | IllegalArgumentException e) {
            int[] ids = layers.stream().mapToInt(OskariLayer::getId).toArray();
            log.warn(e, "Failed to parse WMTS GetCapabilities ids:", Arrays.toString(ids));
            return;
        }

        for (OskariLayer layer : layers) {
            try {
                OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWMTS(wmts, layer, null);
                layerService.update(layer);
            } catch (IllegalArgumentException e) {
            	log.warn(e, "Failed to update layerId:", layer.getId());
            }
        }
    }

    private static class UrlTypeVersion {

        private final String url;
        private final String type;
        private final String version;

        private UrlTypeVersion(OskariLayer layer) {
            url = layer.getSimplifiedUrl(true);
            type = layer.getType();
            version = layer.getVersion();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof UrlTypeVersion)) {
                return false;
            }
            if (o == this) {
                return true;
            }
            UrlTypeVersion s = (UrlTypeVersion) o;
            return url.equals(s.url)
                    && type.equals(s.type)
                    && version.equals(s.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, type, version);
        }

    }
}
