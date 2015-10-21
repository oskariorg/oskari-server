package fi.nls.oskari.control.statistics.plugins;

import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

/**
 * MyBatis Mapper for the SQL table oskari_statistical_datasource_plugins
 */
public interface StatisticalDatasourceMapper {
    @Select("SELECT class_name as className, localized_name_id as localizedNameId" +
            " FROM oskari_statistical_datasource_plugins")
    @ResultType(StatisticalDatasource.class)
    public StatisticalDatasource getAll();
}
