package org.oskari.print.util;

import java.util.Map;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;

public class ModifiedActionParameters extends ActionParameters {

    private final User user;
    private final Map<String, String> httpParams;

    public ModifiedActionParameters(User user, Map<String, String> httpParams) {
        this.user = user;
        this.httpParams = httpParams;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getHttpParam(String key) {
        return httpParams.get(key);
    }

}
