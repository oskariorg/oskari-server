package org.oskari.map.myfeatures.service;

import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.service.ServiceException;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserLayerMapper {

    //UserLayer related
    void insertUserLayer(final UserLayer userlayer);
    int updateUserLayer(final UserLayer userlayer);
    UserLayer findUserLayer(long id);
    List<UserLayer> findUserLayerByUuid(String uuid);
    void deleteUserLayer(final long id) throws ServiceException;
    int updatePublisherName(@Param ("id") long id, @Param ("uuid") String uuid, @Param ("publisher_name") String name);
    String getUserLayerBbox (final long userLayerId);

    //UserLayerData related
    void insertUserLayerData(@Param ("user_layer_data") final UserLayerData userLayerData, @Param("user_layer_id") final long userLayerId, @Param("srid") final int srid);
    int updateUserLayerData(final UserLayerData userLayerData);
    void deleteUserLayerDataByLayerId (final long userLayerId);
    void deleteUserLayerData (final long id);

    /**
     * Returns features given a bbox and layer id.
     * @param layerId
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @param srid
     * @return
     */
    @ResultMap("UserLayerDataResult")
    @Select("SELECT " +
            " id, " +
            " user_layer_id, " +
            " uuid, " +
            " feature_id, " +
            " property_json, " +
            " ST_ASTEXT(geometry) as wkt, " +
            " ST_SRID(geometry) as srid, " +
            " created, " +
            " updated " +
            " FROM user_layer_data " +
            " WHERE "+
            " user_layer_id = #{layerId} " +
            " AND " +
            " ST_INTERSECTS(" +
            "   ST_MAKEENVELOPE(#{minX}, #{minY}, #{maxX}, #{maxY}, #{srid}), " +
        "       geometry)")
    List<UserLayerData> findAllByBBOX(@Param("layerId") int layerId,
                                @Param("minX") double minX,
                                @Param("minY") double minY,
                                @Param("maxX") double maxX,
                                @Param("maxY") double maxY,
                                @Param("srid") int srid);

    /**
     * Returns features given a bbox and layer id.
     * Imitates Geoserver/geotools "loose bbox" sql query.
     * It should be faster than intersects as it relies on _feature bounding box_ intersecting with given
     * bbox instead of the _actual feature geometry_.
     * @param layerId
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @param srid
     * @return
     */
    @ResultMap("UserLayerDataResult")
    @Select("SELECT " +
            " id, " +
            " user_layer_id, " +
            " uuid, " +
            " feature_id, " +
            " property_json, " +
            " ST_ASTEXT(geometry) as wkt, " +
            " ST_SRID(geometry) as srid, " +
            " created, " +
            " updated " +
            " FROM user_layer_data " +
            " WHERE "+
            " user_layer_id = #{layerId} " +
            " AND " +
            " geometry && ST_MAKEENVELOPE(#{minX}, #{minY}, #{maxX}, #{maxY}, #{srid})")
    List<UserLayerData> findAllByLooseBBOX(@Param("layerId") int layerId,
                                      @Param("minX") double minX,
                                      @Param("minY") double minY,
                                      @Param("maxX") double maxX,
                                      @Param("maxY") double maxY,
                                      @Param("srid") int srid);

}
