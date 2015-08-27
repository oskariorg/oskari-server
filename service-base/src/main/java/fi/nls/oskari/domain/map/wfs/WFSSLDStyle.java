package fi.nls.oskari.domain.map.wfs;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Handles WFS SLD styles
 *
 * Used in WFSLayerStore that handles the WFS configuration.
 */
public class WFSSLDStyle {
    private String id;
    private String name;
    private String SLDStyle;

    /**
     * Gets id
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets id
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets SLD style
     *
     * @return SLD style (xml)
     */
    @JsonProperty("SLDStyle")
    public String getSLDStyle() {
        return SLDStyle;
    }

    /**
     * Sets SLD style
     *
     * @param style
     */
    public void setSLDStyle(String style) {
        this.SLDStyle = style;
    }

    /**
     * Print format
     *
     * @return object description
     */
    @JsonIgnore
    public String toString() {
        return "id: " + this.id + ", name: " + this.name + ", SLDStyle: "
                + this.SLDStyle;
    }
}
