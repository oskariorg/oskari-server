package fi.nls.oskari.control.statistics.plugins.db;

import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

/**
 * MyBatis Mapper for the SQL table oskari_statistical_datasource_plugins
 */
public interface StatisticalDatasourceMapper {
    @Select("SELECT class_name as className, locale as locale" +
            " FROM oskari_statistical_datasource_plugins")
    @ResultType(StatisticalDatasource.class)
    public StatisticalDatasource getAll();
}
