package fi.nls.oskari.myplaces;

import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * Created by SMAKINEN on 8.7.2015.
 */
public interface MyPlaceMapper {
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
            " ST_SetSRID(ST_GeometryFromText(#{geomAsText}), #{srid}), " +
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
            " geometry = ST_SetSRID(ST_GeometryFromText(#{geomAsText}), #{srid}), " +
            " place_desc = #{desc}, " +
            " link = #{link}, " +
            " image_url = #{imageUrl} " +
            " WHERE "+
            " id = #{id} ")
    Long updateMyPlace(MyPlace myPlace);
}
