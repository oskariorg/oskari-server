package fi.nls.oskari.map.userlayer.service;

import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.BaseService;

import java.util.List;

public interface UserLayerDbService extends BaseService<UserLayer> {

        public long insertUserLayerRow(final UserLayer userlayer );
        public int updateUserLayerCols(final UserLayer userlayer);
        public UserLayer getUserLayerById(long id);
        public List<UserLayer> getUserLayerByUid(String uid);
        public void deleteUserLayerById(final long id) throws ServiceException;
        public void deleteUserLayer(final UserLayer userlayer) throws ServiceException;
        public void deleteUserLayer(final long id) throws ServiceException;
        public int updatePublisherName(final long id, final String uuid, final String name);
}
