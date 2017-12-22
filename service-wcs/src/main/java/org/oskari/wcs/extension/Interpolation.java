package org.oskari.wcs.extension;

public enum Interpolation {

    NEAREST_NEIGHBOUR(
            "http://www.opengis.net/spec/WCS_service-extension_interpolation/1.0/conf/nearest-neighbor",
            "http://www.opengis.net/def/interpolation/OGC/1/nearest-neighbor"
    ),
    LINEAR(
            "http://www.opengis.net/spec/WCS_service-extension_interpolation/1.0/conf/nearest-neighbor",
            "http://www.opengis.net/def/interpolation/OGC/1/linear"
    ),
    CUBIC(
            "http://www.opengis.net/spec/WCS_service-extension_interpolation/1.0/conf/nearest-neighbor",
            "http://www.opengis.net/def/interpolation/OGC/1/cubic"
    );

    public static final String NS_URI = "http://www.opengis.net/wcs/crs/1.0";
    public static final String PROFILE_IDENTIFIER = "http://www.opengis.net/WCS_service-extension_interpolation/1.0";
    public static final String EXTENSION_ELEMENT_LOCALNAME = "interpolationSupported";

    public final String profile;
    public final String method;

    private Interpolation(String profile, String method) {
        this.profile = profile;
        this.method = method;
    }

}
