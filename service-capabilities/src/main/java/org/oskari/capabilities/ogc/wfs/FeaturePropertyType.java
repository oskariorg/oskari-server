package org.oskari.capabilities.ogc.wfs;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FeaturePropertyType {
    public String name;
    public String type;

    @JsonIgnore
    public boolean isGeometry() {
        return "GeometryPropertyType".equals(type);
    }
}
