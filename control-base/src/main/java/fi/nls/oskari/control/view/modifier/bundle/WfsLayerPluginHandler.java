package fi.nls.oskari.control.view.modifier.bundle;

import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;

/**
 *  modifier for WfsLayerPlugin config
 */
public class WfsLayerPluginHandler {

    private static final Logger LOGGER = LogFactory.getLogger(WfsLayerPluginHandler.class);
    public static final String PLUGIN_NAME = "Oskari.mapframework.bundle.mapwfs2.plugin.WfsLayerPlugin";
    public static final String KEY_ID = "id";
    public static final String KEY_ISPUBLISHED = "isPublished";
    public static final String KEY_CONFIG = "config";

    private static final String KEY_HOST = "hostname";
    private static final String KEY_PORT = "port";
    private static final String KEY_PATH = "contextPath";
    private static final String HOST = PropertyUtil.getOptional("oskari.transport.domain");
    private static final String PORT = PropertyUtil.getOptional("oskari.transport.port");
    private static final String PATH = PropertyUtil.getOptional("oskari.transport.url");


    public JSONObject setupWfsLayerPluginConfig(final JSONObject originalPlugin, final String viewType) {
        if(originalPlugin == null) {
            LOGGER.debug("Tried to modify WfsLayerPlugin, but plugin didn't exist!");
            return null;
        }
        if(!PLUGIN_NAME.equals(originalPlugin.optString(KEY_ID))) {
            LOGGER.debug("Tried to modify WfsLayerPlugin, but given JSON isn't WfsLayerPlugin!");
            return null;
        }

        JSONObject config = getConfig(originalPlugin);
        setupIsPublished(config, viewType);

        // setup env-specific wfs config
        if(HOST != null && !config.has(KEY_HOST)) {
            JSONHelper.putValue(config, KEY_HOST, HOST);
        }
        if(PORT != null && !config.has(KEY_PORT)) {
            JSONHelper.putValue(config, KEY_PORT, PORT);
        }

        if(PATH != null && !config.has(KEY_PATH)) {
            JSONHelper.putValue(config, KEY_PATH, PATH);
        }
        return originalPlugin;
    }

    private JSONObject getConfig(JSONObject original) {
        JSONObject config = original.optJSONObject(KEY_CONFIG);
        if(config == null) {
            config = new JSONObject();
            JSONHelper.putValue(original, KEY_CONFIG, config);
        }
        return config;
    }

    private void setupIsPublished(final JSONObject config, final String viewType) {
        if(config.has(KEY_ISPUBLISHED)) {
            return;
        }
        if(viewType.equals(ViewTypes.PUBLISHED)) {
            JSONHelper.putValue(config, KEY_ISPUBLISHED, true);
        }

    }

}
