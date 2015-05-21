package fi.nls.oskari.wfs.util;

import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.domain.map.wfs.WFSParserConfig;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Get Feature engine parser configs for wfs (2.0.0) featuretypes
 */

public class WFSParserConfigs {

    protected static final Logger log = LogFactory
            .getLogger(WFSParserConfigs.class);
    private JSONObject config = null;
    private static WFSLayerConfigurationService wfsService;

    /**
     * Get initial parser config for a feature type
     * @param typename_withPrefix  Feature type name with namespace prefix
     * @return parser config
     */
    public JSONArray getFeatureTypeConfig(String typename_withPrefix) {

        if (this.config == null) readFeatureTypeConfig();
        return JSONHelper.getJSONArray(this.config, typename_withPrefix);

    }

    /**
     * Get default wfs 2.0.0 parser configuration and replaces placeholders
     *
     * @param nsUri          namespace URI
     * @param nameWithPrefix featuretype name with namespace prefix
     * @return
     */
    public JSONArray getDefaultFeatureTypeConfig(String nsUri, String nameWithPrefix) {

        JSONArray default_conf = JSONHelper.getJSONArray(this.config, "default");
        try {
            // Set place holder values
            JSONObject conf = JSONHelper.getJSONObject(default_conf, 0);
            if (conf != null) {
                conf.remove("feature_namespace_uri");
                conf.put("feature_namespace_uri", nsUri);
                JSONObject pconf = conf.optJSONObject("parse_config");
                if (pconf != null) {
                    pconf.remove("root");
                    JSONObject rootns = new JSONObject();
                    rootns.put("rootNS", nsUri);
                    rootns.put("name", nameWithOutPrefix(nameWithPrefix));
                    pconf.put("root", rootns);
                    JSONArray paths = pconf.optJSONArray("paths");
                    // Gml id is always there
                    paths.getJSONObject(0).remove("path");
                    paths.getJSONObject(0).put("path", "/" + nameWithPrefix + "/@gml:id");

                    //TODO: more sophisticated default path conf - use getfeature&count=1 to find out paths automaticly

                    return default_conf;
                }


            }
        } catch (Exception e) {
            log.debug("Creating default fe parser configs failed", e);
        }
        return null;
    }

    private String nameWithOutPrefix(String name) {
        return name.split(":")[name.split(":").length - 1];
    }

    /**
     * Get default parser config and fill placehorders
     *
     * @param wfsl
     * @return
     */
    public JSONArray getDefaultFeatureTypeConfig(WFSLayerConfiguration wfsl) {
        if (wfsl == null) return null;
        JSONArray default_conf = JSONHelper.getJSONArray(this.config, "default");
        try {
            // Set place holder values
            JSONObject conf = JSONHelper.getJSONObject(default_conf, 0);
            if (conf != null) {
                conf.remove("feature_namespace_uri");
                conf.put("feature_namespace_uri", wfsl.getFeatureNamespaceURI());
                JSONObject pconf = conf.optJSONObject("parse_config");
                if (pconf != null) {
                    pconf.remove("root");
                    JSONObject rootns = new JSONObject();
                    rootns.put("rootNS", wfsl.getFeatureNamespaceURI());
                    rootns.put("name", wfsl.getFeatureElement());
                    pconf.put("root", rootns);
                    JSONArray paths = pconf.optJSONArray("paths");
                    // Gml id is always there
                    paths.getJSONObject(0).remove("path");
                    paths.getJSONObject(0).put("path", "/" + wfsl.getFeatureNamespace() + ":" + wfsl.getFeatureElement() + "/@gml:id");

                    return default_conf;
                }


            }
        } catch (Exception e) {
            log.debug("Creating default fe parser configs failed", e);
        }
        return null;
    }

    /**
     * Read initial parser configs for wfs feature types
     * Seed configs are defined in oskari_wfs_parser_config table
     */
    private void readFeatureTypeConfig() {
        if (wfsService == null) {
            wfsService = new WFSLayerConfigurationServiceIbatisImpl();
        }

        try {
            List<WFSParserConfig> confList = wfsService.findWFSParserConfigs(null);
            //Create json for configs
            JSONObject conftmp = new JSONObject();
            for (WFSParserConfig conf : confList) {
                if (conf.getName() != null) {
                    if (conftmp.has(conf.getName())) {
                        // append
                        conftmp.getJSONArray(conf.getName()).put(JSONHelper.createJSONObject(conf.toJSONString()));
                    } else {
                        JSONArray confa = new JSONArray();
                        confa.put(JSONHelper.createJSONObject(conf.toJSONString()));
                        conftmp.put(conf.getName(), confa);
                    }
                }
            }
            if (conftmp.length() > 0) this.config = conftmp;

        } catch (Exception e) {
            log.warn("Failed to query wfs parser configs via SQL client");
        }

    }


}