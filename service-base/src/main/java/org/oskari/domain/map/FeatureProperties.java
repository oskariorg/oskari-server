package org.oskari.domain.map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

public class FeatureProperties {
    public String name;
    public String type;
    public String rawType;
    public String label;
    public boolean hidden;
    public Map<String,Object> format;
    @JsonIgnore
    public int order;
}
