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
        @Result(property="created", column="created"),
        @Result(property="updated", column="updated"),
        @Result(property="ownerUuid", column="owner_uuid"),
        @Result(property="extent", column="extent"),
        @Result(property="locale", column="locale"),
        @Result(property="fields", column="fields"),
        @Result(property="options", column="options"),
        @Result(property="attributes", column="attributes")
    })
    @Select("SELECT id, feature_count, created, updated, owner_uuid, extent, locale, fields, options, attributes FROM myfeatures_layer WHERE id = #{layerId}")
    MyFeaturesLayer findLayer(UUID layerId);

    @ResultMap("MyFeaturesLayerResult")
    @Select("SELECT id, feature_count, created, updated, owner_uuid, extent, locale, fields, options, attributes FROM myfeatures_layer WHERE owner_uuid = #{ownerUuid}")
    List<MyFeaturesLayer> findLayersByOwnerUuid(String ownerUuid);

    @Insert("INSERT INTO myfeatures_layer "
        + "(id, owner_uuid, locale, fields, options, attributes) VALUES "
        + "(#{id}, #{ownerUuid}, #{locale}::json, #{fields}::json, #{options}::json, #{attributes}::json)")
    public void insertLayer(MyFeaturesLayer layer);

    @Update("UPDATE myfeatures_layer SET "
        + "locale = #{locale}::json,"
        + "fields = #{fields}::json,"
        + "options = #{options}::json,"
        + "attributes = #{attributes}::json,"
        + "updated = current_timestamp "
        + "WHERE id = #{id}")
    public void updateLayer(MyFeaturesLayer layer);

    @Delete("DELETE FROM myfeatures_layer WHERE id = #{layerId}")
    public int deleteLayer(UUID layerId);

    @Delete("DELETE FROM myfeatures_layer WHERE owner_uuid = #{ownerUuid}")
    public int deleteLayersByOwnerUuid(String ownerUuid);

    @Insert("INSERT INTO myfeatures_feature (layer_id, fid, geom, properties) VALUES (#{layerId}, #{feature.fid}, #{feature.geometry}, #{feature.properties}::json)")
    public int insertFeature(UUID layerId, MyFeaturesFeature feature);

    @Select("SELECT fid, geom, properties, created, updated FROM myfeatures_feature WHERE layer_id = #{layerId} AND fid = #{fid}")
    @Results(id = "MyFeaturesFeatureResult", value = {
        @Result(property="fid", column="fid", id=true),
        @Result(property="geometry", column="geom"),
        @Result(property="properties", column="properties"),
        @Result(property="created", column="created"),
        @Result(property="updated", column="updated"),
    })
    public MyFeaturesFeature findFeatureById(UUID layerId, String fid);

    @Select("UPDATE myfeatures_feature SET geom = #{feature.geometry}, properties = #{feature.properties}, updated = current_timestamp WHERE layer_id = #{layerId} AND fid = #{feature.fid}")
    public void updateFeature(UUID layerId, MyFeaturesFeature feature);

    @Delete("DELETE FROM myfeatures_feature WHERE layer_id = #{layerId} AND fid = #{featureId}")
    public void deleteFeature(UUID layerId, String featureId);

    @Select("SELECT fid, geom, properties, created, updated FROM myfeatures_feature WHERE layer_id = #{layerId}")
    @ResultMap("MyFeaturesFeatureResult")
    public List<MyFeaturesFeature> findFeatures(UUID layerId);

    @Select("SELECT fid, geom, properties, created, updated FROM myfeatures_feature WHERE layer_id = #{layerId} AND geom && ST_MakeEnvelope(#{minX}, #{minY}, #{maxX}, #{maxY})")
    @ResultMap("MyFeaturesFeatureResult")
    public List<MyFeaturesFeature> findFeaturesByBbox(UUID layerId, double minX, double minY, double maxX, double maxY);

    @Update("UPDATE myfeatures_feature SET geom = ST_FlipCoordinates(geom), updated = current_timestamp WHERE layer_id = #{layerId}")
    public void swapAxisOrder(UUID layerId);

    // Don't touch `updated` as this is caused by update of the features, not an update on the layer
    @Update("MERGE INTO myfeatures_layer AS a "
        + "USING (SELECT layer_id, COUNT(*) AS count, ST_Extent(geom) AS extent FROM myfeatures_feature WHERE layer_id = #{layerId}) AS b "
        + "ON a.id = b.layer_id "
        + "WHEN MATCHED THEN "
        + "UPDATE SET "
        + "a.feature_count = b.count,"
        + "a.extent = ARRAY [ST_XMin(b.extent), ST_YMin(b.extent), ST_XMax(b.extent), ST_YMax(b.extent)]")
    public void refreshLayerMetadata(UUID layerId);

    @Select("SELECT current_timestamp")
    @Options(useCache = false)
    OffsetDateTime now();

}
