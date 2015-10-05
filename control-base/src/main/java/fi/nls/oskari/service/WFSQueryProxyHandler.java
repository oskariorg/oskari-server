package fi.nls.oskari.service;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;

/**
 * Custom modifier for wfsquery proxy service. Overrides getConfig and returns proxy config
 * based on wfs layer mapping to given id parameter (requires parameter wfs_layer_id to work).
 */
public class WFSQueryProxyHandler extends ProxyServiceConfig {
    private static final Logger log = LogFactory.getLogger(WFSQueryProxyHandler.class);

    private static final WFSLayerConfigurationService layerConfigurationService = new WFSLayerConfigurationServiceIbatisImpl();
    private static final String PARAM_WFS_LAYER_ID = "wfs_layer_id";

    public boolean isValid() {
        return true;
    }

    /**
     * Returns version of the config populated by wfs service info based on given id parameter.
     * @return
     */
    public ProxyServiceConfig getConfig(final ActionParameters params) {
        ProxyServiceConfig config = new ProxyServiceConfig();
        config.setUrl("-- url missing --");
        config.setUsername(null);
        config.setPassword(null);
        config.setEncoding(getEncoding());
        config.setParamNames(getParamNames());
        config.setHeaders(getHeaders());

        // request WFS input for WPS execute process via wfs layer id
        final int layer_id = ConversionHelper.getInt(params.getHttpParam(PARAM_WFS_LAYER_ID), -1);
        if(layer_id == -1) {
            log.error("Layer id parameter missing", params);
            config.setUrl("--param missing--");
        }

        final WFSLayerConfiguration lc = layerConfigurationService.findConfiguration(layer_id);
        if(lc == null) {
            config.setUrl("--layer configuration for id " + layer_id + " missing--");
        }
        else {
            config.setUrl(lc.getURL());
            config.setUsername(lc.getUsername());
            config.setPassword(lc.getPassword());
        }
        return config;
    }
}
