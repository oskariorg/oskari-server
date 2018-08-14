package org.oskari.print.util;

import java.util.Map;

import fi.nls.oskari.control.ActionParameters;

public class ModifiedActionParameters extends ActionParameters {

    private final Map<String, String> httpParams;

    public ModifiedActionParameters(Map<String, String> httpParams) {
        this.httpParams = httpParams;
    }

    @Override
    public String getHttpParam(String key) {
        return httpParams.get(key);
    }

}
