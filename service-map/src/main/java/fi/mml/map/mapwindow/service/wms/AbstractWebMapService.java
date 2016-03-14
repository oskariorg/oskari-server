package fi.mml.map.mapwindow.service.wms;

import fi.mml.capabilities.LayerDocument;
import fi.mml.capabilities.LegendURLDocument;
import fi.mml.capabilities.StyleDocument;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractWebMapService implements WebMapService {

    protected final String LEGEND_HASHMAP_KEY_SEPARATOR = "_";
    /**
     * Available styles key: name, value: onlineResource
     */
    protected Map<String, String> legends = new HashMap<String, String>();
    public String getLegendForStyle(String key) {
        Map<String, String> supportedStyles = this.getSupportedStyles();
        return legends.get(key+LEGEND_HASHMAP_KEY_SEPARATOR+supportedStyles.get(key));
    }
}
