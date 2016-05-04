package fi.mml.map.mapwindow.service.wms;

import fi.mml.capabilities.DimensionDocument.Dimension;
import fi.mml.capabilities.KeywordDocument;
import fi.mml.capabilities.LayerDocument.Layer;
import fi.mml.capabilities.LegendURLDocument.LegendURL;
import fi.mml.capabilities.StyleDocument.Style;
import fi.mml.capabilities.WMSCapabilitiesDocument;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * 
 * 1.3.0 implementation of WMS
 * 
 */
public class WebMapServiceV1_3_0_Impl extends AbstractWebMapService {

	/**
	 * Available styles key: name, value: title
	 */
	private Map<String, String> styles = new HashMap<String, String>();
	
	private boolean isQueryable = false;
	
	/**
	 * Available formats
	 */
	private String[] formats = new String[0];

    private String[] keywords = new String[0];

	private List<String> time = new ArrayList<>();

    private String[] CRSs = new String[0];

	/** url for request */
	private String getCapabilitiesUrl;
	
	/** Logger */
	private Logger log = LogFactory.getLogger(WebMapServiceV1_3_0_Impl.class);

    public String getVersion() {
        return "1.3.0";
    }
	/**
	 * Creates a new object
	 * 
	 * @param url getCapabilitiesUrl
	 * @param data
	 * @param layerName
	 * @throws WebMapServiceParseException 
	 */
	public WebMapServiceV1_3_0_Impl(String url, String data, String layerName) throws WebMapServiceParseException {
		getCapabilitiesUrl = url;
		parseXML(data, layerName);
	}
	
	
	/**
	 * Parses xml and finds layers
	 * 
	 * @param data
	 * @param layerName
	 */
	private void parseXML(String data, String layerName) throws WebMapServiceParseException {
		try {
			
			final WMSCapabilitiesDocument wmtms = WMSCapabilitiesDocument.Factory.parse(data);
			
			/* Gather all root styles in this map, so that there are no duplicates */
			Map<String, String> rootStyles = new HashMap<String, String>();
			
			/* process root layer */
			Layer rootLayer = wmtms.getWMSCapabilities().getCapability().getLayer();
			gatherStylesAndLegends(rootLayer, rootStyles);
			styles.putAll(rootStyles);

			getFormats(wmtms);

            getCRSs(wmtms);
			
			/* continue to childs */
			Layer[] layers = rootLayer.getLayerArray();
			for (Layer layer: layers) {
				parse(layer, layerName, 0, new HashMap<String, String>());	
			}			
			
		} catch (Exception e) {
			throw new WebMapServiceParseException(e);
		}
		
	}
    /**
     * Parses keywords for layer the same way that queryable is done.
     * Not sure of the logic but at least they fail similarly if they fail.
     * @param layer
     * @param layerName
     */
    private void setKeywords(Layer layer, String layerName) {
        // Layername is already compared when this is called so should always be true
        // if the method is called just keeping this because queyable also checks it
        if (layerName.equals(layer.getName()) && layer.getKeywordList() != null) {
            KeywordDocument.Keyword[] words = layer.getKeywordList().getKeywordArray();
            if(words != null) {
                List<String> list = new ArrayList<String>(words.length);
                for( KeywordDocument.Keyword w : words) {
                    list.add(w.getStringValue());
                }
                keywords = list.toArray(keywords);
            }
        }
    }
	
	private void setQueryable(Layer layer, String layerName) {
		if (!isQueryable && layerName.equals(layer.getName())) {
			isQueryable = layer.getQueryable();
		}
	}

	private void setTimeValue(Layer layer) {
		//if (layerName.equals(layer.getName())) {
			for (Dimension dimension : layer.getDimensionArray()) {
				if (dimension.getName().equals("time")) {
					List<String> value;
					String originalValue = dimension.getStringValue();
					if(originalValue == null) {
						value = Collections.emptyList();
					} else if(dimension.getMultipleValues()) {
						String[] split = originalValue.split(",");
						value = new ArrayList(Arrays.asList(split));
					} else {
						value = new ArrayList(Collections.singletonList(originalValue));
					}
					time = value;
				}
			}
		//}
	}
	/**
	 * Gathers styles from given layer to given map
	 *
	 * @param layer
	 * @param foundStyles
	 */
	private void gatherStylesAndLegends(Layer layer, Map<String, String> foundStyles) {
		if (layer.getStyleArray() != null) {
			Style[] stylesArray = layer.getStyleArray();
			for(Style style: stylesArray) {
				String styleName = style.getName();
				String styleTitle = style.getTitle();
				foundStyles.put(styleName, styleTitle);

				LegendURL[] lurl = style.getLegendURLArray();
				if (lurl != null && lurl.length > 0) {
					/* Online resource is in xlink namespace */
					String href = lurl[0].getOnlineResource().newCursor().getAttributeText(new QName("http://www.w3.org/1999/xlink", "href"));
					legends.put(styleName+LEGEND_HASHMAP_KEY_SEPARATOR+styleTitle, href);
				}
			}
		}
	}

