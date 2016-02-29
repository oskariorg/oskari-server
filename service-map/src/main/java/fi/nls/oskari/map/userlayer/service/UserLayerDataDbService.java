package fi.nls.oskari.map.userlayer.service;

import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.service.db.BaseService;

public interface UserLayerDataDbService extends BaseService<UserLayerData> {

        public long insertUserLayerDataRow(final UserLayerData userlayerata);
        public int updateUserLayerDataCols(final UserLayerData userlayerdata);

}
