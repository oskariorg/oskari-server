package org.oskari.control.layer.model;

import fi.nls.oskari.domain.map.style.VectorStyle;

import java.util.List;
import java.util.Map;

public class LayerExtendedOutput extends LayerOutput {

    public String coverage;
    public List<VectorStyle> styles;
    public Map<String, Object> capabilities;

    public List<FeatureProperties> properties;
}
