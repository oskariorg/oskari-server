package fi.nls.oskari.myplaces;

import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import org.apache.ibatis.annotations.Delete;
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
}
