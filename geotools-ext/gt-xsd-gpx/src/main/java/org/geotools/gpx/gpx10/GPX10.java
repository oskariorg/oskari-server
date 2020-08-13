package org.geotools.gpx.gpx10;


import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.xsd.XSD;

/**
 * This interface contains the qualified names of all the types,elements, and
 * attributes in the http://www.topografix.com/GPX/1/0 schema.
 *
 * @generated
 */
public final class GPX10 extends XSD {

    /** singleton instance */
    private static final GPX10 instance = new GPX10();

    /**
     * Returns the singleton instance.
     */
    public static final GPX10 getInstance() {
        return instance;
    }

    /**
     * private constructor
     */
    private GPX10() {
    }

    protected void addDependencies(Set dependencies) {
        //TODO: add dependencies here
    }

    /**
     * Returns 'http://www.topografix.com/GPX/1/0'.
     */
    public String getNamespaceURI() {
        return NAMESPACE;
    }

    /**
     * Returns the location of 'gpx10.xsd.'.
     */
    public String getSchemaLocation() {
        return getClass().getResource("gpx10.xsd").toString();
    }

    /** @generated */
    public static final String NAMESPACE = "http://www.topografix.com/GPX/1/0";

    /* Type Definitions */
    /** @generated */
    public static final QName boundsType =
            new QName("http://www.topografix.com/GPX/1/0","boundsType");
    /** @generated */
    public static final QName copyrightType =
            new QName("http://www.topografix.com/GPX/1/0","copyrightType");
    /** @generated */
    public static final QName degreesType =
            new QName("http://www.topografix.com/GPX/1/0","degreesType");
    /** @generated */
    public static final QName dgpsStationType =
            new QName("http://www.topografix.com/GPX/1/0","dgpsStationType");
    /** @generated */
    public static final QName emailType =
            new QName("http://www.topografix.com/GPX/1/0","emailType");
    /** @generated */
    public static final QName extensionsType =
            new QName("http://www.topografix.com/GPX/1/0","extensionsType");
    /** @generated */
    public static final QName fixType =
            new QName("http://www.topografix.com/GPX/1/0","fixType");
    /** @generated */
    public static final QName gpxType =
            new QName("http://www.topografix.com/GPX/1/0","gpxType");
    /** @generated */
    public static final QName latitudeType =
            new QName("http://www.topografix.com/GPX/1/0","latitudeType");
    /** @generated */
    public static final QName linkType =
            new QName("http://www.topografix.com/GPX/1/0","linkType");
    /** @generated */
    public static final QName longitudeType =
            new QName("http://www.topografix.com/GPX/1/0","longitudeType");
    /** @generated */
    public static final QName metadataType =
            new QName("http://www.topografix.com/GPX/1/0","metadataType");
    /** @generated */
    public static final QName personType =
            new QName("http://www.topografix.com/GPX/1/0","personType");
    /** @generated */
    public static final QName ptsegType =
            new QName("http://www.topografix.com/GPX/1/0","ptsegType");
    /** @generated */
    public static final QName ptType =
            new QName("http://www.topografix.com/GPX/1/0","ptType");
    /** @generated */
    public static final QName rteType =
            new QName("http://www.topografix.com/GPX/1/0","rte");
    public static final QName rteptType =
            new QName("http://www.topografix.com/GPX/1/0","rtept");
    /** @generated */
    public static final QName trksegType =
            new QName("http://www.topografix.com/GPX/1/0","trkseg");
    public static final QName trkptType =
            new QName("http://www.topografix.com/GPX/1/0","trkpt");
    /** @generated */
    public static final QName trkType =
            new QName("http://www.topografix.com/GPX/1/0","trk");
    /** @generated */
    public static final QName wptType =
            new QName("http://www.topografix.com/GPX/1/0","wpt");

    /* Elements */
    /** @generated */
    public static final QName gpx =
            new QName("http://www.topografix.com/GPX/1/0","gpx");

    /* Attributes */

}
