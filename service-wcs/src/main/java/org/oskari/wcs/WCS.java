package org.oskari.wcs;

public interface WCS {

    public static final String VERSION_201 = "2.0.1";

    public static final String NS_XSD = "http://www.w3.org/2001/XMLSchema";
    public static final String NS_GML = "http://www.opengis.net/gml/3.2";
    public static final String NS_GML_COV = "http://www.opengis.net/gmlcov/1.0";

    public static final String NS_WCS = "http://www.opengis.net/wcs/2.0";

    public static final String NS_RSUB = "http://www.opengis.net/wcs/range-subsetting/1.0";
    public static final String NS_SCAL = "http://www.opengis.net/wcs/scaling/1.0";
    public static final String NS_INT = "http://www.opengis.net/wcs/interpolation/1.0";
    public static final String NS_CRS = "http://www.opengis.net/wcs/crs/1.0";

    public static final String PROFILE_KVP = "http://www.opengis.net/spec/WCS_protocol-binding_get-kvp/1.0.1";
    public static final String PROFILE_XML_POST = "http://www.opengis.net/spec/WCS_protocol-binding_post-xml/1.0";

    public static final String PROFILE_EXT_RECORD_SUBSETTING = "http://www.opengis.net/spec/WCS_service-extension_range-subsetting/1.0/conf/record-subsetting";
    public static final String PROFILE_EXT_SCALING = "http://www.opengis.net/spec/WCS_service-extension_scaling/1.0/conf/scaling";
    public static final String PROFILE_EXT_INTERP = "http://www.opengis.net/spec/WCS_service-extension_interpolation/1.0/conf/interpolation";
    public static final String PROFILE_EXT_INTERP_PER_AXIS = "http://www.opengis.net/spec/WCS_service-extension_interpolation/1.0/conf/interpolation-per-axis";
    public static final String PROFILE_EXT_CRS = "http://www.opengis.net/spec/WCS_service-extension_crs/1.0/conf/crs";

}
