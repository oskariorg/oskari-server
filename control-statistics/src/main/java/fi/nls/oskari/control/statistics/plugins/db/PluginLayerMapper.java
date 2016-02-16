package fi.nls.oskari.control.statistics.plugins.db;

import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

/**
 * MyBatis Mapper for the SQL table oskari_statistical_plugin_layers
 */
public interface PluginLayerMapper {
    @Select("SELECT plugin_id as pluginId, plugin_layer_id as pluginLayerId,  oskari_layer_id as oskariLayerId" +
            " FROM oskari_statistical_plugin_layers WHERE " +
            " plugin_id = 'fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin'")
    @ResultType(PluginLayer.class)
    public PluginLayer getAll();

    @Select("SELECT plugin_id as pluginId, plugin_layer_id as pluginLayerId,  oskari_layer_id as oskariLayerId" +
            " FROM oskari_statistical_plugin_layers WHERE " +
            " plugin_id = #{pluginId}")
    @ResultType(PluginLayer.class)
    public PluginLayer getAllForPlugin(String pluginId);
}
