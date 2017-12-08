package fi.mml.map.mapwindow.service.wms;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import fi.mml.wms.v111.Format;
import fi.mml.wms.v111.GetFeatureInfo;
import fi.mml.wms.v111.Keyword;
import fi.mml.wms.v111.Layer;
import fi.mml.wms.v111.LegendURL;
import fi.mml.wms.v111.Name;
import fi.mml.wms.v111.SRS;
import fi.mml.wms.v111.Style;
import fi.mml.wms.v111.Title;
import fi.mml.wms.v111.WMTMSCapabilitiesDocument;
import fi.nls.oskari.map.geometry.ProjectionHelper;

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

            Layer rootLayer = wmtms.getWMTMSCapabilities().getCapability().getLayer();
            LinkedList<Layer> path = new LinkedList<>();
            boolean found = find(rootLayer, layerName, path, 0);
            if (!found) {
                throw new WebMapServiceParseException("Could not find layer");
            }

            for (Layer layer : path) {
                parseStylesAndLegends(layer);
            }
            this.formats = parseFormats(wmtms);
            this.CRSs = parseCRSs(wmtms);

            Layer layer = path.getLast();
            this.queryable = "1".equals(layer.getQueryable().toString());
            this.time = Arrays.stream(layer.getExtentArray())
                .filter(ext -> "time".equals(ext.getName()))
                .findAny()
                .map(ext -> Arrays.asList(ext.xmlText().split(",")))
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
     * @param path holds information of the branches we are in
     * @param lvl how deep we are in the subLayers, if this gets too high we quit
     * @return true if this is the layer we're looking for, false if not
     */
    private boolean find(Layer layer, String layerName, LinkedList<Layer> path, int lvl)
            throws WebMapServiceParseException {
        if (lvl > 5) {
            throw new WebMapServiceParseException("We tried to parse layers to fifth level of recursion, this is too much. Cancel.");
        }
        if (layerName.equals(layer.getName().xmlText())) {
            // Add current layer
            path.addLast(layer);
            return true;
        }
        Layer[] subLayers = layer.getLayerArray();
        if (subLayers != null && subLayers.length > 0) {
            // Remember current layer while we're search its' subLayers
            path.addLast(layer);
            for (Layer subLayer : subLayers) {
                if (find(subLayer, layerName, path, lvl + 1)) {
                    return true;
                }
            }
            // None of the subLayers matched, remove current layer from the path as well
            path.removeLast();
        }
        return false;
    }

    private void parseStylesAndLegends(Layer layer) {
        Style[] stylesArray = layer.getStyleArray();
        if (stylesArray == null) {
            return;
        }
        for (Style style : stylesArray) {
            Name name = style.getName();
            if (name == null) {
                continue;
            }
            String styleName = name.xmlText();

            Title title = style.getTitle();
            String styleTitle = title.xmlText();
            if (title != null) {
                styleTitle = title.xmlText();
            }
            // Use styleName if styleTitle is not available
            if (styleTitle == null || styleTitle.isEmpty()) {
                styleTitle = styleName;
            }

            this.styles.put(styleName, styleTitle);

            LegendURL[] lurl = style.getLegendURLArray();
            if (lurl == null || lurl.length == 0 || lurl[0].getOnlineResource() == null) {
                continue;
            }
            /* OnlineResource is in xlink namespace */
            String href = lurl[0].getOnlineResource().newCursor().getAttributeText(new QName("http://www.w3.org/1999/xlink", "href"));
            if (href != null) {
                this.legends.put(styleName + LEGEND_HASHMAP_KEY_SEPARATOR + styleTitle, href);
            }
        }
    }

    private String[] parseKeywords(Layer layer) {
        if (layer.getKeywordList() != null) {
            Keyword[] words = layer.getKeywordList().getKeywordArray();
            if (words != null) {
                String[] keywords = new String[words.length];
                for (int i = 0; i < words.length; i++) {
                    keywords[i] = words[i].xmlText();
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
                formats[i] = tmpformats[i].newCursor().getTextValue();
            }
            return formats;
        }
        return new String[0];
    }

    private String[] parseCRSs(WMTMSCapabilitiesDocument wmtms) {
        SRS[] crss = wmtms.getWMTMSCapabilities().getCapability().getLayer().getSRSArray();
        String[] CRSs = new String[crss.length];
        for (int i = 0; i < crss.length; i++) {
            CRSs[i] = ProjectionHelper.shortSyntaxEpsg(crss[i].newCursor().getTextValue());
        }
        return CRSs;
    }

}
