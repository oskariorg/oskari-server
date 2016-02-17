package fi.nls.oskari.control.statistics.db;

import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

/**
 * MyBatis Mapper for the SQL table oskari_maplayers
 */
public interface LayerMetadataMapper {
    @Select("SELECT id as oskariLayerId, name as oskariLayerName, url as url" +
            " FROM oskari_maplayer")
    @ResultType(LayerMetadata.class)
    public LayerMetadata getAllMetadata();
}
