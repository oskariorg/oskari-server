package fi.nls.oskari.control.layer;

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
import org.json.JSONObject;

/**
 * Get WMS capabilites and return JSON
 */
@OskariActionRoute("GetWFSDescribeFeature")
public class GetWFSDescribeFeatureHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetWFSDescribeFeatureHandler.class);
    private final WFSLayerConfigurationService layerConfigurationService = new WFSLayerConfigurationServiceIbatisImpl();

    private static final String PARM_LAYER_ID = "layer_id";

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
        int id = getLayerId(layer_id);
        JSONObject response = new JSONObject();

        if (id != -1) {
            // Get WFS url in wfs layer configuration
            WFSLayerConfiguration lc = layerConfigurationService.findConfiguration(id);
            if (lc != null) {
                final String wfsurl = WFSDescribeFeatureHelper.parseDescribeFeatureUrl(lc.getURL(), lc.getWFSVersion(), lc.getFeatureNamespace(), lc.getFeatureElement());
                JSONObject props = getRawDescribeFeatureType(wfsurl, lc.getUsername(), lc.getPassword());
                // Simple type match (string or numeric)
                response = WFSDescribeFeatureHelper.getFeatureTypesTextOrNumeric(layer_id, props);

                // Add WPS params
                JSONHelper.putValue(response, WPS_PARAMS, JSONHelper.createJSONObject(lc.getWps_params()));
            }

        } else if (layer_id.indexOf(ANALYSIS_PREFIX) > -1) {
            // Set analysis layer field types
            response = WFSDescribeFeatureHelper.getAnalysisFeaturePropertyTypes(layer_id);
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
     * @param url  DescribeFeatureType Url
     *             e.g. http://tampere.navici.com/tampere_wfs_geoserver/ows?SERVICE=WFS&VERSION=1.1.0&REQUEST=DescribeFeatureType&TYPENAME=tampere_ora:KIINTEISTOT_ALUE
     * @param user
     * @param pass
     * @return  Raw JSON object  (auto conversion of xml to JSON)
     * @throws ActionException
     */
    private JSONObject getRawDescribeFeatureType(final String url, final String user, final String pass) throws ActionException {
        try {
            final String response = WFSDescribeFeatureHelper.getResponse(url, user, pass);
            return WFSDescribeFeatureHelper.xml2JSON(response);
        } catch (ServiceException ex) {
            throw new ActionException("Error getting properties", ex);
        }
    }
}
