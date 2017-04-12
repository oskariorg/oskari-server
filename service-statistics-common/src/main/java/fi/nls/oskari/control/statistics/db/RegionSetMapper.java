package fi.nls.oskari.control.statistics.db;

import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MyBatis Mapper for the SQL table oskari_maplayers for thematic map layers
 */
public interface RegionSetMapper {
    @Select("SELECT id as oskariLayerId, name as oskariLayerName, url as url, attributes as attributes, srs_name as srs" +
            " FROM oskari_maplayer WHERE type = 'statslayer'")
    @ResultType(RegionSet.class)
    List<RegionSet> getRegionSets();

    @Select("SELECT id as oskariLayerId, name as oskariLayerName, url as url, attributes as attributes, srs_name as srs" +
            " FROM oskari_maplayer WHERE type = 'statslayer' and id = #{id}")
    @ResultType(RegionSet.class)
    RegionSet getRegionSet(long id);
}
