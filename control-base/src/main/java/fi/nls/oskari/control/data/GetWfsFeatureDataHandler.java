package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.Layer;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.data.service.GetGeoPointDataService;
import fi.nls.oskari.map.data.service.WFSFeatureService;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

@OskariActionRoute("GetWfsFeatureData")
public class GetWfsFeatureDataHandler extends ActionHandler {

    private final OskariLayerService mapLayerService = new OskariLayerServiceIbatisImpl();
    private final WFSFeatureService wfsFeatureService = new WFSFeatureService();

    private Logger log = LogFactory.getLogger(GetWfsFeatureDataHandler.class);

    private static final String PARAM_LAYERS = "layerIds";
    private static final String PARAM_MODE = "mode";
    private static final String PARAM_ZOOM = "zoom";
    private static final String PARAM_GEOJSON = "geojson";
    private static final String MODE_VALUE_FEATURES = "features";
    private static final String MODE_VALUE_DATA_TO_TABLE = "data_to_table";
    public static final String FLOW_PM_MAP_WFS_QUERY_ID = "flow_pm_map_wfs_query_id";
    private static final List<String> ACCEPTED_MODES = new ArrayList<String>();


    @Override
    public void init() {
        ACCEPTED_MODES.add(MODE_VALUE_FEATURES);
        ACCEPTED_MODES.add(MODE_VALUE_DATA_TO_TABLE);

    }

    @Override
    public void handleAction(final ActionParameters params)
            throws ActionException {

        final String layerIds = params.getHttpParam(PARAM_LAYERS);
        final String[] layerIdsArr = layerIds.split(",");
        boolean only_one_layer = false;

        final int zoom = ConversionHelper
                .getInt(params.getHttpParam(PARAM_ZOOM), 0);
        final String mode = getMode(params
                .getHttpParam(PARAM_MODE, "features"));
        final String wfsQueryId = params.getHttpParam(FLOW_PM_MAP_WFS_QUERY_ID, "");

        final JSONArray data = new JSONArray();
        JSONObject geojs = new JSONObject();
        try {

            geojs = new JSONObject(params.getHttpParam(PARAM_GEOJSON, "{}"));

        } catch (JSONException ee) {
            log.warn("Couldn't parse geojson from POST request", ee);
        }
        if (!geojs.has("features")) {
            throw new ActionException(
                    "Valid GeoJson must be in parameter geojson:");
        }
        JSONObject rootJson = new JSONObject();
        for (String id : layerIdsArr) {
            if (id.indexOf('_') >= 0) {
                continue;
            }

            final int layerId = ConversionHelper.getInt(id, -1);
            if (layerId == -1) {
                log.warn("Couldnt parse layer id", id);
                continue;
            }

            final OskariLayer layer = mapLayerService.find(layerId);
            final String layerType = layer.getType();

            if (Layer.TYPE_WFS.equals(layerType)) {
                JSONArray features = null;
                try {
                    // Geojson geometry is used for selecting features
                    // default tolerance (buffer radius) is based on zoom
                    features = wfsFeatureService.getWFSFeatures(zoom, geojs,
                            layerId);
                    if (mode.equals(MODE_VALUE_FEATURES)) {
                        final JSONObject layerJson = new JSONObject();
                        layerJson.put(GetGeoPointDataService.TYPE, layerType);
                        layerJson.put(GetGeoPointDataService.LAYER_ID, layerId);
                        layerJson.put("features", features);
                        data.put(layerJson);
                    } else {
                        if (!only_one_layer) {
                            // Only for one layer
                            rootJson = GetFeaturesForTable(features, wfsQueryId);
                        }
                    }
                } catch (JSONException je) {
                    log.warn("Could not add features", features,
                            "- for layerId", layerId);
                }
            }
        }

        try {

            if (mode.equals(MODE_VALUE_FEATURES)) {
                rootJson.put("data", data);
                rootJson.put("layerCount", data.length());
            }

            ResponseHelper.writeResponse(params, rootJson);
        } catch (JSONException je) {
            throw new ActionException("Could not populate GFI JSON: "
                    + log.getAsString(data), je);
        }
    }

    private JSONObject GetFeaturesForTable(JSONArray data, String wfsQueryId) {
        JSONObject rootJson = new JSONObject();
        JSONObject headerJson = new JSONObject();
        //Map<String, String> allHeaders = new HashMap<String, String> ();

        List<String> allHeaders = new ArrayList<String>();

        Map<String, Integer> headersWidth = new HashMap<String, Integer>();


        headersWidth.put("pnr:PaikanNimi", new Integer(300));  // ???

        /* Do headers */
        headerJson = new JSONObject();
        try {
            rootJson.put("featureDatas", data);
            headerJson.put("header", "Feature");
            headerJson.put("width", new Integer(100));
            headerJson.put("dataIndex", "feature");
            rootJson.accumulate("headers", headerJson);
            Iterator<String> headersIter = allHeaders.iterator();// keySet().iterator();

            while (headersIter.hasNext()) {
                String headerName = headersIter.next();
                //headerName = headerName.replace(":", "_");
                //System.out.println(headerName);
                headerJson = new JSONObject();
                headerJson.put("header", headerName);
                if (headersWidth.containsKey(headerName)) {
                    headerJson.put("width", headersWidth.get(headerName));
                } else {
                    headerJson.put("width", new Integer(100));
                }
                headerJson.put("dataIndex", headerName.toLowerCase().replaceAll("\\s", "_").replaceAll(":", "_"));
                rootJson.accumulate("headers", headerJson);
            }
            /** Add wfs query id  */
            rootJson.put("wfsQueryId", wfsQueryId);
        } catch (JSONException e) {
            log.warn("Failed to create JSON for table", e);
        }


        return rootJson;
    }

    private String getMode(final String mode) throws ActionException {
        final String lowerCaseMode = mode.toLowerCase();
        if (!ACCEPTED_MODES.contains(lowerCaseMode)) {
            throw new ActionException("Unknown mode  requested "
                    + mode);
        }
        return lowerCaseMode;
    }

}
