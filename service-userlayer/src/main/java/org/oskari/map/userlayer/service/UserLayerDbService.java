package org.oskari.map.userlayer.service;

import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.domain.map.userlayer.UserLayerStyle;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.ServiceException;

import java.util.List;

public abstract class UserLayerDbService extends OskariComponent {
    //UserLayer related
    public abstract int insertUserLayer(final UserLayer userlayer, final UserLayerStyle userLayerStyle, final List<UserLayerData> userLayerDataList) throws ServiceException;

    public abstract int updateUserLayerCols(final UserLayer userlayer);

    public abstract UserLayer getUserLayerById(long id);

    public abstract List<UserLayer> getUserLayerByUuid(String uuid);

    public abstract void deleteUserLayerById(final long id) throws ServiceException;

    public abstract void deleteUserLayer(final UserLayer userlayer) throws ServiceException;

    public abstract void deleteUserLayersByUuid(String uuid) throws ServiceException;

    public abstract int updatePublisherName(final long id, final String uuid, final String name);
	
	public abstract String getUserLayerExtent (final long id);

    //UserLayerStyle related
    public abstract int updateUserLayerStyleCols(final UserLayerStyle userLayerStyle);

    public abstract UserLayerStyle getUserLayerStyleById(final long id);

    //UserLayerData related
    public abstract int updateUserLayerDataCols(final UserLayerData userlayerdata);

}
