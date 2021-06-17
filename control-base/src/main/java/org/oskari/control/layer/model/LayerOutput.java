package org.oskari.control.layer.model;

import com.fasterxml.jackson.annotation.JsonInclude;

// only include non-nulls (==don't include nulls)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LayerOutput {

    public int id;
    public String name;
}
