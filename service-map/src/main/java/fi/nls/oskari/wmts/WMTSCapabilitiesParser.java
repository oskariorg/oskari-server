package fi.nls.oskari.wmts;

import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.XmlHelper;
import fi.nls.oskari.wmts.domain.TileMatrixLimits;
import fi.nls.oskari.wmts.domain.TileMatrixSet;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import fi.nls.oskari.wmts.domain.WMTSCapabilitiesLayer;
import org.apache.axiom.om.OMElement;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Initial parsing for WMTS capabilities in a way that admin-layerselector can interpret it.
 */
public class WMTSCapabilitiesParser {
    private static final String KEY_LAYERS_WITH_REMARKS = "layersWithRemarks";

    public WMTSCapabilities parseCapabilities(final String xml)
            throws Exception {
        if (xml == null) {
            return null;
        }
        final WMTSCapabilities caps = new WMTSCapabilities();

        // parse string to axiom object
        final OMElement doc = XmlHelper.parseXML(xml);
        final OMElement contents = XmlHelper.getChild(doc, "Contents");

        // parse layers
        final Iterator<OMElement> layerIterator = contents.getChildrenWithLocalName("Layer");
        while (layerIterator.hasNext()) {
            caps.addLayer(LayerParser.parse(layerIterator.next()));
        }
        // parse tilematrixsets
        final Iterator<OMElement> matrixIterator = contents.getChildrenWithLocalName("TileMatrixSet");
        while (matrixIterator.hasNext()) {
            caps.addTileMatrixSet(TileMatrixSetParser.parse(matrixIterator.next()));
        }
        return caps;
    }

    public JSONObject parseCapabilitiesToJSON(final String xml, final String url, String currentCrs)
            throws Exception {

        final WMTSCapabilities caps = parseCapabilities(xml);

        // start building result
        final JSONObject result = new JSONObject();

        final JSONArray layersNode = new JSONArray();
        JSONHelper.putValue(result, "layers", layersNode);
        for (WMTSCapabilitiesLayer layer : caps.getLayers()) {
            final Map<String, Set<TileMatrixLimits>> links = layer.getLinks();
            JSONObject layerJson = layer.getAsJSON();
            final String matrixsetid = getMatrixSetId(caps, links, currentCrs);
            if(matrixsetid == null ){
                JSONHelper.putValue(layerJson, "title", layer.getTitle() + "  *");
                result.put(KEY_LAYERS_WITH_REMARKS,"true");
            }
            else {
                JSONHelper.putValue(layerJson, "tileMatrixSetId", matrixsetid);
            }
            if (!layerJson.has("url")) {
                JSONHelper.putValue(layerJson, "url", url);
            }
            layersNode.put(layerJson);

        }

        final JSONObject matrixNode = new JSONObject();
        JSONHelper.putValue(result, "matrixSets", matrixNode);
        for (TileMatrixSet matrix : caps.getTileMatrixSets()) {
            JSONHelper.putValue(matrixNode, matrix.getId(), matrix.getAsJSON());
        }

        return result;
    }

    /**
     * Get tile matrix set id of current crs
     * @param caps
     * @param links
     * @param currentCrs
     * @return
     */
    public String getMatrixSetId(final WMTSCapabilities caps, Map<String, Set<TileMatrixLimits>> links, String currentCrs) {

        for (String link : links.keySet()) {
             if(ProjectionHelper.shortSyntaxEpsg(caps.getMatrixCRS(link)).equals(currentCrs)){
                 return link;
             }
        }
        return null;
    }

}
