package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wmts.domain.TileMatrixLimits;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import fi.nls.oskari.wmts.domain.WMTSCapabilitiesLayer;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 17.12.2013
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class LayerJSONFormatterWMTS extends LayerJSONFormatter {

    public static final String KEY_TILEMATRIXIDS = "tileMatrixIds";
    private static Logger log = LogFactory.getLogger(LayerJSONFormatterWMTS.class);

    public JSONObject getJSON(OskariLayer layer,
                              final String lang,
                              final boolean isSecure) {

        final JSONObject layerJson = getBaseJSON(layer, lang, isSecure);

        // Use capabities in 1st hand for to get matrix id

        JSONHelper.putValue(layerJson, "tileMatrixSetId", layer.getTileMatrixSetId());

        // TODO: parse tileMatrixSetData for styles and set default style name from the one where isDefault = true
        String styleName = layer.getStyle();

        if(styleName == null || styleName.isEmpty()) {
            styleName = "default";
        }
        JSONHelper.putValue(layerJson, "style", styleName);
        JSONArray styles = new JSONArray();
        // currently supporting only one style (default style)
        styles.put(createStylesJSON(styleName, styleName, null));
        JSONHelper.putValue(layerJson, "styles", styles);

        // if options have urlTemplate -> use it (treat as a REST layer)
        final String urlTemplate = JSONHelper.getStringFromJSON(layer.getOptions(), "urlTemplate", null);
        final boolean needsProxy = useProxy(layer);
        if(urlTemplate != null) {
            if(needsProxy) {
                // remove requestEncoding so we always get KVP params when proxying
                JSONObject options = layerJson.optJSONObject("options");
                options.remove("requestEncoding");
            } else {
                // setup tileURL for REST layers
                final String originalUrl = layer.getUrl();
                layer.setUrl(urlTemplate);
                JSONHelper.putValue(layerJson, "tileUrl", layer.getUrl(isSecure));
                // switch back the original url in case it's used down the line
                layer.setUrl(originalUrl);
            }
        }
        return layerJson;
    }

    /**
     *
     * @param wmts
     * @param layer
     * @return
     */

    public static JSONObject createCapabilitiesJSON(final WMTSCapabilities wmts,final WMTSCapabilitiesLayer layer) {

        JSONObject capabilities = new JSONObject();
        if(layer == null) {
            return capabilities;
        }

        List<JSONObject> tileMatrix = LayerJSONFormatterWMTS.createTileMatrixArray(wmts, layer);
        JSONHelper.putValue(capabilities, KEY_TILEMATRIXIDS, new JSONArray(tileMatrix));

        return capabilities;
    }

    /**
     * Return array of wmts tilematrixsets  (Crs code and Identifier)
     * @param wmts wmts service capabilities
     * @param layer  wmts layer capabilities
     * @return
     */
    public static List<JSONObject> createTileMatrixArray(final WMTSCapabilities wmts, final WMTSCapabilitiesLayer layer) {
        final List<JSONObject> tileMatrix = new ArrayList<>();
        Map<String, Set<TileMatrixLimits>> links = layer.getLinks();
        if (links.size() > 0 ) {
            //Loop matrixSet links
            for (Map.Entry<String, Set<TileMatrixLimits>> entry : links.entrySet()) {
                String crs = wmts.getMatrixCRS(entry.getKey());
                if(crs != null){
                    tileMatrix.add(JSONHelper.createJSONObject(ProjectionHelper.shortSyntaxEpsg(crs), entry.getKey()));
                }
            }
        }
        return tileMatrix;
    }

    /**
     * Constructs a  csr set containing the supported coordinate ref systems of WMS service
     *
     * @param wmts WebMapService
     * @return Set<String> containing the supported coordinate ref systems of WMS service
     */
    public static Set<String> getCRSs(final WMTSCapabilities wmts, final WMTSCapabilitiesLayer layer) {

        Set<String>  crss = new HashSet<String>();

        Map<String, Set<TileMatrixLimits>> links = layer.getLinks();
        if (links.size() > 0 ) {
            //Loop matrixSet links
            for (Map.Entry<String, Set<TileMatrixLimits>> entry : links.entrySet()) {
                String crs = wmts.getMatrixCRS(entry.getKey());
                crss.add(ProjectionHelper.shortSyntaxEpsg(crs));
            }
            return crss;
        }

        return null;
    }

    /**
     * Get matrix id by current crs
     * @param crs
     * @return
     */
    public static String getTileMatrixSetId(final JSONObject capabilities, final String crs) {
        if (capabilities.has("tileMatrixIds")) {
            JSONArray jsa = JSONHelper.getJSONArray(capabilities, "tileMatrixIds");

            for (int i = 0, size = jsa.length(); i < size; i++) {
                JSONObject js = JSONHelper.getJSONObject(jsa, i);
                if(js.has(crs)){
                    return JSONHelper.getStringFromJSON(js,crs,null);
                }
            }
        }
        return null;
    }



}
