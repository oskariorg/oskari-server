package fi.nls.oskari.geoserver;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Created by SMAKINEN on 1.9.2015.
 */
@JsonRootName("namespace")
public class Namespace {
    public String prefix;
    public String uri;
}
