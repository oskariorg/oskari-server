package fi.nls.oskari.control.statistics.plugins.db;

import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MyBatis Mapper for the SQL table oskari_statistical_datasource
 */
public interface StatisticalDatasourceMapper {

    @Select("SELECT id, locale, config, plugin" +
            " FROM oskari_statistical_datasource")
    @ResultType(StatisticalDatasource.class)
    List<StatisticalDatasource> getAll();
}
