package fi.nls.oskari.control.statistics.db;

import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

/**
 * MyBatis Mapper for the SQL table oskari_statistical_layers
 */
public interface LayerMapper {
    @Select("SELECT oskari_layer_id as oskariLayerId, " +
            "oskari_region_id_tag as oskariRegionIdTag, oskari_name_id_tag as oskariNameIdTag" +
            " FROM oskari_statistical_layers")
    @ResultType(Layer.class)
    public Layer getAll();
}