	/**
	 * Gathers get feature info formats from feature info
	 * 
	 * @param wmtms
	 */
	private void getFormats(WMSCapabilitiesDocument wmtms) {
		
		String[] tmpformats = wmtms.getWMSCapabilities().getCapability().getRequest().getGetFeatureInfo().getFormatArray();
		formats = new String[tmpformats.length];
		formats = wmtms.getWMSCapabilities().getCapability().getRequest().getGetFeatureInfo().getFormatArray();
		
	}

    /**
     * Get supported crss  of the service from the parent layer
     *
     * @param wmtms
     */
    private void getCRSs(WMSCapabilitiesDocument wmtms) {

        CRSs = wmtms.getWMSCapabilities().getCapability().getLayer().getCRSArray();

    }

	/**
	 * Parses Styles, legends and queryable value from given layer. In case of non valid layer, it tries to do recursion on
	 * sublayers that this layer might contain
	 *  
	 * @param layer that is checked
	 * @param checkedLayerName name of the layer
	 * @param currentLevel current level of recursion
	 * @param foundStyles map where found styles are accumulated
	 */
	private void parse(Layer layer, String checkedLayerName, int currentLevel, Map<String, String> foundStyles) {
		if (currentLevel > 5) {
			throw new RuntimeException("We tried to parse layers to fifth level of recursion, this is too much. Cancel.");
		}
		
		try {
			/* check if this layer is the one we are after */
			boolean valid = true;
			if (layer.getName() == null) {
				valid = false;
			} else if (!layer.getName().equals(checkedLayerName)) {
				valid = false;
		    }			
			
			if (layerHasSublayers(layer)) {
				/* this is a parent for other layers. Since structure is hierarchical
				 * we'll have to collect styles from this one */
				gatherStylesAndLegends(layer, foundStyles);
			} 
		
			if (valid) {
				/* We found the layer we were after, next we must once again 
				 * gather styles and check for queryable value */
				gatherStylesAndLegends(layer, foundStyles);
				setTimeValue(layer);
				setQueryable(layer, checkedLayerName);
                setKeywords(layer, checkedLayerName);
				
				/* Now we have all styles in place, copy those over to final map */
				//log.debug("Parsing for WMS url '" + getCapabilitiesUrl + "' with layer '" + checkedLayerName + "' done, found styles: ");
				for (String styleName : foundStyles.keySet()) {
					// log.debug("StyleName: " + styleName);
					styles.put(styleName, foundStyles.get(styleName));
				}
			} else {
				/* this layer is not valid for our processing. This might be
				 * because of layer not containing name or other required attribute.
				 * Layer design can be done so that sublayers contain needed information,
				 * so we will try recursively on lower levels. */
				if(layerHasSublayers(layer)) {
					for (Layer subLayer: layer.getLayerArray()) {
						parse(subLayer, checkedLayerName, currentLevel+1, foundStyles);				
					}	
				}
			}			
			
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse styles from url '" + getCapabilitiesUrl + "', e");
		}
	}
	
	/**
	 * Returns true if given layer has sublayers
	 * 
	 * @param layer
	 * @return
	 */
	private boolean layerHasSublayers(Layer layer) {
		if(layer.getLayerArray() != null && layer.getLayerArray().length > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public Map<String, String> getSupportedStyles() {
		return styles;
	}
	
	public String getCapabilitiesUrl() {
		return getCapabilitiesUrl;
	}
	
	public String[] getFormats() {
		return formats;
	}
	
	public Map<String, String> getSupportedLegends() {
		return legends;
	}
	
	public boolean isQueryable() {
		return isQueryable;
	}

    public String[] getKeywords() {
        return keywords;
    }

	public List<String> getTime() {
		return time;
	}

    public String[] getCRSs() {
        return CRSs;
    }
}
