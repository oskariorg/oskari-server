package org.oskari.statistics.user;

import fi.nls.oskari.service.db.BaseIbatisService;

import java.util.List;

public class UserIndicatorServiceImpl extends BaseIbatisService<UserIndicator> implements UserIndicatorService {
    @Override
    protected String getNameSpace() {
        return "UserIndicator";
    }

    public List<UserIndicator> findAllOfUser(long userID){
        return queryForList(getNameSpace()+".findAllOfUser", userID);
    }

    public void deleteByUserId (long userID){
        delete(getNameSpace()+".deleteByUserId", userID);
    }
}
