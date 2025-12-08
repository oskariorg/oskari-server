package org.oskari.service.maplayer;

import org.oskari.user.User;

import java.util.List;

public class DescribeLayerQuery {

    private final Long viewId;
    private final String layerId;
    private final User user;
    private final String lang;
    private final String crs;
    private final List<String> styles;

    public Long getViewId() {
        return viewId;
    }

    public String getLayerId() {
        return layerId;
    }

    public User getUser() {
        return user;
    }

    public String getLang() {
        return lang;
    }

    public String getCrs() {
        return crs;
    }

    public List<String> getStyles() {
        return styles;
    }

    public DescribeLayerQuery(Long viewId, String layerId, User user, String lang, String crs) {
        this(viewId, layerId, user, lang, crs, null);
    }

    public DescribeLayerQuery(Long viewId, String layerId, User user, String lang, String crs, List<String> styles) {
        this.viewId = viewId;
        this.layerId = layerId;
        this.user = user;
        this.lang = lang;
        this.crs = crs;
        this.styles = styles;
    }

}
