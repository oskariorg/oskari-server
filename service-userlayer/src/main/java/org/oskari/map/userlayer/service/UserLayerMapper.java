package org.oskari.map.userlayer.service;

import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.domain.map.userlayer.UserLayerStyle;
import fi.nls.oskari.service.ServiceException;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserLayerMapper {

    //UserLayer related
    public void insertUserLayerRow(final UserLayer userlayer);
    public int updateUserLayerCols(final UserLayer userlayer);
    public UserLayer findUserLayer(long id);
    public List<UserLayer> findUserLayerByUuid(String uuid);
    public void deleteUserLayerRow(final long id) throws ServiceException;
    public int updatePublisherName(@Param ("id") long id, @Param ("uuid") String uuid, @Param ("publisher_name") String name);
    public String getUserLayerBbox (final long userLayerId);

    //UserLayerStyle related
    public void insertUserLayerStyleRow(final UserLayerStyle userLayerStyle);
    public void deleteUserLayerStyleRow(final long id);
    public UserLayerStyle findUserLayerStyle(final long id);
    public int updateUserLayerStyleCols (final UserLayerStyle userLayeStyle);

    //UserLayerData related
    public void insertUserLayerDataRow(@Param ("user_layer_data") final UserLayerData userLayeData, @Param("user_layer_id") final long userLayerId);
    public int updateUserLayerDataCols(final UserLayerData userLayerData);
    public void deleteUserLayerDataByLayerId (final long userLayerId);
    public void deleteUserLayerDataRow (final long id);

}
