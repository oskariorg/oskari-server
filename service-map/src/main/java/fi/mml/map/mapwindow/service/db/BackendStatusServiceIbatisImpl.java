package fi.mml.map.mapwindow.service.db;

import fi.nls.oskari.domain.map.BackendStatus;
import fi.nls.oskari.service.db.BaseIbatisService;

import java.util.List;

public class BackendStatusServiceIbatisImpl extends BaseIbatisService<BackendStatus> implements BackendStatusService {

    @Override
    protected String getNameSpace() {
        return "BackendStatus";
    }

    public List<BackendStatus> findAllKnown() {
        return queryForList(getNameSpace() + ".findAllKnown");
    }
}
