package org.oskari.control.layer.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

// only include non-nulls (==don't include nulls)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LayerOutput {

    public int id;
    // technical name
    public String name;
    public String url;
    // ui label for user
    public String title;

    public Integer dataprovider;
    // additional URL-params/querystring for requests to service
    public Map<String, Object> params;
    // flags for controlling frontend behavior (wrapX on WMTS, singleTile on WMS etc)
    // styles for vector features
    // TODO: consider moving options and params to extended. They should not be needed before the layer is on the map
    //   AND having styles in options will make the listing bigger than it needs to be
    public Map<String, Object> options;
}
