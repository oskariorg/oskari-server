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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Injects datasource list to statsgrid config

 {
     "datasources" : [{
         "id" : 1,
         "name" : "Omat indikaattorit",
         "type" : "user",
         "regionsets": [1]
     }, {
         "id" : 2,
         "name" : "SotkaNET",
         "type" : "system",
         "info" : {
            "url" : "http://moreinfoaboutthis.here
         },
         "regionsets": [1, 2]
     }, {
         "id" : 3,
         "name" : "KHR",
         "type" : "system",
         "regionsets": [1]
     }],
     "regionsets": [{
        "id": 1,
        "name": "Municipalities"
     },{
         "id": 2,
         "name": "NUTS 1"
     }]
 }
 */
@OskariViewModifier("statsgrid")
public class StatsgridHandler extends BundleHandler {

    private static final String KEY_DATASOURCES = "sources";
    private static final String KEY_INFO = "info";
    private static final String KEY_REGIONSETS = "regionsets";

    public boolean modifyBundle(final ModifierParams params) throws ModifierException {
        final JSONObject config = getBundleConfig(params.getConfig());

        if (config == null) {
            return false;
        }
        final String language = params.getLocale().getLanguage();

        List<StatisticalDatasource> datasources = StatisticalDatasourcePluginManager.getInstance().getDatasources();

        List<JSONObject> sources = datasources.stream()
                .map(src -> toJSON(src, language))
                .collect(Collectors.toList());
        JSONHelper.putValue(config, KEY_DATASOURCES, new JSONArray(sources));

        Collection<JSONObject> regionsets = datasources.stream()
                // get layers from sources
                .map(src -> src.getLayers())
                .flatMap(layerList -> layerList.stream())
                // get distinct layers by creating a map with layer ids as keys
                .collect(Collectors.toMap(DatasourceLayer::getMaplayerId, Function.identity(), (a,b) -> a))
                .values().stream()
                // check permissions
                .filter(l -> l.hasPermission(params.getUser()))
                .sorted(Comparator.comparing(DatasourceLayer::getOrderNumber))
                // write to JSON
                .map(l -> toJSON(l, language))
                .collect(Collectors.toList());

        JSONHelper.putValue(config, KEY_REGIONSETS, new JSONArray(regionsets));
        return false;
    }

    private String getType(final String plugin) {
        if(UserIndicatorsStatisticalDatasourceFactory.TYPE.equalsIgnoreCase(plugin)) {
            // with type = user -> user can save data as own indicator
            return "user";
        }
        return "system";
    }

    private JSONObject toJSON(StatisticalDatasource src, String language) {
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
        return item;
    }

    private JSONObject toJSON(DatasourceLayer layer, String language) {
        JSONObject item = new JSONObject();
        JSONHelper.putValue(item, KEY_ID, layer.getMaplayerId());
        JSONHelper.putValue(item, KEY_NAME, layer.getTitle(language));
        return item;
    }
}
