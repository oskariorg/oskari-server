package org.oskari.service.wfs3.model;

import java.util.Objects;

/**
 * @see https://raw.githubusercontent.com/opengeospatial/WFS_FES/master/core/openapi/schemas/link.yaml
 */
public class WFS3Link {

    private String href;
    private String rel;
    private String type;
    private String hreflang;
    private String title;

    public WFS3Link() {}

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

    public void setHref(String href) {
        this.href = href;
    }

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHreflang() {
        return hreflang;
    }

    public void setHreflang(String hreflang) {
        this.hreflang = hreflang;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WFS3Link other = (WFS3Link) obj;
        if (href == null) {
            if (other.href != null)
                return false;
        } else if (!href.equals(other.href))
            return false;
        if (hreflang == null) {
            if (other.hreflang != null)
                return false;
        } else if (!hreflang.equals(other.hreflang))
            return false;
        if (rel == null) {
            if (other.rel != null)
                return false;
        } else if (!rel.equals(other.rel))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}
