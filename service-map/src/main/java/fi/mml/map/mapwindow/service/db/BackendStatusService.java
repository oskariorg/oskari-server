package fi.mml.map.mapwindow.service.db;

import fi.nls.oskari.domain.map.BackendStatus;
import fi.nls.oskari.service.db.BaseService;

import java.util.List;

public interface BackendStatusService extends BaseService<BackendStatus>{
    
    public List<BackendStatus> findAllKnown();

}
