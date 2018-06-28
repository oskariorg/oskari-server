package fi.nls.oskari.control;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePluginManager;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.control.statistics.plugins.user.UserIndicatorsStatisticalDatasourceFactory;
import fi.nls.oskari.control.view.modifier.bundle.BundleHandler;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONArray;
import org.json.JSONObject;
import static fi.nls.oskari.control.ActionConstants.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Injects datasource list to statsgrid config

 {
     "datasources" : [{
         "id" : 1,
         "name" : "Omat indikaattorit",
         "type" : "user"
     }, {
         "id" : 2,
         "name" : "SotkaNET",
         "type" : "system",
         "info" : {
            "url" : "http://moreinfoaboutthis.here
        }
     }, {
         "id" : 3,
         "name" : "KHR",
         "type" : "system"
     }]
 }
 */
@OskariViewModifier("statsgrid")
public class StatsgridHandler extends BundleHandler {

    private static final String KEY_DATASOURCES = "sources";
    private static final String KEY_INFO = "info";
    private static final String KEY_REGIONSETS = "regionsets";

    private static final StatisticalDatasourcePluginManager pluginManager = StatisticalDatasourcePluginManager.getInstance();

    public boolean modifyBundle(final ModifierParams params) throws ModifierException {
        final JSONObject config = getBundleConfig(params.getConfig());

        if (config == null) {
            return false;
        }
        final String language = params.getLocale().getLanguage();
        JSONArray sourcesList = new JSONArray();
        JSONHelper.putValue(config, KEY_DATASOURCES, sourcesList);

        List<StatisticalDatasource> list = pluginManager.getDatasources();
        Map<Long, DatasourceLayer> regionsets = new HashMap<>();
        for(StatisticalDatasource src : list) {
            JSONObject item = new JSONObject();
            JSONHelper.putValue(item, KEY_ID, src.getId());
            JSONHelper.putValue(item, KEY_NAME, src.getName(language));
            JSONHelper.putValue(item, KEY_TYPE, getType(src.getPlugin()));
            JSONHelper.putValue(item, KEY_INFO, src.getConfigJSON().optJSONObject(KEY_INFO));
            // add layer ids as available regionsets for the datasource
            JSONHelper.putValue(item, KEY_REGIONSETS, new JSONArray(src
                    .getLayers()
                    .stream()
                    .map(DatasourceLayer::getMaplayerId)
                    .collect(Collectors.toSet())));

            sourcesList.put(item);
            for(DatasourceLayer layer : src.getLayers()) {
                regionsets.put(layer.getMaplayerId(), layer);
            }
        }

        JSONArray regionsetList = new JSONArray();
        JSONHelper.putValue(config, KEY_REGIONSETS, regionsetList);
        // TODO: check permissions
        for(DatasourceLayer layer : regionsets.values()) {
            JSONObject item = new JSONObject();
            JSONHelper.putValue(item, KEY_ID, layer.getMaplayerId());
            JSONHelper.putValue(item, KEY_NAME, layer.getTitle(language));
            regionsetList.put(item);
        }
        return false;
    }

    private String getType(final String plugin) {
        if(UserIndicatorsStatisticalDatasourceFactory.TYPE.equalsIgnoreCase(plugin)) {
            // with type = user -> user can save data as own indicator
            return "user";
        }
        return "system";
    }
}
