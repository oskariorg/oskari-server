package fi.nls.oskari.wmts;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.XmlHelper;
import fi.nls.oskari.wmts.domain.TileMatrixLimits;
import fi.nls.oskari.wmts.domain.TileMatrixSet;
import fi.nls.oskari.wmts.domain.WMTSCapabilitiesLayer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.SimpleNamespaceContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Initial parsing for WMTS capabilities in a way that admin-layerselector can interpret it.
 */
public class WMTSCapabilitiesParser {

    private Logger log = LogFactory.getLogger(WMTSCapabilitiesParser.class);

    public JSONObject parseCapabilitiesToJSON(final String xml, final String url) throws Exception {
        if(xml == null) {
            return null;
        }
        final Set<TileMatrixSet> tileMatrixSets = new HashSet<TileMatrixSet>();
        final Set<WMTSCapabilitiesLayer> layers = new HashSet<WMTSCapabilitiesLayer>();

        // parse string to axiom object
        final OMElement doc = XmlHelper.parseXML(xml);
        final OMElement contents = XmlHelper.getChild(doc, "Contents");

        // parse layers
        final Iterator<OMElement> layerIterator = contents.getChildrenWithLocalName("Layer");
        while(layerIterator.hasNext()) {
            layers.add(LayerParser.parse(layerIterator.next()));
        }

        // parse tilematrixsets
        final Iterator<OMElement> matrixIterator = contents.getChildrenWithLocalName("TileMatrixSet");
        while(matrixIterator.hasNext()) {
            tileMatrixSets.add(TileMatrixSetParser.parse(matrixIterator.next()));
        }

        // start building result
        final JSONObject result = new JSONObject();

        final JSONArray layersNode = new JSONArray();
        JSONHelper.putValue(result, "layers", layersNode);
        for(WMTSCapabilitiesLayer layer : layers) {
            final Map<String, Set<TileMatrixLimits>> links = layer.getLinks();
            if(links.size() == 1) {
                JSONObject layerJson = layer.getAsJSON();
                if(!layerJson.has("url")) {
                    JSONHelper.putValue(layerJson, "url", url);
                }
                layersNode.put(layerJson);
            }
            else if(links.size() > 1) {
                for(String link : links.keySet()) {
                    JSONObject layerJson = layer.getAsJSON();
                    JSONHelper.putValue(layerJson, "title", layer.getTitle() + "/" + link + " (SRS: " + getMatrixCRS(link, tileMatrixSets) + ")");
                    JSONHelper.putValue(layerJson, "tileMatrixSetId", link);
                    JSONHelper.putValue(layerJson, "additionalId", link);

                    if(!layerJson.has("url")) {
                        JSONHelper.putValue(layerJson, "url", url);
                    }
                    layersNode.put(layerJson);
                }
            }
        }

        final JSONObject matrixNode = new JSONObject();
        JSONHelper.putValue(result, "matrixSets", matrixNode);
        for(TileMatrixSet matrix : tileMatrixSets) {
            JSONHelper.putValue(matrixNode, matrix.getId(), matrix.getAsJSON());
        }

        return result;
    }

    private String getMatrixCRS(final String id, final Set<TileMatrixSet> tileMatrixSets) {
        for(TileMatrixSet matrix : tileMatrixSets) {
            if(matrix.getId().equals(id)) {
                return matrix.getCrs();
            }
        }
        return "CRS N/A";
    }

}
