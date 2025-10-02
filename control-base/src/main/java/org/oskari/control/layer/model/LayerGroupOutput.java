package org.oskari.control.layer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LayerGroupOutput {

    public int id;
    public int orderNumber;
    public int parentId;
    public boolean selectable;
    public String name;
    public String desc;
    public List<LayerLinkOutput> layers;

}
