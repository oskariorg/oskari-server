package fi.nls.oskari.control.statistics.plugins;

import org.apache.ibatis.annotations.Select;

/**
 * MyBatis Mapper for the SQL table oskari_statistical_datasource_plugins
 */
public interface StatisticalDatasourceMapper {
    @Select("SELECT class_name, localized_name_id" +
            " FROM oskari_statistical_datasource_plugins")
    public StatisticalDatasource getAll();
}
