package fi.mml.map.mapwindow.service.wms;

import fi.mml.wms.v111.*;
import fi.mml.wms.v111.Layer.Queryable.Enum;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;

import javax.xml.namespace.QName;
import java.lang.Exception;
import java.util.*;

public class WebMapServiceV1_1_1_Impl extends AbstractWebMapService {

	/**
	 * Available styles, key:name => value:title 
	 */
	private Map<String, String> styles = new HashMap<String, String>();
	
	/**
	 * Available formats
	 */
	private String[] formats = new String[0];
	
	private boolean isQueryable = false;
    private String[] keywords = new String[0];
	private List<String> time = new ArrayList<>();
    public String[] CRSs = new String[0];

	/** Logger */
	private static final Logger log = LogFactory.getLogger(WebMapServiceV1_1_1_Impl.class);
	
	/**
	 * Url for this service
	 */
	private String getCapabilitiesUrl;	
	
	/**
	 * Creates a new 1.1.1 implementation
	 * @param data
	 * @param layerName
	 * 
	 * @throws WebMapServiceParseException if data cannot be parsed from url 
	 */
	public WebMapServiceV1_1_1_Impl(String url, String data, String layerName) throws WebMapServiceParseException {
		getCapabilitiesUrl = url;
		parseXML(data, layerName);
	}

    public String getVersion() {
        return "1.1.1";
    }
	/**
	 * Parses xml and finds layers
	 * 
	 * @param data
	 * @param layerName
	 */
	private void parseXML(String data, String layerName) throws WebMapServiceParseException {
		try {
			final WMTMSCapabilitiesDocument wmtms = WMTMSCapabilitiesDocument.Factory.parse(data);
			
			/* Gather all root styles in this map, so that there are no duplicates */
			Map<String, String> rootStyles = new HashMap<String, String>();
			
			/* process root layer */
			Layer rootLayer = wmtms.getWMTMSCapabilities().getCapability().getLayer();
			gatherStylesAndLegends(rootLayer, rootStyles);
			styles.putAll(rootStyles);
			
			/* Continue to childs */
			Layer[] layers = rootLayer.getLayerArray();
			for (Layer layer: layers) {
				parse(layer, layerName, 0, new HashMap<String, String>());
			}
			
			getFormats(wmtms);

			getCRSs(wmtms);
			
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
    private void setKeywords(final Layer layer, final String layerName) {
        // Layername is already compared when this is called so should always be true
        // if the method is called just keeping this because queyable also checks it
        if (layerName.equals(layer.getName()) && layer.getKeywordList() != null) {
            final Keyword[] words = layer.getKeywordList().getKeywordArray();
            if(words != null) {
                final List<String> list = new ArrayList<String>(words.length);
                for(Keyword w : words) {
                    list.add(w.xmlText());
                }
                keywords = list.toArray(keywords);
            }
        }
    }

	private void setQueryable(Layer layer, String layerName) {
		if (!isQueryable && layerName.equals(layer.getName().newCursor().getTextValue())) {
			Enum queryables = layer.getQueryable();
			if ("1".equals(String.valueOf(queryables))) {
				isQueryable = true;
			}
	    } 
	}

	private void setTimeValue(Layer layer, String layerName) {
		if (layerName.equals(layer.getName())) {
			for (Extent extent : layer.getExtentArray()) {
				if (extent.getName().equals("time")) {
					time = new ArrayList(Collections.singletonList(extent.toString()));
				}
			}
		}
	}
	
	/**
	 * Gathers styles from given layer to given map
	 * 
	 * @param layer
	 * @param foundStyles
	 */
	private void gatherStylesAndLegends(Layer layer, Map<String, String> foundStyles) {
		if (layer.getStyleArray() == null) {
			return;
		}
		Style[] stylesArray = layer.getStyleArray();
		for(Style style: stylesArray) {

			if (style.getName().newCursor() == null || style.getTitle().newCursor() == null) {
				continue;
			}
			String styleName = style.getName().newCursor().getTextValue();
			String styleTitle = style.getTitle().newCursor().getTextValue();
			if(styleTitle == null || styleTitle.isEmpty()) {
				styleTitle = styleName;
			}
			foundStyles.put(styleName, styleTitle);

			LegendURL[] lurl = style.getLegendURLArray();
			if (lurl == null || lurl.length == 0 || lurl[0].getOnlineResource() == null) {
				continue;
			}
			/* OnlineResource is in xlink namespace */
			String href = lurl[0].getOnlineResource().newCursor().getAttributeText(new QName("http://www.w3.org/1999/xlink", "href"));
			if (href != null) {
				legends.put(styleName, href);
			}
		}
	}
	/**
	 * Gathers get feature info formats from feature info
	 * 
	 * @param wmtms
	 */
	private void getFormats(WMTMSCapabilitiesDocument wmtms) {
		
		// GetFeatureifo element is optional. We dont want throw null pointer exception from it.
		if (wmtms.getWMTMSCapabilities().getCapability().getRequest().getGetFeatureInfo() != null) {
			Format[] tmpformats = wmtms.getWMTMSCapabilities().getCapability().getRequest().getGetFeatureInfo().getFormatArray();
			
			formats = new String[tmpformats.length];
			
			for (int i = 0; i < tmpformats.length; i++) {
				formats[i] = tmpformats[i].newCursor().getTextValue();
			}
		}
		
	}

	/**
	 * Get supported crss  of the service from the parent layer
	 *
	 * @param wmtms
	 */
	private void getCRSs(WMTMSCapabilitiesDocument wmtms) {

		SRS[] crss = wmtms.getWMTMSCapabilities().getCapability().getLayer().getSRSArray();

		CRSs = new String[crss.length];

		for (int i = 0; i < crss.length; i++) {
			CRSs[i] = ProjectionHelper.shortSyntaxEpsg(crss[i].newCursor().getTextValue());
		}

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
			} else if (!layer.getName().newCursor().getTextValue().equals(checkedLayerName)) {
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
				setTimeValue(layer, checkedLayerName);
				setQueryable(layer, checkedLayerName);
                setKeywords(layer, checkedLayerName);
				
				/* Now we have all styles in place, copy those over to final map */
				for (String styleName : foundStyles.keySet()) {
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
