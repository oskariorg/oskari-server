package org.oskari.service.wfs3.model;

public enum WFS3ConformanceClass {

    Core("http://www.opengis.net/spec/wfs-1/3.0/req/core"),
    OpenAPI3("http://www.opengis.net/spec/wfs-1/3.0/req/oas30"),
    GeoJSON("http://www.opengis.net/spec/wfs-1/3.0/req/geojson");

    public final String url;

    private WFS3ConformanceClass(String url) {
        this.url = url;
    }


}
