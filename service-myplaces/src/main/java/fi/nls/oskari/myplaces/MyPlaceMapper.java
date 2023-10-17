package fi.nls.oskari.myplaces;

import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * Created by SMAKINEN on 8.7.2015.
 */
public interface MyPlaceMapper {
    @ResultMap("MyPlacesResult")
    @Select("SELECT " +
            " id, " +
            " uuid, " +
            " category_id, " +
            " name, " +
            " attention_text, " +
            " created, " +
            " updated, " +
            " place_desc, " +
            " link, " +
            " image_url, " +
            " ST_ASTEXT(geometry) as wkt, "+
            " ST_SRID(geometry) as srid "+
            " FROM my_places " +
            " WHERE "+
            " uuid = #{uuid} ")
    List<MyPlace> findAllByUuId(String uuid);

    @ResultMap("MyPlacesResult")
    @Select("SELECT " +
            " id, " +
            " uuid, " +
            " category_id, " +
            " name, " +
            " attention_text, " +
            " created, " +
            " updated, " +
            " place_desc, " +
            " link, " +
            " image_url, " +
            " ST_ASTEXT(geometry) as wkt, "+
            " ST_SRID(geometry) as srid "+
            " FROM my_places " +
            " WHERE "+
            " category_id = #{categoryId} ")
    List<MyPlace> findAllByCategoryId(long categoryId);

    @ResultMap("MyPlacesResult")
    @Select("SELECT " +
            " id, " +
            " uuid, " +
            " category_id, " +
            " name, " +
            " attention_text, " +
            " created, " +
            " updated, " +
            " place_desc, " +
            " link, " +
            " image_url, " +
            " ST_ASTEXT(geometry) as wkt, "+
            " ST_SRID(geometry) as srid "+
            " FROM my_places " +
            " WHERE "+
            " id=ANY(#{ids}) ")
    List<MyPlace> findAllByPlaceIdIn(@Param("ids") long[] ids);

    @ResultMap("MyPlacesResult")
    @Select("SELECT " +
            " id, " +
            " uuid, " +
            " category_id, " +
            " name, " +
            " attention_text, " +
            " created, " +
            " updated, " +
            " place_desc, " +
            " link, " +
            " image_url, " +
            " ST_ASTEXT(geometry) as wkt, " +
            " ST_SRID(geometry) as srid " +
            " FROM my_places " +
            " WHERE "+
            " category_id = #{categoryId} " +
            " AND " +
            " ST_INTERSECTS(" +
            "   ST_MAKEENVELOPE(#{minX}, #{minY}, #{maxX}, #{maxY}, #{srid}), " +
        "       geometry)")
    List<MyPlace> findAllByBBOX(@Param("categoryId") int categoryId,
                                @Param("minX") double minX,
                                @Param("minY") double minY,
                                @Param("maxX") double maxX,
                                @Param("maxY") double maxY,
                                @Param("srid") int srid);
    MyPlace findPlace(long id);
    MyPlaceCategory find(long categoryId);
    @Update("update categories set " +
            "   publisher_name = #{publisher_name}" +
            "   where uuid = #{uuid} and id = #{id}")
    int updatePublisherName(Map map);
    List<MyPlaceCategory> findByIds(@Param("list") List<Long> idList);
    List<MyPlaceCategory> freeFind(Map<String, Object> data);
    List<MyPlaceCategory> findAll();
    @Delete("delete from categories where uuid = #{uid}")
    void deleteByUid(String uid);

    @Insert("INSERT INTO my_places (" +
            " uuid, " +
            " category_id, " +
            " name, " +
            " attention_text, " +
            " updated, " +
            " geometry, " +
            " place_desc, " +
            " link, " +
            " image_url " +
            ") " +
            " VALUES (" +
            " #{uuid}, " +
            " #{categoryId}, " +
            " #{name}, " +
            " #{attentionText}, " +
            " now(), " +
            " ST_SetSRID(ST_GeometryFromText(#{wkt}), #{applicationSRID}), " +
            " #{desc}, " +
            " #{link}, " +
            " #{imageUrl} " +
            ")")
    @Options(useGeneratedKeys=true, keyColumn="id", keyProperty="id")
    Long addMyPlace(MyPlace myPlace);

    @Update("UPDATE my_places SET " +
            " category_id = #{categoryId}, " +
            " name = #{name}, " +
            " attention_text = #{attentionText}, " +
            " updated = now(), " +
            " geometry = ST_SetSRID(ST_GeometryFromText(#{wkt}), #{applicationSRID}), " +
            " place_desc = #{desc}, " +
            " link = #{link}, " +
            " image_url = #{imageUrl} " +
            " WHERE "+
            " id = #{id} ")
    Long updateMyPlace(MyPlace myPlace);

    @Delete("DELETE FROM my_places "+
            " WHERE "+
            " id = #{id} ")
    Long deleteMyPlace(long id);

}
