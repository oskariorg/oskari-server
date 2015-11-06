package fi.nls.oskari.control.statistics.plugins.sotka.db;

import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

/**
 * MyBatis Mapper for the SQL table oskari_statistical_sotka_layers
 */
public interface SotkaLayerMapper {
    @Select("SELECT sotka_layer_id as sotkaLayerId,  oskari_layer_name as oskariLayerName" +
            " FROM oskari_statistical_sotka_layers")
    @ResultType(SotkaLayer.class)
    public SotkaLayer getAll();
}
