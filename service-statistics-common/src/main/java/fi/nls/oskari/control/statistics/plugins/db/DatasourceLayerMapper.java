package fi.nls.oskari.control.statistics.plugins.db;

import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MyBatis Mapper for the SQL table oskari_statistical_plugin_layers
 */
public interface DatasourceLayerMapper {

    @Select("SELECT datasource_id as datasourceId, layer_id as maplayerId, " +
            "    source_property as sourceProperty, layer_property as layerProperty" +
            " FROM oskari_statistical_layer WHERE " +
            " datasource_id = #{datasourceId}")
    @ResultType(DatasourceLayer.class)
    List<DatasourceLayer> getLayersForDatasource(long datasourceId);
    /*
    {"statistics":{"featuresUrl":"http://dev.paikkatietoikkuna.fi/geoserver/oskari/wfs","regionIdTag":"kuntakoodi","nameIdTag":"kuntanimi"}}
     */
}
