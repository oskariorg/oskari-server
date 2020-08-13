package org.oskari.myplaces.service.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import fi.nls.oskari.domain.map.MyPlaceCategory;

public interface MyPlaceCategoryMapper {

    @Results(id = "CategoryResult", value = {
            @Result(property="id", column="id", id=true),
            @Result(property="uuid", column="uuid"),
            @Result(property="isDefault", column="default"),
            @Result(property="publisher_name", column="publisher_name"),
            @Result(property="category_name", column="category_name"),
            @Result(property="options", column="options")
    })
    @Select("SELECT id, uuid, \"default\", publisher_name, category_name, options FROM categories WHERE id = #{id}")
    public MyPlaceCategory getById(long id);

    @ResultMap("CategoryResult")
    @Select("SELECT id, uuid, \"default\", publisher_name, category_name, options FROM categories WHERE uuid = #{uuid}")
    public List<MyPlaceCategory> getByUserId(String uuid);

    @Insert("INSERT INTO categories (uuid, \"default\", publisher_name, category_name, options)"
            + " VALUES (#{uuid}, #{isDefault}, #{publisher_name}, #{category_name}, CAST(#{options} as json))")
    @Options(useGeneratedKeys=true, keyColumn="id", keyProperty="id")
    public void insert(MyPlaceCategory category);

    @Update("UPDATE categories"
            + " SET uuid = #{uuid}"
            + ",\"default\" = #{isDefault}"
            + ",publisher_name = #{publisher_name}"
            + ",category_name = #{category_name}"
            + ",options = CAST(#{options} as json)"
            + " WHERE id = #{id}")
    public void update(MyPlaceCategory category);

    @Delete("DELETE FROM categories WHERE id = #{id}")
    public int delete(long id);

    @Delete("DELETE FROM categories WHERE uuid = #{uuid}")
    public int deleteByUuid(String uuid);


}
