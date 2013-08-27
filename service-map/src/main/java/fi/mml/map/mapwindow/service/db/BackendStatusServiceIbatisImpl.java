package fi.mml.map.mapwindow.service.db;

import java.util.List;

import fi.nls.oskari.domain.map.BackendStatus;
import fi.nls.oskari.service.db.BaseIbatisService;

public class BackendStatusServiceIbatisImpl extends BaseIbatisService<BackendStatus> implements BackendStatusService {

    @Override
    protected String getNameSpace() {
        return "BackendStatus";
    }

    @Override
    public List<BackendStatus> findAllKnown() {
        return queryForList(getNameSpace() + ".findAllKnown");
    }
    
    

}
