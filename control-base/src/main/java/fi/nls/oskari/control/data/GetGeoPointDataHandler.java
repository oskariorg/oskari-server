package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.data.domain.GFIRequestParams;
import fi.nls.oskari.map.data.domain.GFIRestQueryParams;
import fi.nls.oskari.map.data.service.GetGeoPointDataService;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static fi.nls.oskari.control.ActionConstants.*;

@OskariActionRoute("GetFeatureInfoWMS")
public class GetGeoPointDataHandler extends ActionHandler {

	private final OskariLayerService mapLayerService = new OskariLayerServiceMybatisImpl();
	private final GetGeoPointDataService geoPointService = new GetGeoPointDataService();

	private Logger log = LogFactory.getLogger(GetGeoPointDataHandler.class);

    private static final String PARAM_LAYERS = "layerIds";
    private static final String PARAM_X = "x";
    private static final String PARAM_Y = "y";
    private static final String PARAM_BBOX = "bbox";
    private static final String PARAM_WIDTH = "width";
    private static final String PARAM_HEIGHT = "height";
    private static final String PARAM_ZOOM = "zoom";
    private static final String PARAM_PARAMS = "params";

	@Override
    public void handleAction(final ActionParameters params) throws ActionException {
	     
		final String layerIds = params.getRequiredParam(PARAM_LAYERS);
		final String[] layerIdsArr = layerIds.split(",");
		final JSONObject allLayerAdditionalParams = getAllLayerAdditionalParams(params);

        final double lat = ConversionHelper.getDouble(params.getHttpParam(PARAM_LAT), -1);
        final double lon = ConversionHelper.getDouble(params.getHttpParam(PARAM_LON), -1);
        final int zoom = ConversionHelper.getInt(params.getHttpParam(PARAM_ZOOM), 0);
        
        final JSONArray data = new JSONArray();
		final String srs = params.getHttpParam(PARAM_SRS, "EPSG:3067");

		for (String id : layerIdsArr) {
			final int layerId = ConversionHelper.getInt(id, -1);
			if (layerId == -1) {
                log.warn("Couldn't parse layer id", id);
                continue;
			}

			final OskariLayer layer = mapLayerService.find(layerId);
			final String layerType = layer.getType();

			if (OskariLayer.TYPE_WMS.equals(layerType)) {
			    final GFIRequestParams gfiParams = new GFIRequestParams();
			    gfiParams.setBbox(params.getRequiredParam(PARAM_BBOX));
			    gfiParams.setHeight(params.getHttpParam(PARAM_HEIGHT));
			    gfiParams.setLat(lat);
			    gfiParams.setLayer(layer);
			    gfiParams.setLon(lon);
			    gfiParams.setWidth(params.getHttpParam(PARAM_WIDTH));
			    gfiParams.setX(params.getHttpParam(PARAM_X));
			    gfiParams.setY(params.getHttpParam(PARAM_Y));
			    gfiParams.setZoom(zoom);
                gfiParams.setSRSName(srs);
                gfiParams.setAdditionalParams(allLayerAdditionalParams.optJSONObject(id));
			    
			    final JSONObject response = geoPointService.getWMSFeatureInfo(gfiParams);
                if (response != null) {
                    data.put(response);
                }
			} else if (OskariLayer.TYPE_ARCGIS93.equals(layerType)) {
				final GFIRestQueryParams gfiParams = new GFIRestQueryParams();

				gfiParams.setBbox(params.getRequiredParam(PARAM_BBOX));
				gfiParams.setLat(lat);
				gfiParams.setLayer(layer);
				gfiParams.setLon(lon);

				gfiParams.setSRSName(srs);

				final JSONObject response = geoPointService.getRESTFeatureInfo(gfiParams);
				if (response != null) {
					data.put(response);
				}
			}
		}

		try {
	        final JSONObject rootJson = new JSONObject();
            rootJson.put("data", data);
			rootJson.put("layerCount", data.length());
	        ResponseHelper.writeResponse(params, rootJson);
		} catch (JSONException je) {
		    throw new ActionException("Could not populate GFI JSON: " + log.getAsString(data), je);
		}
	}

	private JSONObject getAllLayerAdditionalParams(final ActionParameters params) {
		try {
			return new JSONObject(params.getHttpParam(PARAM_PARAMS, "{}"));
		} catch (JSONException e) {
			log.warn("Couldn't parse params from POST request", e);
			return new JSONObject();
		}
	}
}
