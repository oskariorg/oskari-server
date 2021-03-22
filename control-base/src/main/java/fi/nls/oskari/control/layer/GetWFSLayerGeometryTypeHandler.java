package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerCapabilities;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.util.WFSDescribeFeatureHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.service.util.ServiceFactory;

/**
 * Get WFS layer geometry type
 * @deprecated As of release 2.3.0, use fi.nls.oskari.control.layer.GetWFSLayerFields action route instead for WFS layers
 * For Analysis, Myplaces and Userlayer use values returned from GetXXLayers action route (JSONFormatter handles)
 */
@Deprecated
@OskariActionRoute("GetWFSLayerGeometryType")
public class GetWFSLayerGeometryTypeHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetWFSLayerGeometryTypeHandler.class);
    private static final String PARM_LAYER_ID = "layer_id";
    private final OskariLayerService layerService = ServiceFactory.getMapLayerService();

    public void handleAction(ActionParameters params) throws ActionException {
        final int id = params.getRequiredParamInt(PARM_LAYER_ID);
        String response = null;

        OskariLayer layer = layerService.find(id);
        if (layer == null) {
            throw new ActionParamsException("Layer not found: " + id);
        }
        WFSLayerCapabilities caps = new WFSLayerCapabilities(layer.getCapabilities());
        try {
            final String geometryField = caps.getGeometryAttribute();

            final String wfsurl = WFSDescribeFeatureHelper.parseDescribeFeatureUrl(layer.getUrl(), layer.getVersion(), layer.getName());
            final String wfsResponse = WFSDescribeFeatureHelper.getResponse(wfsurl, layer.getUsername(), layer.getPassword());
            JSONObject props = WFSDescribeFeatureHelper.xml2JSON(wfsResponse);

            JSONObject schema = props.getJSONObject("xsd:schema");
            JSONObject complexType = schema.getJSONObject("xsd:complexType");
            JSONObject complexContent = complexType.getJSONObject("xsd:complexContent");
            JSONObject extension = complexContent.getJSONObject("xsd:extension");
            JSONObject sequence = extension.getJSONObject("xsd:sequence");
            JSONArray elements = sequence.getJSONArray("xsd:element");
            for (int i = 0; i < elements.length(); i++) {
                if (elements.getJSONObject(i).getString("name").equals(geometryField)) {
                    response = elements.getJSONObject(i).getString("type");
                }
            }

            // Add WPS params

            ResponseHelper.writeResponse(params, response);
        } catch (JSONException | ServiceException ex) {
            throw new ActionException("Unexpected input", ex);
        }
    }
}
