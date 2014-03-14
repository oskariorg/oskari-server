package fi.nls.oskari.map.userlayer.service;

import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.BaseService;

import java.util.List;

public interface UserLayerDataDbService extends BaseService<UserLayerData> {

        public long insertUserLayerDataRow(final UserLayerData userlayerata);
        public int updateUserLayerDataCols(final UserLayerData userlayerdata);

}
