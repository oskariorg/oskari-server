package org.oskari.map.myfeatures.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;

public interface MyFeaturesMapper {

    @Results(id = "MyFeaturesLayerResult", value = {
        @Result(property="id", column="id", id=true),
        @Result(property="featureCount", column="feature_count"),
        @Result(property="opacity", column="opacity"),
        @Result(property="published", column="published"),
        @Result(property="created", column="created"),
        @Result(property="updated", column="updated"),
        @Result(property="ownerUuid", column="owner_uuid"),
        @Result(property="extent", column="extent"),
        @Result(property="locale", column="locale"),
        @Result(property="fields", column="fields"),
        @Result(property="options", column="options"),
        @Result(property="attributes", column="attributes")
    })
    @Select("SELECT id, feature_count, opacity, published, created, updated, owner_uuid, extent, locale, fields, options, attributes FROM myfeatures_layer WHERE id = #{layerId}")
    MyFeaturesLayer findLayer(UUID layerId);

    @ResultMap("MyFeaturesLayerResult")
    @Select("SELECT id, feature_count, opacity, published, created, updated, owner_uuid, extent, locale, fields, options, attributes FROM myfeatures_layer WHERE owner_uuid = #{ownerUuid}")
    List<MyFeaturesLayer> findLayersByOwnerUuid(String ownerUuid);

    @Insert("INSERT INTO myfeatures_layer "
        + "(id, feature_count, opacity, published, owner_uuid, created, updated, locale, fields, options, attributes) VALUES "
        + "(#{id}, 0, #{opacity}, #{published}, #{ownerUuid}, #{created}, #{updated}, #{locale}::json, #{fields}::json, #{options}::json, #{attributes}::json)")
    public void insertLayer(MyFeaturesLayer layer);

    @Update("UPDATE myfeatures_layer SET "
        + "opacity = #{opacity},"
        + "published = #{published},"
        + "locale = #{locale}::json,"
        + "fields = #{fields}::json,"
        + "options = #{options}::json,"
        + "attributes = #{attributes}::json,"
        + "updated = #{updated} "
        + "WHERE id = #{id}")
    public void updateLayer(MyFeaturesLayer layer);

    @Delete("DELETE FROM myfeatures_layer WHERE id = #{layerId}")
    public int deleteLayer(UUID layerId);

    @Delete("DELETE FROM myfeatures_layer WHERE owner_uuid = #{ownerUuid}")
    public int deleteLayersByOwnerUuid(String ownerUuid);

    @Insert("INSERT INTO myfeatures_feature (layer_id, created, updated, fid, geom, properties) VALUES (#{layerId}, #{feature.created}, #{feature.updated}, #{feature.fid}, #{feature.geometry}, #{feature.properties}::json)")
    @Options(useGeneratedKeys = true, keyProperty = "feature.id", keyColumn = "id")
    public void insertFeature(UUID layerId, MyFeaturesFeature feature);

    @Select("SELECT id, created, updated, fid, geom, properties FROM myfeatures_feature WHERE id = #{featureId}")
    @Results(id = "MyFeaturesFeatureResult", value = {
        @Result(property="id", column="id", id=true),
        @Result(property="created", column="created"),
        @Result(property="updated", column="updated"),
        @Result(property="fid", column="fid"),
        @Result(property="geometry", column="geom"),
        @Result(property="properties", column="properties")
    })
    public MyFeaturesFeature findFeatureById(long featureId);

    @Select("UPDATE myfeatures_feature SET updated = #{updated}, fid = #{fid}, geom = #{geometry}, properties = #{properties} WHERE id = #{id}")
    public void updateFeature(MyFeaturesFeature feature);

    @Delete("DELETE FROM myfeatures_feature WHERE id = #{featureId}")
    public void deleteFeature(long featureId);

    @Select("SELECT id, created, updated, fid, geom, properties FROM myfeatures_feature WHERE layer_id = #{layerId}")
    @ResultMap("MyFeaturesFeatureResult")
    public List<MyFeaturesFeature> findFeatures(UUID layerId);

    @Select("SELECT id, created, updated, fid, geom, properties FROM myfeatures_feature WHERE layer_id = #{layerId} AND geom && ST_MakeEnvelope(#{minX}, #{minY}, #{maxX}, #{maxY})")
    @ResultMap("MyFeaturesFeatureResult")
    public List<MyFeaturesFeature> findFeaturesByBbox(UUID layerId, double minX, double minY, double maxX, double maxY);

    @Update("UPDATE myfeatures_feature SET geom = ST_FlipCoordinates(geom), updated = #{now} WHERE layer_id = #{layerId}")
    public void swapAxisOrder(UUID layerId, OffsetDateTime now);

    // Don't touch `updated` as this is caused by update of the features, not an update on the layer
    @Update(
          "UPDATE myfeatures_layer a SET feature_count = b.count, extent = b.extent FROM ("
        + "  SELECT cnt AS count, ARRAY [ST_XMin(xtent), ST_YMin(xtent), ST_XMax(xtent), ST_YMax(xtent)] AS extent "
        + "  FROM ("
        + "    SELECT COUNT(*) AS cnt, ST_Extent(geom) AS xtent FROM myfeatures_feature WHERE layer_id = #{layerId}"
        + "  )"
        + ") AS b "
        + "WHERE a.id = #{layerId}")
    public void refreshLayerMetadata(UUID layerId);

    @Select("SELECT current_timestamp")
    @Options(useCache = false)
    OffsetDateTime now();

}
