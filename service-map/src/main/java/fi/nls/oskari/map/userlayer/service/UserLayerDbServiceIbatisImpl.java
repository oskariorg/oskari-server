package fi.nls.oskari.map.userlayer.service;

import com.ibatis.sqlmap.client.SqlMapSession;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.userlayer.service.UserLayerDbService;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.BaseIbatisService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserLayerDbServiceIbatisImpl extends
        BaseIbatisService<UserLayer> implements UserLayerDbService {

    private static final Logger log = LogFactory.getLogger(UserLayerDbServiceIbatisImpl.class);


    @Override
    protected String getNameSpace() {
        return "UserLayer";
    }

    /*
     * The purpose of this method is to allow many SqlMapConfig.xml files in a
     * single portlet
     */
    protected String getSqlMapLocation() {
        return "META-INF/SqlMapConfig_UserLayer.xml";
    }

    /**
     * insert UserLayer table row
     *
     * @param userLayer
     */

    public long insertUserLayerRow(final UserLayer userLayer) {

        log.debug("Insert user_layer row:", userLayer);
        final Long id = queryForObject(getNameSpace() + ".insertUserLayer", userLayer);
        userLayer.setId(id);
        log.debug("Got user_layer id:", id);
        return id;
    }

    /**
     * update UserLayer table row field mapping
     *
     * @param userLayer
     */
    public int updateUserLayerCols(final UserLayer userLayer) {


        try {
            return getSqlMapClient().update(
                    getNameSpace() + ".updateUserLayerCols", userLayer);
        } catch (SQLException e) {
            log.error(e, "Failed to update userLayer col mapping", userLayer);
        }
        return 0;
    }

    /**
     * Get UserLayer row  by id
     *
     * @param id
     * @return userLayer object
     */
    public UserLayer getUserLayerById(long id) {
        return queryForObject(getNameSpace() + ".findUserLayer", id);
    }


    /**
     * Get UserLayer rows of one user by uuid
     *
     * @param uid user uuid
     * @return List of userLayer objects
     */
    public List<UserLayer> getUserLayerByUid(String uid) {
        return queryForList(getNameSpace() + ".findUserLayerByUid", uid);
    }

    public void deleteUserLayerById(final long id) throws ServiceException {
        final UserLayer userLayer = getUserLayerById(id);
        deleteUserLayer(userLayer);
    }

    public void deleteUserLayer(final UserLayer userLayer) throws ServiceException {
        if(userLayer == null) {
            throw new ServiceException("Tried to delete userLayer with <null> param");
        }
        final SqlMapSession session = openSession();
        try {
            session.startTransaction();
            session.delete(getNameSpace() + ".delete-userLayer-data", userLayer.getId());
            session.delete(getNameSpace() + ".delete-userLayer", userLayer.getId());
            // style is for now 1:1 to userLayer so we can delete it here
            session.delete(getNameSpace() + ".delete-userLayer-style", userLayer.getStyle_id());
            session.commitTransaction();
        } catch (Exception e) {
            throw new ServiceException("Error deleting userLayer data with id:" + userLayer.getId(), e);
        } finally {
            endSession(session);
        }
    }



    /**
     * Updates a userLayer publisher screenName
     *
     * @param id
     * @param uuid
     * @param name
     */
    public int updatePublisherName(final long id, final String uuid, final String name) {

        final Map<String, Object> data = new HashMap<String,Object>();
        data.put("publisher_name", name);
        data.put("uuid", uuid);
        data.put("id", id);
        try {
            return getSqlMapClient().update(
                    getNameSpace() + ".updatePublisherName", data);
        } catch (SQLException e) {
            log.error(e, "Failed to update publisher name", data);
        }
        return 0;
    }
}
