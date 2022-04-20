package org.oskari.capabilities.ogc.wfs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.nls.oskari.util.WFSConversionHelper;

public class FeaturePropertyType {
    public String name;
    public String type;

    @JsonIgnore
    public boolean isGeometry() {
        return WFSConversionHelper.isGeometryType(type);
    }
}
