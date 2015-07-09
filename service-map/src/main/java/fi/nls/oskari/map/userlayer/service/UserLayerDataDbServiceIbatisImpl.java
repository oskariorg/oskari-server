package fi.nls.oskari.map.userlayer.service;

import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.db.BaseIbatisService;

import java.sql.SQLException;

public class UserLayerDataDbServiceIbatisImpl extends
        BaseIbatisService<UserLayerData> implements UserLayerDataDbService {

    private static final Logger log = LogFactory.getLogger(UserLayerDataDbServiceIbatisImpl.class);


    @Override
    protected String getNameSpace() {
        return "UserLayerData";
    }

    /*
     * The purpose of this method is to allow many SqlMapConfig.xml files in a
     * single portlet
     */
    protected String getSqlMapLocation() {
        return "META-INF/SqlMapConfig_UserLayer.xml";
    }

    /**
     * insert UserLayerData table row
     *
     * @param userLayer
     */
    public long insertUserLayerDataRow(final UserLayerData userLayer) {

       // log.debug("Insert user layer data row:", userLayer);
        final Long id = queryForObject(getNameSpace() + ".insertUserLayerData", userLayer);
        userLayer.setId(id);

        return id;
    }

    /**
     * update UserLayerData table row field mapping
     *
     * @param userLayer
     */
    public int updateUserLayerDataCols(final UserLayerData userLayer) {


        try {
            return getSqlMapClient().update(
                    getNameSpace() + ".updateUserLayerDataCols", userLayer);
        } catch (SQLException e) {
            log.error(e, "Failed to update userLayer col mapping", userLayer);
        }
        return 0;
    }


}
