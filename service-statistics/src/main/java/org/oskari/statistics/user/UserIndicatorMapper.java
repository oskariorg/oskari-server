package org.oskari.statistics.user;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * MyBatis Mapper for the SQL table oskari_statistical_indicator for thematic map user indicators.
 * The database currently supports multiple years/regionsets per indicator, but these queries do not so the API
 * will change a bit in the future.
 */
public interface UserIndicatorMapper {

    // TODO: support multiple year/regionsets
    @Select("SELECT" +
            "    i.id," +
            "    i.user_id," +
            "    i.title," +
            "    i.source," +
            "    i.description," +
            "    i.published," +
            "    i.created," +
            "    i.updated," +
            "    d.year," +
            "    d.regionset_id " +
            "FROM" +
            "    oskari_statistical_indicator i" +
            "    LEFT JOIN oskari_statistical_indicator_data d ON i.id = d.indicator_id " +
            "WHERE" +
            "    i.user_id =  #{userId} " +
            "ORDER BY i.id, d.regionset_id, d.year")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "regionsetId", column = "regionset_id")
    })
    @ResultType(UserIndicatorDataRow.class)
    List<UserIndicatorDataRow> findByUser(long userId);

    // TODO: support multiple year/regionsets
    @Select("SELECT" +
            "    i.id," +
            "    i.user_id," +
            "    i.title," +
            "    i.source," +
            "    i.description," +
            "    i.published," +
            "    i.created," +
            "    i.updated," +
            "    d.year," +
            "    d.regionset_id " +
            "FROM" +
            "    oskari_statistical_indicator i" +
            "    LEFT JOIN oskari_statistical_indicator_data d ON i.id = d.indicator_id " +
            "WHERE" +
            "    i.id = #{id} " +
            "ORDER BY i.id, d.regionset_id, d.year")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "regionsetId", column = "regionset_id")
    })
    @ResultType(UserIndicatorDataRow.class)
    List<UserIndicatorDataRow> findById(long id);


    @Select("SELECT data FROM oskari_statistical_indicator_data" +
            " WHERE indicator_id = #{indicator} AND regionset_id = #{regionset} AND year = #{year}")
    String getData(@Param("indicator") long indicator, @Param("regionset") long regionset, @Param("year") int year);


    @Delete("delete from oskari_statistical_indicator where id = #{id} AND user_id = #{userId}")
    int delete(@Param("id") long id, @Param("userId") long userId);

    @Delete("delete from oskari_statistical_indicator where user_id = #{userId}")
    int deleteByUser(long userId);

    @Insert("INSERT INTO oskari_statistical_indicator_data"
            + " (indicator_id, regionset_id, year, data)"
            + " VALUES (#{indicator}, #{regionset}, #{year}, #{data})")
    void addData(@Param("indicator") long indicator, @Param("regionset") long regionset, @Param("year") int year, @Param("data") String data);

    @Delete("DELETE FROM oskari_statistical_indicator_data" +
            " WHERE indicator_id = #{indicator} AND regionset_id = #{regionset} AND year = #{year}")
    int deleteData(@Param("indicator") long indicator, @Param("regionset") long regionset, @Param("year") int year);

    @Insert("INSERT INTO oskari_statistical_indicator"
            + " (user_id, title, source, description, published)"
            + " VALUES (#{userId}, #{title}, #{source}, #{description}, #{published})")
    @Options(useGeneratedKeys=true, keyColumn = "id", keyProperty = "id")
    void addIndicator(UserIndicatorDataRow row);

    @Update("update oskari_statistical_indicator set" +
            "    title = #{title}," +
            "    source = #{source}," +
            "    description = #{description}," +
            "    published = #{published}," +
            "    updated = #{updated}" +
            "    where id = #{id} AND user_id = #{userId}")
    int updateIndicator(UserIndicatorDataRow row);
}
