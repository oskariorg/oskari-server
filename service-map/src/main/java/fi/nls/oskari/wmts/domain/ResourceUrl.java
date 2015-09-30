package fi.nls.oskari.wmts.domain;

/**
 * Created by SMAKINEN on 28.9.2015.
 */
public class ResourceUrl {

    private String format;
    private String type;
    private String template;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
