package org.oskari.service.backendstatus;

import java.util.List;

import fi.nls.oskari.domain.map.BackendStatus;

public interface BackendStatusService {

    public List<BackendStatus> findAll();
    public List<BackendStatus> findAllWithAlert();
    public void insertAll(List<BackendStatus> statuses);

}
