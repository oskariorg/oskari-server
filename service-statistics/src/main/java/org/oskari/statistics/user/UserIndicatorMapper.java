package org.oskari.statistics.user;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * MyBatis Mapper for the SQL table oskari_user_indicator for thematic map user indicators.
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
            "    d.year," +
            "    d.regionset_id " +
            "FROM" +
            "    oskari_user_indicator i" +
            "    JOIN oskari_user_indicator_data d ON i.id = d.indicator_id " +
            "WHERE" +
            "    i.user_id =  #{userId}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "material", column = "regionset_id")
    })
    @ResultType(UserIndicator.class)
    List<UserIndicator> findByUser(long userId);

    // TODO: support multiple year/regionsets
    @Select("SELECT" +
            "    i.id," +
            "    i.user_id," +
            "    i.title," +
            "    i.source," +
            "    i.description," +
            "    i.published," +
            "    d.year," +
            "    d.regionset_id " +
            "FROM" +
            "    oskari_user_indicator i" +
            "    JOIN oskari_user_indicator_data d ON i.id = d.indicator_id " +
            "WHERE" +
            "    i.id = #{id}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "material", column = "regionset_id")
    })
    @ResultType(UserIndicator.class)
    UserIndicator findById(long id);


    @Select("SELECT data FROM oskari_user_indicator_data" +
            " WHERE indicator_id = #{indicator} AND regionset_id = #{regionset} AND year = #{year}")
    String getData(@Param("indicator") long indicator, @Param("regionset") long regionset, @Param("year") int year);


    @Delete("delete from oskari_user_indicator where id = #{id} AND user_id = #{userId}")
    int delete(@Param("id") long id, @Param("userId") long userId);

    @Delete("delete from oskari_user_indicator where user_id = #{userId}")
    void deleteByUser(long userId);
}
