package fi.nls.oskari.domain.map.wfs;

/**
 * Handles WFS Parser config data
 *
 * Used in Admin layer management  wfs 2.0.0
 */
public class WFSParserConfig {
    private String id;
    private String name;
    private String type;
    private String request_template;
    private String response_template;
    private String parse_config;
    private String sld_style;
    private String title;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRequest_template() {
        return request_template;
    }

    public void setRequest_template(String request_template) {
        this.request_template = request_template;
    }

    public String getResponse_template() {
        return response_template;
    }

    public void setResponse_template(String response_template) {
        this.response_template = response_template;
    }

    public String getParse_config() {
        return parse_config;
    }

    public void setParse_config(String parse_config) {
        this.parse_config = parse_config;
    }

    public String getSld_style() {
        return sld_style;
    }

    public void setSld_style(String sld_style) {
        this.sld_style = sld_style;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String toJSONString() {
       StringBuilder sb = new StringBuilder();
       sb.append("{\"id\": \"" + this.id + "\"");
       sb.append(",\"request_template\": \"" + this.request_template + "\"");
       sb.append(",\"response_template\": \"" + this.response_template +  "\"");
       sb.append(",\"type\": \""+ this.type +"\"");
       sb.append(",\"title\": \""+ this.title +"\"");
       if(this.sld_style != null) sb.append(",\"sdl_style\": \"" + this.sld_style +  "\"");
       if(this.parse_config != null)sb.append(",\"parse_config\" : " + this.parse_config);
       sb.append("}");
       return sb.toString();
    }
}
