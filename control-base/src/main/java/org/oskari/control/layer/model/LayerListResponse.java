package org.oskari.control.layer.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LayerListResponse {

    public List<LayerGroupOutput> groups;
    public List<LayerOutput> layers;
    public Map<Integer, DataProviderOutput> providers;

}
