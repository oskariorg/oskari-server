package org.oskari.service.wfs3;

import java.util.Objects;

/**
 * @see https://raw.githubusercontent.com/opengeospatial/WFS_FES/master/core/openapi/schemas/link.yaml
 */
public class WFS3Link {

    private final String href;
    private final String rel;
    private final String type;
    private final String hreflang;
    private final String title;

    public WFS3Link(String href, String rel, String type, String hreflang, String title) {
        Objects.requireNonNull(href);
        this.href = href;
        this.rel = rel;
        this.type = type;
        this.hreflang = hreflang;
        this.title = title;
    }

    public String getHref() {
        return href;
    }

    public String getRel() {
        return rel;
    }

    public String getType() {
        return type;
    }

    public String getHreflang() {
        return hreflang;
    }

    public String getTitle() {
        return title;
    }

}
