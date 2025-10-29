package org.oskari.domain.map;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

// only include non-nulls (==don't include nulls)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LayerOutput {

    public String id;
    public String type;
    public String name;
    public String metadataUuid;
    public Integer dataproviderId;
    public Date created;
    public Date updated;
}
