package fi.nls.oskari.printout.config;

import java.util.Properties;

public enum ConfigValue {

    /* 1.26 */
    
    MAPPRODUCER_LOCALURL_MATCH("mapproducer.localurl.match"),
    
    MAPPRODUCER_LOCALURL_PREFIX("mapproducer.localurl.prefix"),

    MAPPRODUCER_USERAGENT("mapproducer.useragent"),

    MAPPRODUCER_REFERER("mapproducer.referer"),

    MAPPRODUCER_LOGO_RESOURCE("mapproducer.logo.resource"),

    MAPPRODUCER_IMAGE_UPSCALE_ALGORITHM("mapproducer.image.upscale"),

    LAYER_URLTEMPLATE_LOCALHOST("layer.urltemplate.localhost"),

    /* */

    LAYER_URLTEMPLATE_MYPLACES("layer.urltemplate.myplaces"),

    /* */
    LAYER_URLTEMPLATE_MYPLACES_LAYERS("layer.urltemplate.myplaces.layers"),

    /* */
    LAYER_URLTEMPLATE_WFSLAYER_LEGACY("layer.urltemplate.wfslayer.legacy"),

    /* */
    LAYER_URLTEMPLATE_WFSLAYER("layer.urltemplate.wfslayer"),

    /* */
    LAYER_URLTEMPLATE_STATSLAYER("layer.urltemplate.statslayer"),

    /* */
    LAYER_CACHE_EXCLUDE("layer.cache.exclude"),

    /* */
    LAYERDEFINITION_CREDENTIALS_MYPLACES("layer.credentials.myplaces"),

    /* */
    LAYER_TIMEOUT_SECONDS("layer.timeout.seconds"),

    /* */
    LAYER_TEMPLATE("layer.template"),

    /* */
    MAPPRODUCER_MAXEXTENT("mapproducer.maxextent"),

    /* */
    LAYER_TILES_URL_WHITELIST("layer.tiles.url.whitelist"),

    /* For allowing tiles from transport to show in printout */
    LAYER_TILES_URL_TRANSPORT("layer.transport.url"),

    /* */
    EPSGCODE("epsgCode"),

    /* */
    GRIDSUBSETNAME("gridSubsetName"),

    /* */
    GRIDRESOURCE("gridResource"),

    /* */
    GEOJSON_DEBUG("geojson.debug"),

    /* */
    SCALE_RESOLVER("scale.resolver"),

    /* */
    LAYERSURL("layersURL"),

    REDIS_BLOB_CACHE("redis.blobcache"),
    REDIS_HOST("redis.host"),
    REDIS_PORT("redis.port"),

    /* temp fix to 'support' ditching useless minor scale resolution maps */
    MAPLINK_ZOOM_OFFSET("maplink.zoom.offset")

    ;

    public static final String CONFIG_SYSTEM_PROPERTY = "fi.nls.oskari.imaging.config";
    public static final String DEFAULT_PROPERTIES = "default.properties";

    private String key;

    private ConfigValue(String key) {
        this.key = key;
    }

    public String getConfigProperty(Properties props, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public String getConfigProperty(Properties props) {
        return props.getProperty(key);
    }

    public Integer getConfigProperty(Properties props, Integer defaultValue) {
        String val = props.getProperty(key);
        if (val == null) {
            return defaultValue;
        }
        return Integer.parseInt(val, 10);
    }
}
