package org.oskari.control.layer.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataProviderOutput {

    public int id;
    public String name;
    public String desc;

}
