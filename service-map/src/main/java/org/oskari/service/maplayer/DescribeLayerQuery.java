package org.oskari.service.maplayer;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.oskari.user.User;

import java.util.List;

public class DescribeLayerQuery {

    private final String layerId;
    private final User user;
    private final String lang;
    private final CoordinateReferenceSystem crs;
    private final List<String> styles;

    public String getLayerId() {
        return layerId;
    }

    public User getUser() {
        return user;
    }

    public String getLang() {
        return lang;
    }

    public CoordinateReferenceSystem getCrs() {
        return crs;
    }

    public List<String> getStyles() {
        return styles;
    }

    public DescribeLayerQuery(String layerId, User user, String lang, CoordinateReferenceSystem crs, List<String> styles) {
        this.layerId = layerId;
        this.user = user;
        this.lang = lang;
        this.crs = crs;
        this.styles = styles;
    }

}
