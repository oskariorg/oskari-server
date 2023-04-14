package org.oskari.capabilities.ogc.wfs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import fi.nls.oskari.util.WFSConversionHelper;

import java.util.HashMap;
import java.util.Map;

public class FeaturePropertyType {
    public String name;
    public String type;
    public Map<String, String> restrictions = new HashMap<>();

    @JsonIgnore
    public boolean isGeometry() {
        return WFSConversionHelper.isGeometryType(type);
    }

    public static FeaturePropertyType fromMap(Map deserialized) {
        FeaturePropertyType type = new FeaturePropertyType();
        type.name = (String) deserialized.get("name");
        type.type = (String) deserialized.get("type");
        type.restrictions = (Map<String, String>) deserialized.get("restrictions");
        return type;
    }
}
