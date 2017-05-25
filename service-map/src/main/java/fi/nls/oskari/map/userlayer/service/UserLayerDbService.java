package fi.nls.oskari.map.userlayer.service;

import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.BaseService;

import java.util.List;

public interface UserLayerDbService extends BaseService<UserLayer> {

        long insertUserLayerRow(final UserLayer userlayer );
        int updateUserLayerCols(final UserLayer userlayer);
        UserLayer getUserLayerById(long id);
        List<UserLayer> getUserLayerByUid(String uid);
        void deleteUserLayerById(final long id) throws ServiceException;
        void deleteUserLayer(final UserLayer userlayer) throws ServiceException;
        void deleteUserLayer(final long id) throws ServiceException;
        void deleteUserLayerByUid(final String uid) throws ServiceException;
        int updatePublisherName(final long id, final String uuid, final String name);
}
