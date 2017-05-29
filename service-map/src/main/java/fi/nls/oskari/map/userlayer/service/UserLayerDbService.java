package fi.nls.oskari.map.userlayer.service;

import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.domain.map.userlayer.UserLayerStyle;
import fi.nls.oskari.service.ServiceException;

import java.util.List;

public interface UserLayerDbService {
        //UserLayer related
        public int insertUserLayer(final UserLayer userlayer, final UserLayerStyle userLayerStyle, final List <UserLayerData> userLayerDataList) throws ServiceException;
        public int updateUserLayerCols(final UserLayer userlayer);
        public UserLayer getUserLayerById(long id);
        public List<UserLayer> getUserLayerByUuid(String uuid);
        public void deleteUserLayerById(final long id) throws ServiceException;
        public void deleteUserLayer(final UserLayer userlayer) throws ServiceException;
        public void deleteUserLayersByUuid(String uuid) throws ServiceException;
        public int updatePublisherName(final long id, final String uuid, final String name);
        //UserLayerStyle related
        public int updateUserLayerStyleCols(final UserLayerStyle userLayerStyle);
        public UserLayerStyle getUserLayerStyleById(final long id);
        //UserLayerData related
        public int updateUserLayerDataCols(final UserLayerData userlayerdata);
}
