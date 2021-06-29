package org.oskari.control.layer.model;

import java.util.HashMap;
import java.util.Map;

public class LayerExtendedOutput extends LayerOutput {

    public String coverage;
    // for convenience in mapfull layers -> datasources display
    // prefixed with _ to notify tmp nature
    public String _dataproviderName;
    // previously subtitle
    public String desc;

    public Boolean isQueryable;
    // Mostly server-side flags but
    // can include ui labels for vector feature properties etc
    public Map<String, Object> attributes = new HashMap<>();
}
