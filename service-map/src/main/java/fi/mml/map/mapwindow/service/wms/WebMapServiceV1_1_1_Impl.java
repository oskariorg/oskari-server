package fi.mml.map.mapwindow.service.wms;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import fi.mml.wms.v111.Format;
import fi.mml.wms.v111.GetFeatureInfo;
import fi.mml.wms.v111.Keyword;
import fi.mml.wms.v111.Layer;
import fi.mml.wms.v111.LegendURL;
import fi.mml.wms.v111.SRS;
import fi.mml.wms.v111.Style;
import fi.mml.wms.v111.WMTMSCapabilitiesDocument;

/**
 * 1.1.1 implementation of WMS
 */
public class WebMapServiceV1_1_1_Impl extends AbstractWebMapService {

    public WebMapServiceV1_1_1_Impl(String url, String data, String layerName) throws WebMapServiceParseException {
        super(url);
        parseXML(data, layerName);
    }

    public String getVersion() {
        return "1.1.1";
    }

    private void parseXML(String data, String layerName) throws WebMapServiceParseException {
        try {
            WMTMSCapabilitiesDocument wmtms = WMTMSCapabilitiesDocument.Factory.parse(data);

            Layer layerCapabilities = wmtms.getWMTMSCapabilities().getCapability().getLayer();
            LinkedList<Layer> path = new LinkedList<>();
            boolean found = find(layerCapabilities, layerName, path, 0);
            if (!found) {
                throw new WebMapServiceParseException("Could not find layer");
            }

            this.styles = new HashMap<>();
            this.legends = new HashMap<>();
            for (Layer layer : path) {
                parseStylesAndLegends(layer, styles, legends);
            }
            this.formats = parseFormats(wmtms);
            this.CRSs = parseCRSs(wmtms);

            Layer layer = path.getLast();
            this.queryable = "1".equals(layer.getQueryable().toString());
            this.time = Arrays.stream(layer.getExtentArray())
                .filter(ext -> "time".equals(ext.getName()))
                .findAny()
                .map(ext -> getText(ext))
                .map(Optional::get)
                .map(s -> Arrays.asList(s.split(",")))
                .orElse(Collections.emptyList());
            this.keywords = parseKeywords(layer);
        } catch (Exception e) {
            throw new WebMapServiceParseException(e);
        }
    }

    /**
     * Traverse layer tree, trying to find one specific layer
     * @param layer layer to inspect
     * @param layerName name of the layer we're looking for
     * @param path holds information of the branch we are in
     * @param lvl how deep we are in the subLayers, if this gets too high we quit
     * @return false if no such layer exists, true otherwise,
     *         LinkedList<Layer> path contains the path from root to the layer
     */
    private boolean find(Layer layer, String layerName, LinkedList<Layer> path, int lvl)
            throws WebMapServiceParseException {
        if (lvl > 5) {
            throw new WebMapServiceParseException(
                    "We tried to parse layers to fifth level of recursion,"
                    + " this is too much. Cancel.");
        }
        if (layerName.equals(getText(layer.getName()).orElse(null))) {
            // Add current layer before returning
            path.addLast(layer);
            return true;
        }
        Layer[] subLayers = layer.getLayerArray();
        if (subLayers != null && subLayers.length > 0) {
            // Remember current layer while we check its subLayers
            path.addLast(layer);
            for (Layer subLayer : subLayers) {
                if (find(subLayer, layerName, path, lvl + 1)) {
                    return true;
                }
            }
            // None of the subLayers matched, remove current layer from the correct path
            path.removeLast();
        }
        return false;
    }

    private void parseStylesAndLegends(Layer layer,
            Map<String, String> styles,
            Map<String, String> legends) {
        Style[] stylesArray = layer.getStyleArray();
        if (stylesArray == null) {
            return;
        }
        for (Style style : stylesArray) {
            Optional<String> styleNameOpt = getText(style.getName());
            if (!styleNameOpt.isPresent()) {
                continue;
            }
            String styleName = styleNameOpt.get();
            String styleTitle = getText(style.getTitle()).orElse(styleName);

            styles.put(styleName, styleTitle);

            LegendURL[] lurl = style.getLegendURLArray();
            if (lurl == null || lurl.length == 0 || lurl[0].getOnlineResource() == null) {
                continue;
            }
            /* OnlineResource is in xlink namespace */
            String href = lurl[0].getOnlineResource().newCursor().getAttributeText(XLINK_HREF);
            if (href != null) {
                legends.put(styleName + LEGEND_HASHMAP_KEY_SEPARATOR + styleTitle, href);
            }
        }
    }

    private String[] parseKeywords(Layer layer) {
        if (layer.getKeywordList() != null) {
            Keyword[] words = layer.getKeywordList().getKeywordArray();
            if (words != null) {
                String[] keywords = new String[words.length];
                for (int i = 0; i < words.length; i++) {
                    keywords[i] = getText(words[i])
                            .orElseThrow(() -> new IllegalArgumentException("Empty keyword"));
                }
                return keywords;
            }
        }
        return new String[0];
    }

    private String[] parseFormats(WMTMSCapabilitiesDocument wmtms) {
        GetFeatureInfo gfi = wmtms.getWMTMSCapabilities().getCapability().getRequest().getGetFeatureInfo();
        if (gfi != null) {
            Format[] tmpformats = gfi.getFormatArray();
            String[] formats = new String[tmpformats.length];
            for (int i = 0; i < tmpformats.length; i++) {
                formats[i] = getText(tmpformats[i])
                        .orElseThrow(() -> new IllegalArgumentException("Empty format"));
            }
            return formats;
        }
        return new String[0];
    }

    private String[] parseCRSs(WMTMSCapabilitiesDocument wmtms) {
        SRS[] crss = wmtms.getWMTMSCapabilities().getCapability().getLayer().getSRSArray();
        String[] CRSs = new String[crss.length];
        for (int i = 0; i < crss.length; i++) {
            CRSs[i] = getText(crss[i])
                    .orElseThrow(() -> new IllegalArgumentException("Empty EPSG code"));
        }
        return CRSs;
    }

    private static Optional<String> getText(XmlObject obj) {
        if (obj == null) {
            return Optional.empty();
        }
        XmlCursor cursor = obj.newCursor();
        if (cursor == null) {
            return Optional.empty();
        }
        String text = cursor.getTextValue();
        if (text == null || text.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(text);
    }

}
