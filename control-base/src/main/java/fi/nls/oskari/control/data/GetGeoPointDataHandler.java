package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.data.domain.GFIRequestParams;
import fi.nls.oskari.map.data.domain.GFIRestQueryParams;
import fi.nls.oskari.map.data.service.GetGeoPointDataService;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.map.myplaces.service.GeoServerProxyService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static fi.nls.oskari.control.ActionConstants.*;

@OskariActionRoute("GetFeatureInfoWMS")
public class GetGeoPointDataHandler extends ActionHandler {

	private final OskariLayerService mapLayerService = new OskariLayerServiceIbatisImpl();
	private final GetGeoPointDataService geoPointService = new GetGeoPointDataService();
    private final GeoServerProxyService myplacesService = new GeoServerProxyService();

	private Logger log = LogFactory.getLogger(GetGeoPointDataHandler.class);

    private static final String PARAM_LAYERS = "layerIds";
    private static final String PARAM_X = "x";
    private static final String PARAM_Y = "y";
    private static final String PARAM_BBOX = "bbox";
    private static final String PARAM_WIDTH = "width";
    private static final String PARAM_HEIGHT = "height";
    private static final String PARAM_STYLES = "styles";
    private static final String PARAM_ZOOM = "zoom";
    private static final String PARAM_GEOJSON = "geojson";

	@Override
    public void handleAction(final ActionParameters params) throws ActionException {
	     
		final String layerIds = params.getRequiredParam(PARAM_LAYERS);
		final String[] layerIdsArr = layerIds.split(",");
		
        final User user = params.getUser();
        final double lat = ConversionHelper.getDouble(params.getHttpParam(PARAM_LAT), -1);
        final double lon = ConversionHelper.getDouble(params.getHttpParam(PARAM_LON), -1);
        final int zoom = ConversionHelper.getInt(params.getHttpParam(PARAM_ZOOM), 0);
        
        final JSONArray data = new JSONArray();
		JSONObject geojs = new JSONObject();
        try {
           
            geojs = new JSONObject(params.getHttpParam(
                    PARAM_GEOJSON, "{}"));

        } catch (JSONException ee) {
            log.warn("Couldn't parse geojson from POST request", ee);
        }
		final String srs = params.getHttpParam(PARAM_SRS, "EPSG:3067");

		for (String id : layerIdsArr) {
			if (id.indexOf('_') >= 0) {
			    if (id.startsWith("myplaces_")) {
			        // Myplaces wfs query modifier
                    final JSONObject response = myplacesService.getFeatureInfo(lat, lon, zoom, id, user.getUuid(), srs);
                    if(response != null) {
                        data.put(response);
                    }
			    }
			    continue;
			}
			
			final int layerId = ConversionHelper.getInt(id, -1);
			if(layerId == -1) {
                log.warn("Couldnt parse layer id", id);
                continue;
			}

			final OskariLayer layer = mapLayerService.find(layerId);
			final String layerType = layer.getType();

			if (OskariLayer.TYPE_WMS.equals(layerType)) {
			    final GFIRequestParams gfiParams = new GFIRequestParams();
			    gfiParams.setBbox(params.getRequiredParam(PARAM_BBOX));
			    gfiParams.setCurrentStyle(params.getHttpParam(PARAM_STYLES, ""));
			    gfiParams.setHeight(params.getHttpParam(PARAM_HEIGHT));
			    gfiParams.setLat(lat);
			    gfiParams.setLayer(layer);
			    gfiParams.setLon(lon);
			    gfiParams.setWidth(params.getHttpParam(PARAM_WIDTH));
			    gfiParams.setX(params.getHttpParam(PARAM_X));
			    gfiParams.setY(params.getHttpParam(PARAM_Y));
			    gfiParams.setZoom(zoom);
                gfiParams.setSRSName(srs);
			    
			    final JSONObject response = geoPointService.getWMSFeatureInfo(gfiParams);
                if(response != null) {
                    data.put(response);
                }
				continue;
			} else if (OskariLayer.TYPE_ARCGIS93.equals(layerType)) {
				final GFIRestQueryParams gfiParams = new GFIRestQueryParams();

				gfiParams.setBbox(params.getRequiredParam(PARAM_BBOX));
				gfiParams.setLat(lat);
				gfiParams.setLayer(layer);
				gfiParams.setLon(lon);

				gfiParams.setSRSName(srs);

				final JSONObject response = geoPointService.getRESTFeatureInfo(gfiParams);
				if(response != null) {
					data.put(response);
				}
				continue;
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
}
