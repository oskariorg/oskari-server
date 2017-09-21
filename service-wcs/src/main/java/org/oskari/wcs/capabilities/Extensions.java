package org.oskari.wcs.capabilities;

public enum Extensions {

    CrsSupported("http://www.opengis.net/wcs/service-extension/crs/1.0", "crsSupported"), InterpolationSupported(
            "http://www.opengis.net/WCS_service-extension_interpolation/1.0",
            "interpolationSupported");

    final String ns;
    final String name;

    private Extensions(String ns, String name) {
        this.ns = ns;
        this.name = name;
    }

}
