package fi.nls.oskari.map.layer;

import fi.nls.oskari.domain.map.DataProvider;
import org.apache.ibatis.annotations.*;
import org.json.JSONObject;

import java.util.List;

public interface DataProviderMapper {

    @Select("select id, locale from oskari_dataprovider where id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "locale", column = "locale")
    })
    @Options(useCache = true)
    DataProvider find(int id);

    @Select("select id, locale from oskari_dataprovider order by id")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "locale", column = "locale")
    })
    List<DataProvider> findAll();

    @Update("update oskari_dataprovider set locale = #{locale} where id = #{id}")
    void update(JSONObject locale, int id);

    @Delete("delete from oskari_dataprovider where id = #{id}")
    void delete(int id);

    @Insert("insert into oskari_dataprovider (locale) values (#{locale})")
    @Options(useGeneratedKeys=true)
    void insert(final DataProvider dataProvider);

}
