package fi.nls.oskari.wmts.domain;

public class ResourceUrl {

    private final String format;
    private final String type;
    private final String template;

    public ResourceUrl(String format, String type, String template) {
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
