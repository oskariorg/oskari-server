package fi.nls.oskari.geoserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.OskariRuntimeException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;

/**
 * Created by SMAKINEN on 1.9.2015.
 */
public class GeoserverPopulator {

    public static final String NAMESPACE = "oskari";
    private static final Logger LOG = LogFactory.getLogger(GeoserverPopulator.class);

    private static final WFSLayerConfigurationService WFS_SERVICE = new WFSLayerConfigurationServiceIbatisImpl();

    public static final String KEY_URL = "url";
    public static final String KEY_USER = "user";
    public static final String KEY_PASSWD = "password";

    public static void setupAll(final String srs)
            throws Exception {

        try{
            MyplacesHelper.setupMyplaces(srs);
        }catch(Exception e){
            LOG.error(e, "Error when setting my places");
            LOG.debug(e.getMessage());
        }

        try{
            AnalysisHelper.setupAnalysis(srs);
        }catch(Exception e){
            LOG.error(e, "Error when setting analysis");
            LOG.debug(e.getMessage());
        }

        try{
            UserlayerHelper.setupUserlayers(srs);
        }catch(Exception e){
            LOG.error(e, "Error when setting user layers");
            LOG.debug(e.getMessage());
        }
    }

    public static String getGeoserverProp(final String module, final String part) {
        final String preferProp = "geoserver." + module + "." + part;
        final String fallbackProp = "geoserver." + part;

        final String prop = PropertyUtil.get(preferProp,
                PropertyUtil.getOptional(fallbackProp));

        if (prop == null) {
            throw new OskariRuntimeException("Geoserver properties not configured! Tried: " + preferProp + " and " + fallbackProp);
        }
        return prop;
    }

    public static Geoserver getGeoserver(String module) {

        final String geoserverBaseUrl = getGeoserverProp(module, KEY_URL); // http://localhost:8080/geoserver
        final String geoserverUser = getGeoserverProp(module, KEY_USER); // admin
        final String geoserverPasswd = getGeoserverProp(module, KEY_PASSWD); // geoserver

        if (geoserverBaseUrl == null || geoserverUser == null || geoserverPasswd == null) {
            throw new OskariRuntimeException("Geoserver properties not configured!");
        }

        // https://github.com/Netflix/feign/wiki/Custom-error-handling
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        return Feign.builder()
                .decoder(new JacksonDecoder(mapper))
                .encoder(new JacksonEncoder(mapper))
                .logger(new feign.Logger.JavaLogger())
                .logLevel(feign.Logger.Level.FULL)
                .requestInterceptor(new BasicAuthRequestInterceptor(geoserverUser, geoserverPasswd))
                .target(Geoserver.class, geoserverBaseUrl + "/rest");
    }

    public static void setupGeoserverConf(OskariLayer layer, String module) {
        layer.setUrl(getGeoserverProp(module, KEY_URL) + "/" + NAMESPACE + "/ows");
        layer.setUsername(getGeoserverProp(module, KEY_USER));
        layer.setPassword(getGeoserverProp(module, KEY_PASSWD));
    }

    public static int setupMyplacesLayer(final String srs) {
        final String name = NAMESPACE + ":my_places";
        OskariLayer baseLayer = LayerHelper.getLayerWithName(name);
        boolean doInsert = baseLayer == null;
        if (doInsert) {
            baseLayer = new OskariLayer();
            baseLayer.setType(OskariLayer.TYPE_WFS);
            baseLayer.setVersion("1.1.0");
            baseLayer.setName(name);
            baseLayer.setLocale(JSONHelper.createJSONObject("{ fi:{name:\"Omat paikat\"},sv:{name:\"My places\"},en:{name:\"My places\"}}"));
            baseLayer.setOpacity(50);
        }
        // setup data producer/layergroup since original doesn't have one
        baseLayer.addDataprovider(LayerHelper.getDataprovider());
        setupGeoserverConf(baseLayer, MyplacesHelper.MODULE_NAME);
        baseLayer.setSrs_name(srs);
        if (!doInsert) {
            LayerHelper.update(baseLayer);
            return baseLayer.getId();
        }
        // insert
        LayerHelper.insert(baseLayer);

        // setup WFS conf with defaults
        WFSLayerConfiguration conf = LayerHelper.getConfig(baseLayer, NAMESPACE);
        conf.setFeatureElement("my_places");
        conf.setFeatureParamsLocales("{\"default\": [\"name\", \"place_desc\",\"link\", \"image_url\"],\"fi\": [\"name\", \"place_desc\",\"link\", \"image_url\"]}");
        WFS_SERVICE.insert(conf);
        return baseLayer.getId();
    }

    public static int setupAnalysisLayer(final String srs) {
        final String name = NAMESPACE + ":analysis_data";
        OskariLayer baseLayer = LayerHelper.getLayerWithName(name);
        boolean doInsert = baseLayer == null;
        if (doInsert) {
            baseLayer = new OskariLayer();
            baseLayer.setType(OskariLayer.TYPE_WFS);
            baseLayer.setVersion("1.1.0");
            baseLayer.setName(name);
            baseLayer.setLocale(JSONHelper.createJSONObject("{ fi:{name:\"Analyysitaso\"},sv:{name:\"Analys\"},en:{name:\"Analyse\"}}"));
            baseLayer.setOpacity(50);
        }
        // setup data producer/layergroup since original doesn't have one
        baseLayer.addDataprovider(LayerHelper.getDataprovider());
        setupGeoserverConf(baseLayer, AnalysisHelper.MODULE_NAME);
        baseLayer.setSrs_name(srs);
        if (!doInsert) {
            LayerHelper.update(baseLayer);
            return baseLayer.getId();
        }
        // insert
        LayerHelper.insert(baseLayer);

        // setup WFS conf with defaults
        WFSLayerConfiguration conf = LayerHelper.getConfig(baseLayer, NAMESPACE);
        conf.setFeatureParamsLocales("{}");
        conf.setFeatureElement("analysis_data");
        WFS_SERVICE.insert(conf);
        return baseLayer.getId();
    }

    public static int setupUserLayer(final String srs) {
        final String name = NAMESPACE + ":vuser_layer_data";
        OskariLayer baseLayer = LayerHelper.getLayerWithName(name);
        boolean doInsert = baseLayer == null;
        if (doInsert) {
            baseLayer = new OskariLayer();
            baseLayer.setType(OskariLayer.TYPE_WFS);
            baseLayer.setVersion("1.1.0");
            baseLayer.setName(name);
            baseLayer.setLocale(JSONHelper.createJSONObject("{ fi:{name:\"Omat aineistot\"},sv:{name:\"User layers\"},en:{name:\"User layers\"}}"));
            baseLayer.setOpacity(80);
        }
        // setup data producer/layergroup since original doesn't have one
        baseLayer.addDataprovider(LayerHelper.getDataprovider());
        setupGeoserverConf(baseLayer, UserlayerHelper.MODULE_NAME);
        baseLayer.setSrs_name(srs);
        if (!doInsert) {
            LayerHelper.update(baseLayer);
            return baseLayer.getId();
        }
        // insert
        LayerHelper.insert(baseLayer);

        // setup WFS conf with defaults
        WFSLayerConfiguration conf = LayerHelper.getConfig(baseLayer, NAMESPACE);
        conf.setFeatureParamsLocales("{}");
        conf.setFeatureElement("vuser_layer_data");
        WFS_SERVICE.insert(conf);
        return baseLayer.getId();
    }
}
