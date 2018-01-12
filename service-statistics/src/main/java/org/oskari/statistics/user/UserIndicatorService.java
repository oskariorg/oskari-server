package org.oskari.statistics.user;

import fi.nls.oskari.service.db.BaseService;

import java.util.List;

public interface UserIndicatorService extends BaseService<UserIndicator> {

    List<UserIndicator> findAllOfUser(long userID);
}
