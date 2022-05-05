package org.oskari.capabilities.ogc.wmts;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceUrl {

    private final String format;
    private final String type;
    private final String template;

    public ResourceUrl(@JsonProperty("format") String format,
                       @JsonProperty("type") String type,
                       @JsonProperty("template") String template) {
        this.format = format;
        this.type = type;
        this.template = template;
    }

    public String getFormat() {
        return format;
    }

    public String getType() {
        return type;
    }

    public String getTemplate() {
        return template;
    }

}
