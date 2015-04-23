package fi.nls.oskari.wfs.util;

import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Get Feature engine parser configs for wfs (2.0.0) featuretypes
 */

public class WFSParserConfigs {

    protected static final Logger log = LogFactory
            .getLogger(WFSParserConfigs.class);
    private  JSONObject config = null;
    private final String wfsParserConfigs = "/fi/nls/oskari/wfs/util/WFSParserConfigs.json";
    private String defaultConfigTemplate = "";

    public JSONArray getFeatureTypeConfig(String typename_withPrefix){
        if(this.config == null) readFeatureTypeConfig();
        return  JSONHelper.getJSONArray(this.config, typename_withPrefix);

    }
    public JSONArray getDefaultFeatureTypeConfig(WFSLayerConfiguration wfsl){
        if(wfsl == null) return null;
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
                   rootns.put("rootNS",wfsl.getFeatureNamespaceURI());
                   rootns.put("name",wfsl.getFeatureElement());
                   pconf.put("root", rootns);
                   JSONArray paths = pconf.optJSONArray("paths");
                   // Gml id is always there
                   paths.getJSONObject(0).remove("path");
                   paths.getJSONObject(0).put("path", "/" + wfsl.getFeatureNamespace() + ":" + wfsl.getFeatureElement() + "/@gml:id");

                   //TODO: more sophisticated default path conf - use getfeature&count=1 to find out paths automaticly

                   return default_conf;
               }


           }
       }
       catch (Exception e){
           log.debug("Creating default fe parser configs failed", e);
       }
        return null;
    }

    private void readFeatureTypeConfig(){
        InputStream inp = this.getClass().getResourceAsStream(this.wfsParserConfigs);
        if(inp != null) {
            try {
                InputStreamReader reader = new InputStreamReader(inp);
                JSONTokener tokenizer = new JSONTokener(reader);
                this.config = JSONHelper.createJSONObject4Tokener(tokenizer);
                reader.close();
            }
            catch (Exception e){
                log.debug("Reading fe parser configs file:",this.wfsParserConfigs, e);
            }
        }


    }



}