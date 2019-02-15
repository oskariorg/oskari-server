package org.oskari.service.wfs3.client;

/**
 * @see https://raw.githubusercontent.com/opengeospatial/WFS_FES/master/core/openapi/schemas/link.yaml
 */
public class WFS3LinkBuilder {

    private String href;
    private String rel;
    private String type;
    private String hreflang;
    private String title;

    public WFS3LinkBuilder set(String key, String value) {
        switch (key) {
        case "href":
            this.href = value;
            break;
        case "rel":
            this.rel = value;
            break;
        case "type":
            this.type = value;
            break;
        case "hreflang":
            this.hreflang = value;
            break;
        case "title":
            this.title = value;
            break;
        }
        return this;
    }

    public WFS3Link build() {
        return new WFS3Link(href, rel, type, hreflang, title);
    }

    public void clear() {
        this.href = null;
        this.rel = null;
        this.type = null;
        this.hreflang = null;
        this.title = null;
    }

}
