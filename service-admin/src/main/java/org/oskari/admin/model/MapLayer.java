package org.oskari.admin.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Used as input data model for inserting layers via Flyway or the admin UI.
 */
public class MapLayer {

    // Integer so it can be null and not part of output
    private Integer id;
    private String type;
    private String url;
    private String username;
    private String password;
    private String version;
    private String name;
    private Map<String, Map<String, String>> locale;

    private String srs;

    private int opacity = 100;
    private String style;
    private double minscale = -1;
    private double maxscale = -1;

    private String legend_image;
    private String metadataid;
    private boolean internal;

    private Map<String, String> params;
    // opts and attributes can be deep nested structure -> Object
    private Map<String, Object> options;
    private Map<String, Object> attributes;

    private String gfi_type;
    private String gfi_xslt;
    private String gfi_content;

    private boolean base_map;
    private boolean realtime; // -> move to options
    private int refresh_rate = -1; // -> move to options
    private int capabilities_update_rate_sec = -1; // -> move to attributes?

    // for admin functionality
    private int dataprovider_id = -1;
    // for initial db content
    private String dataprovider;

    // for admin functionality
    private Set<Integer> group_ids;
    // for initial db content
    private Set<String> groups;

    /*
    "role_permissions": {
        "Guest" : ["VIEW_LAYER", "VIEW_PUBLISHED"],
        "User" : ["VIEW_LAYER", "VIEW_PUBLISHED", "PUBLISH"]
    }*/
    private Map<String, Set<String>> role_permissions;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Map<String, String>> getLocale() {
        return locale;
    }

    public void setLocale(Map<String, Map<String, String>> locale) {
        this.locale = locale;
    }

    public String getSrs() {
        return srs;
    }

    public void setSrs(String srs) {
        this.srs = srs;
    }

    @Deprecated
    public void setSrs_name(String srs_name) {
        this.srs = srs_name;
    }

    public int getOpacity() {
        return opacity;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public double getMinscale() {
        return minscale;
    }

    public void setMinscale(double minscale) {
        this.minscale = minscale;
    }

    public double getMaxscale() {
        return maxscale;
    }

    public void setMaxscale(double maxscale) {
        this.maxscale = maxscale;
    }

    public String getLegend_image() {
        return legend_image;
    }

    public void setLegend_image(String legend_image) {
        this.legend_image = legend_image;
    }

    public String getMetadataid() {
        return metadataid;
    }

    public void setMetadataid(String metadataid) {
        this.metadataid = metadataid;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getGfi_type() {
        return gfi_type;
    }

    public void setGfi_type(String gfi_type) {
        this.gfi_type = gfi_type;
    }

    public String getGfi_xslt() {
        return gfi_xslt;
    }

    public void setGfi_xslt(String gfi_xslt) {
        this.gfi_xslt = gfi_xslt;
    }

    public String getGfi_content() {
        return gfi_content;
    }

    public void setGfi_content(String gfi_content) {
        this.gfi_content = gfi_content;
    }

    public boolean isBase_map() {
        return base_map;
    }

    public void setBase_map(boolean base_map) {
        this.base_map = base_map;
    }

    public boolean isRealtime() {
        return realtime;
    }

    public void setRealtime(boolean realtime) {
        this.realtime = realtime;
    }

    public int getRefresh_rate() {
        return refresh_rate;
    }

    public void setRefresh_rate(int refresh_rate) {
        this.refresh_rate = refresh_rate;
    }

    public int getCapabilities_update_rate_sec() {
        return capabilities_update_rate_sec;
    }

    public void setCapabilities_update_rate_sec(int capabilities_update_rate_sec) {
        this.capabilities_update_rate_sec = capabilities_update_rate_sec;
    }

    public int getDataprovider_id() {
        return dataprovider_id;
    }

    public void setDataprovider_id(int dataprovider_id) {
        this.dataprovider_id = dataprovider_id;
    }

    public String getDataprovider() {
        return dataprovider;
    }

    public void setDataprovider(String dataprovider) {
        this.dataprovider = dataprovider;
    }


    @Deprecated
    public void setOrganization(String organization) {
        this.dataprovider = organization;
    }

    public Set<Integer> getGroup_ids() {
        return group_ids;
    }

    public void setGroup_ids(Set<Integer> group_ids) {
        this.group_ids = group_ids;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    @Deprecated
    public void setInspiretheme(String inspiretheme) {
        Set<String> groups = new HashSet<>();
        groups.add(inspiretheme);
        setGroups(groups);
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public Map<String, Set<String>> getRole_permissions() {
        return role_permissions;
    }

    public void setRole_permissions(Map<String, Set<String>> role_permissions) {
        this.role_permissions = role_permissions;
    }
}
