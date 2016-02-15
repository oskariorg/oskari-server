package fi.nls.oskari.control.statistics.plugins.sotka.db;

import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

/**
 * MyBatis Mapper for the SQL table oskari_statistical_plugin_layers
 */
public interface SotkaLayerMapper {
    @Select("SELECT plugin_layer_id as sotkaLayerId,  oskari_layer_id as oskariLayerId" +
            " FROM oskari_statistical_plugin_layers WHERE " +
            " plugin_id = 'fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin'")
    @ResultType(SotkaLayer.class)
    public SotkaLayer getAll();
}
