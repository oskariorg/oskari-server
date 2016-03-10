package fi.nls.oskari.control.layer;

import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;
import fi.nls.oskari.wfs.util.WFSDescribeFeatureHelper;

/**
 * Get WMS capabilites and return JSON
 */
@OskariActionRoute("GetWFSDescribeFeature")
public class GetWFSDescribeFeatureHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetWFSDescribeFeatureHandler.class);
    private final WFSLayerConfigurationService layerConfigurationService = new WFSLayerConfigurationServiceIbatisImpl();

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

    @Override
    public void init() {
        super.init();
    }

    public void handleAction(ActionParameters params) throws ActionException {
        final String layer_id = params.getHttpParam(PARM_LAYER_ID, "");
        final Boolean simpleType = params.getHttpParam(PARM_SIMPLE, false);
        int id = getLayerId(layer_id);
        JSONObject response = new JSONObject();

        try {
	        if (id != -1) {
	            // Get wfs layer configuration ala Oskari
	            WFSLayerConfiguration lc = layerConfigurationService.findConfiguration(id);
	            if (lc != null) {
	                // Get wfs feature property names  (gml properties excluded)
                    if(simpleType){
                        // types are generalized to text or numeric
                        response = getFeatureTypesTextOrNumeric(lc, layer_id);
                    } else {
                        // returns xsd types for properties
                        response = WFSDescribeFeatureHelper.getWFSFeaturePropertyTypes(lc, layer_id);
                    }
	                // Add WPS params
	                JSONHelper.putValue(response, WPS_PARAMS, JSONHelper.createJSONObject(lc.getWps_params()));
	            }
	
	        } else if (layer_id.indexOf(ANALYSIS_PREFIX) > -1) {
	            // Set analysis layer field types
	            response = WFSDescribeFeatureHelper.getAnalysisFeaturePropertyTypes(layer_id);
	        }
        } catch (ServiceException ex) {
        	
        }

        ResponseHelper.writeResponse(params, response);

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
    private JSONObject getFeatureTypesTextOrNumeric(WFSLayerConfiguration lc, String layer_id) throws ActionException {
        try {
            return WFSDescribeFeatureHelper.getFeatureTypesTextOrNumeric(lc, layer_id);
        } catch (ServiceException ex) {
            throw new ActionException("Error getting properties", ex);
        }
    }
}
