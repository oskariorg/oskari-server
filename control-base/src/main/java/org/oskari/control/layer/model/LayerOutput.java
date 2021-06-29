package org.oskari.control.layer.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;

// only include non-nulls (==don't include nulls)
// only include lists and maps that are NOT empty
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LayerOutput {

    public int id;
    // technical name
    public String layer;
    public String url;
    public String type;
    public String version;
    // ui label for user
    public String name;

    public Double minScale;
    public Double maxScale;

    public Integer opacity;
    public Integer refreshRate;
    public Boolean realtime;
    public String gfiContent;

    public Integer dataprovider;
    // additional URL-params/querystring for requests to service
    public Map<String, Object> params = new HashMap<>();
    // flags for controlling frontend behavior (wrapX on WMTS, singleTile on WMS etc)
    // styles for vector features
    // TODO: consider moving options and params to extended. They should not be needed before the layer is on the map
    //   AND having styles in options will make the listing bigger than it needs to be
    public Map<String, Object> options = new HashMap<>();

    public Date created;

    public String metadataId;
    public Set<String> srs;
    // do we still need "baseLayerId" or could we just put the sublayers and determine parent for them on the frontend?
    public Integer baseLayerId;
    // TODO: should sublayers be in extended output?
    public List<LayerOutput> sublayers;
}
