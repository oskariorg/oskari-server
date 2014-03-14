package fi.nls.oskari.map.userlayer.service;

import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.BaseService;

import java.util.List;

public interface UserLayerDataDbService extends BaseService<UserLayer> {

        public long insertUserLayerRow(final UserLayer userlayer);
        public int updateUserLayerCols(final UserLayer userlayer);
        public Analysis getUserlayerById(long id);
        public List<UserLayer> getUserlayerById(List<Long> idList);
        public List<UserLayer> getUserLayerByUid(String uid);
        public void deleteUserlayerById(final long id) throws ServiceException;
        public void deleteUserLayer(final Analysis analysis) throws ServiceException;
        public int updatePublisherName(final long id, final String uuid, final String name);
}
