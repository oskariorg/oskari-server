package fi.mml.map.mapwindow.service.db;

import fi.nls.oskari.service.db.BaseService;

import java.util.Set;

/**
 * Interface for Maplayer projections service
 *
 */
public interface MaplayerProjectionService extends BaseService {

    public void insertList(final int maplayerId, final Set<String> CRSs);

}
