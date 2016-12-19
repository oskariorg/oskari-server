package fi.nls.oskari.control.statistics.plugins.db;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MyBatis Mapper for the SQL table oskari_statistical_plugin_layers
 */
public interface DatasourceLayerMapper {

    @Select("SELECT datasource_id, layer_id, config" +
            " FROM oskari_statistical_layer WHERE " +
            " datasource_id = #{datasourceId}")
    @Results({
            @Result(property = "datasourceId", column = "datasource_id"),
            @Result(property = "maplayerId", column = "layer_id")
    })
    @ResultType(DatasourceLayer.class)
    List<DatasourceLayer> getLayersForDatasource(long datasourceId);
    /*
    {"statistics":{"featuresUrl":"http://dev.paikkatietoikkuna.fi/geoserver/oskari/wfs","regionIdTag":"kuntakoodi","nameIdTag":"kuntanimi"}}
     */
}
