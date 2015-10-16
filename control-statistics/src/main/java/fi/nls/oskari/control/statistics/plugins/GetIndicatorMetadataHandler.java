package fi.nls.oskari.control.statistics.plugins;

import java.util.Collection;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;

/**
 * This interface gives the relevant information for all the indicators to the frontend.
 * This information can be subsequently used to query the actual indicator data.
 * 
 * - action_route=GetIndicatorMetadataData
 * 
 * eg.
 * OSKARI_URL&action_route=GetIndicatorMetaData
 * Response is in JSON, and contains the indicator metadata for each plugin separately.
 */
@OskariActionRoute("GetIndicatorMetadata")
public class GetIndicatorMetadataHandler extends ActionHandler {
    /**
     * For now, this uses prettymuch static global store for the plugins.
     * In the future it might make sense to inject the pluginManager references to different using controllers.
     */
    private StatisticalDatasourcePluginManager pluginManager;
    
    @Override
    public void handleAction(ActionParameters ap) throws ActionException {
        Collection<StatisticalDatasourcePlugin> plugins = pluginManager.getPlugins();
        for (StatisticalDatasourcePlugin plugin : plugins) {
            pluginManager.getLocalizedPluginName(plugin.getClass());
        }
    }

}
