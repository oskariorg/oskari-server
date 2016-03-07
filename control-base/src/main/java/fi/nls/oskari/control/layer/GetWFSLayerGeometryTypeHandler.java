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

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Get WFS layer geometry type
 */
@OskariActionRoute("GetWFSLayerGeometryType")
public class GetWFSLayerGeometryTypeHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetWFSLayerGeometryTypeHandler.class);
    private final WFSLayerConfigurationService layerConfigurationService = new WFSLayerConfigurationServiceIbatisImpl();

    private static final String PARM_LAYER_ID = "layer_id";

    @Override
    public void init() {
        super.init();
    }

    public void handleAction(ActionParameters params) throws ActionException {
        final String layer_id = params.getHttpParam(PARM_LAYER_ID, "");
        int id = getLayerId(layer_id);
        String response = null;

        try {
        if (id != -1) {
            // Get wfs layer configuration ala Oskari
            WFSLayerConfiguration lc = layerConfigurationService.findConfiguration(id);
            if (lc != null) {
            	final String geometryField = lc.getGMLGeometryProperty();
            	
            	final String wfsurl = WFSDescribeFeatureHelper.parseDescribeFeatureUrl(lc.getURL(), lc.getWFSVersion(), lc.getFeatureNamespace(), lc.getFeatureElement());
                final String wfsResponse = WFSDescribeFeatureHelper.getResponse(wfsurl, lc.getUsername(), lc.getPassword());
                JSONObject props = WFSDescribeFeatureHelper.xml2JSON(wfsResponse);
                
                JSONObject schema = props.getJSONObject("xsd:schema");
                JSONObject complexType = schema.getJSONObject("xsd:complexType");
                JSONObject complexContent = complexType.getJSONObject("xsd:complexContent");
                JSONObject extension = complexContent.getJSONObject("xsd:extension");
                JSONObject sequence = extension.getJSONObject("xsd:sequence");
                JSONArray elements = sequence.getJSONArray("xsd:element");
                for (int i = 0; i < elements.length(); i++)
                {
                	if (elements.getJSONObject(i).getString("name").equals(geometryField))
                	{
                		response = elements.getJSONObject(i).getString("type");
                	}
                }
                
                // Get wfs feature property names  (gml properties excluded)
                //response = getFeatureTypesTextOrNumeric(lc, layer_id);
                // Add WPS params
            }
        }

        ResponseHelper.writeResponse(params, response);
        }
        catch (Exception ex)
        {
        	
        }
    }

    private int getLayerId(final String layer_id) {
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
