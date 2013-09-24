package fi.mml.map.mapwindow.service.db;

import java.util.List;

import fi.nls.oskari.domain.map.BackendStatus;
import fi.nls.oskari.service.db.BaseService;

public interface BackendStatusService extends BaseService<BackendStatus>{
    
    public List<BackendStatus> findAllKnown();

}
