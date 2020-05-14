package org.oskari.service.wfs3.model;

public enum WFS3ConformanceClass {

    Core("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core"),
    OpenAPI3("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/oas30"),
    GeoJSON("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/geojson");

    public final String url;

    private WFS3ConformanceClass(String url) {
        this.url = url;
    }


}
