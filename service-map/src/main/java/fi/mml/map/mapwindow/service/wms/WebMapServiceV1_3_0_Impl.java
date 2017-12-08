package fi.mml.map.mapwindow.service.wms;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import fi.mml.capabilities.KeywordDocument;
import fi.mml.capabilities.LayerDocument.Layer;
import fi.mml.capabilities.LegendURLDocument.LegendURL;
import fi.mml.capabilities.OperationType;
import fi.mml.capabilities.StyleDocument.Style;
import fi.mml.capabilities.WMSCapabilitiesDocument;

/**
 * 1.3.0 implementation of WMS
 */
public class WebMapServiceV1_3_0_Impl extends AbstractWebMapService {

    public WebMapServiceV1_3_0_Impl(String url, String data, String layerName) throws WebMapServiceParseException {
        super(url);
        parseXML(data, layerName);
    }

    public String getVersion() {
        return "1.3.0";
    }

    private void parseXML(String data, String layerName) throws WebMapServiceParseException {
		try {
			WMSCapabilitiesDocument wms = WMSCapabilitiesDocument.Factory.parse(data);

			Layer rootLayer = wms.getWMSCapabilities().getCapability().getLayer();
			LinkedList<Layer> path = new LinkedList<>();
			boolean found = find(rootLayer, layerName, path, 0);
			if (!found) {
                throw new WebMapServiceParseException("Could not find layer");
			}

			for (Layer layer : path) {
			    parseStylesAndLegends(layer);
            }
			this.formats = parseFormats(wms);
            this.CRSs = rootLayer.getCRSArray();

            Layer layer = path.getLast();
            this.queryable = layer.getQueryable();
            this.time = Arrays.stream(layer.getDimensionArray())
                .filter(dimension -> "time".equals(dimension.getName()))
                .findAny()
                .map(d -> Arrays.asList(d.getStringValue().split(",")))
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
        if (layerName.equals(layer.getName())) {
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
			String styleName = style.getName();
			String styleTitle = style.getTitle();
			if (styleTitle == null || styleTitle.isEmpty()) {
				styleTitle = styleName;
			}
			this.styles.put(styleName, styleTitle);

			LegendURL[] lurl = style.getLegendURLArray();
			if (lurl == null || lurl.length == 0 || lurl[0].getOnlineResource() == null) {
				continue;
			}
			/* Online resource is in xlink namespace */
			String href = lurl[0].getOnlineResource().newCursor().getAttributeText(XLINK_HREF);
			if (href != null) {
				this.legends.put(styleName + LEGEND_HASHMAP_KEY_SEPARATOR + styleTitle, href);
			}
		}
	}

    private String[] parseKeywords(Layer layer) {
        KeywordDocument.Keyword[] words = layer.getKeywordList().getKeywordArray();
        if (words == null) {
            return new String[0];
        }
        String[] keywords = new String[words.length];
        for (int i = 0; i < words.length; i++) {
            keywords[i] = words[i].getStringValue();
        }
        return keywords;
    }

    private String[] parseFormats(WMSCapabilitiesDocument wms) {
        OperationType gfi = wms.getWMSCapabilities().getCapability().getRequest().getGetFeatureInfo();
        return gfi == null ? new String[0] : gfi.getFormatArray();
    }

}
