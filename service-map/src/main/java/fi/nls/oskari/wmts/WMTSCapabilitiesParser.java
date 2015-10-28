package fi.nls.oskari.wmts;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
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

    public JSONObject parseCapabilitiesToJSON(final String xml, final String url)
            throws Exception {

        final WMTSCapabilities caps = parseCapabilities(xml);

        // start building result
        final JSONObject result = new JSONObject();

        final JSONArray layersNode = new JSONArray();
        JSONHelper.putValue(result, "layers", layersNode);
        for (WMTSCapabilitiesLayer layer : caps.getLayers()) {
            final Map<String, Set<TileMatrixLimits>> links = layer.getLinks();
            if (links.size() == 1) {
                JSONObject layerJson = layer.getAsJSON();
                if (!layerJson.has("url")) {
                    JSONHelper.putValue(layerJson, "url", url);
                }
                layersNode.put(layerJson);
            } else if (links.size() > 1) {
                for (String link : links.keySet()) {
                    JSONObject layerJson = layer.getAsJSON();
                    JSONHelper.putValue(layerJson, "title", layer.getTitle() + "/" + link + " (SRS: " + caps.getMatrixCRS(link) + ")");
                    JSONHelper.putValue(layerJson, "tileMatrixSetId", link);
                    JSONHelper.putValue(layerJson, "additionalId", link);

                    if (!layerJson.has("url")) {
                        JSONHelper.putValue(layerJson, "url", url);
                    }
                    layersNode.put(layerJson);
                }
            }
        }

        final JSONObject matrixNode = new JSONObject();
        JSONHelper.putValue(result, "matrixSets", matrixNode);
        for (TileMatrixSet matrix : caps.getTileMatrixSets()) {
            JSONHelper.putValue(matrixNode, matrix.getId(), matrix.getAsJSON());
        }

        return result;
    }

}
