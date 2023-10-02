package org.oskari.map.userlayer.service;

import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.service.ServiceException;
import org.apache.ibatis.annotations.Param;

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
}
