package fi.nls.oskari.control.layer;

import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerAttributes;
import fi.nls.oskari.map.layer.OskariLayerService;
import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.util.WFSDescribeFeatureHelper;
import org.oskari.service.util.ServiceFactory;

/**
 * Get WMS capabilites and return JSON
 * @deprecated As of release 2.3.0, use fi.nls.oskari.control.layer.GetWFSLayerFields action route instead for WFS layers
 * For Analysis, Myplaces and Userlayer use values returned from GetXXLayers action route (JSONFormatter handles)
 */
@OskariActionRoute("GetWFSDescribeFeature")
public class GetWFSDescribeFeatureHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetWFSDescribeFeatureHandler.class);
    private final OskariLayerService layerService = ServiceFactory.getMapLayerService();

    private static final String PARM_LAYER_ID = "layer_id";
    private static final String PARM_SIMPLE = "simple";

    private static final String WPS_PARAMS = "wps_params";
    public static final String ANALYSIS_PREFIX = "analysis_";
    public static final String MYPLACES_PREFIX = "myplaces_";
    private static final String MYPLACES_BASELAYER_ID = "myplaces.baselayer.id";
    public static final String USERLAYER_PREFIX = "userlayer_";
    private static final String USERLAYER_BASELAYER_ID = "userlayer.baselayer.id";

    final String myplacesBaseLayerId = PropertyUtil.get(MYPLACES_BASELAYER_ID);
    final String userlayerBaseLayerId = PropertyUtil.get(USERLAYER_BASELAYER_ID);

    public void handleAction(ActionParameters params) throws ActionException {
        final String layer_id = params.getRequiredParam(PARM_LAYER_ID);
        final boolean simpleType = params.getHttpParam(PARM_SIMPLE, false);
        int id = getLayerId(layer_id);

        if (id != -1) {
            ResponseHelper.writeResponse(params, getResponse(id, simpleType));
        } else if (layer_id.indexOf(ANALYSIS_PREFIX) > -1) {
            // Set analysis layer field types
            ResponseHelper.writeResponse(params, WFSDescribeFeatureHelper.getAnalysisFeaturePropertyTypes(layer_id));
        } else {
            ResponseHelper.writeResponse(params, new JSONObject());
        }
    }

    private JSONObject getResponse(int id, boolean simpleType) throws ActionException {
        // Get wfs layer configuration ala Oskari
        OskariLayer layer = layerService.find(id);
        if (layer == null) {
            throw new ActionParamsException("Layer not found: " + id);
        }
        JSONObject response = new JSONObject();
        // Get wfs feature property names  (gml properties excluded)
        if(simpleType){
            // types are generalized to text or numeric
            response = getFeatureTypesTextOrNumeric(layer, "" + id);
        } else {
            // returns xsd types for properties
            try {
                response = WFSDescribeFeatureHelper.getWFSFeaturePropertyTypes(layer, "" + id);
            } catch (ServiceException ex) {
                throw new ActionException("Problem with layer: " + id, ex);
            }
        }
        // Add WPS params
        WFSLayerAttributes attrs = new WFSLayerAttributes(layer.getAttributes());
        JSONHelper.putValue(response, WPS_PARAMS, JSONHelper.createJSONObject(attrs.getWpsParams()));
        return response;
    }

    private int getLayerId(final String layer_id) {

        // WFS layer, myplaces or analysis layer
        if (layer_id.indexOf(MYPLACES_PREFIX) > -1) {
            return ConversionHelper.getInt(myplacesBaseLayerId, -1);
        } else if (layer_id.indexOf(USERLAYER_PREFIX) > -1) {
            return ConversionHelper.getInt(userlayerBaseLayerId, -1);
        }
        // Wfs layer id
        return ConversionHelper.getInt(layer_id, -1);
    }

    /**
     * Request WFS DescribeFeatureType response (xml)
     * @param lc  WFSlayerconfiguration ala Oskari
     *             e.g. http://tampere.navici.com/tampere_wfs_geoserver/ows?SERVICE=WFS&VERSION=1.1.0&REQUEST=DescribeFeatureType&TYPENAME=tampere_ora:KIINTEISTOT_ALUE
     * @param layer_id for keying response
     * @return  JSON object  (feature property names and text/numeric typing of property)
     * @throws ActionException
     */
    private JSONObject getFeatureTypesTextOrNumeric(OskariLayer layer, String layer_id) throws ActionException {
        try {
            return WFSDescribeFeatureHelper.getFeatureTypesTextOrNumeric(layer, layer_id);
        } catch (ServiceException ex) {
            throw new ActionException("Error getting properties", ex);
        }
    }
}
