package org.oskari.service.backendstatus;

import fi.nls.oskari.domain.map.BackendStatus;

import java.util.List;

public interface BackendStatusService {

    public List<BackendStatus> findAll();
    public List<BackendStatus> findAllWithAlert();
    public void insertAll(List<BackendStatus> statuses);

}
