package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.domain.map.wfs.WFSSLDStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 17.12.2013
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class LayerJSONFormatterWFS extends LayerJSONFormatter {

    private static Logger log = LogFactory.getLogger(LayerJSONFormatterWFS.class);
    private static WFSLayerConfigurationService wfsService = new WFSLayerConfigurationServiceIbatisImpl();


    public JSONObject getJSON(OskariLayer layer,
                                     final String lang,
                                     final boolean isSecure,
                                     final String crs) {

        final JSONObject layerJson = getBaseJSON(layer, lang, isSecure, crs);
        final WFSLayerConfiguration wfsConf = wfsService.findConfiguration(layer.getId());
        JSONHelper.putValue(layerJson, "styles", getStyles(wfsConf));
        // Use maplayer setup
        if(layer.getStyle() == null || layer.getStyle().isEmpty() ){
            JSONHelper.putValue(layerJson, "style", "default");
        }
        else {
            JSONHelper.putValue(layerJson, "style", layer.getStyle());
        }
        JSONHelper.putValue(layerJson, "isQueryable", true);
        JSONHelper.putValue(layerJson, "wps_params", getWpsParams(wfsConf) );
        if(wfsConf != null){
            JSONHelper.putValue(layerJson, "WMSLayerId", wfsConf.getWMSLayerId() );
        }

        return layerJson;
    }

    /**
     * Constructs a style json
     *
     * @param  wfsConf wfs layer configuration
     */
    private JSONArray getStyles(WFSLayerConfiguration wfsConf) {

        JSONArray arr = new JSONArray();
        if (wfsConf == null) return arr;

        final List<WFSSLDStyle> styleList = wfsConf.getSLDStyles();
        if (styleList == null) return arr;

        try {
            for (WFSSLDStyle style : styleList) {
                JSONObject obj = createStylesJSON(style.getName(), style.getName(), style.getName());
                if (obj.length() > 0) {
                    arr.put(obj);
                }
            }
        } catch (Exception e) {
          log.warn("Failed to query wfs styles via SQL client");
        }
        return arr;
    }

    /**
     * Constructs wps params json
     *
     * @param  wfsConf wfs layer configuration
     */
    private JSONObject getWpsParams(WFSLayerConfiguration wfsConf) {

        JSONObject json = new JSONObject();
        if (wfsConf == null) return json;

        return JSONHelper.createJSONObject(wfsConf.getWps_params());

    }

}
