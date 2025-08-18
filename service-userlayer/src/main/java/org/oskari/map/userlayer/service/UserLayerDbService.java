package org.oskari.map.userlayer.service;

import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.ServiceException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.locationtech.jts.geom.Envelope;

import java.util.List;

public abstract class UserLayerDbService extends OskariComponent {

    //UserLayer related
    public abstract int insertUserLayerAndData(final UserLayer userlayer, final List<UserLayerData> userLayerDataList) throws UserLayerException;
    public abstract int updateUserLayer(final UserLayer userlayer) throws UserLayerException;
    public abstract UserLayer getUserLayerById(long id);
    public abstract List<UserLayer> getUserLayerByUuid(String uuid);
    public abstract void deleteUserLayerById(final long id) throws ServiceException;
    public abstract void deleteUserLayer(final UserLayer userlayer) throws ServiceException;
    public abstract void deleteUserLayersByUuid(String uuid) throws ServiceException;
    public abstract int updatePublisherName(final long id, final String uuid, final String name);
	public abstract String getUserLayerExtent (final long id);

    //UserLayerData related
    public abstract int updateUserLayerData(final UserLayerData userlayerdata);

    public abstract SimpleFeatureCollection getFeatures(int layerId, Envelope bbox) throws ServiceException;

}
