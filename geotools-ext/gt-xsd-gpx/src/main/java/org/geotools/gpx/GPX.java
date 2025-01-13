package org.geotools.gpx;


import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.xsd.XSD;

/**
 * This interface contains the qualified names of all the types,elements, and
 * attributes in the http://www.topografix.com/GPX/1/1 schema.
 *
 * @generated
 */
public final class GPX extends XSD {

    /** singleton instance */
    private static final GPX instance = new GPX();

    /**
     * Returns the singleton instance.
     */
    public static final GPX getInstance() {
        return instance;
    }

    /**
     * private constructor
     */
    private GPX() {
    }

    protected void addDependencies(Set dependencies) {
        //TODO: add dependencies here
    }

    /**
     * Returns 'http://www.topografix.com/GPX/1/1'.
     */
    public String getNamespaceURI() {
        return NAMESPACE;
    }

    /**
     * Returns the location of 'gpx.xsd.'.
     */
    public String getSchemaLocation() {
        return getClass().getResource("gpx.xsd").toString();
    }

    /** @generated */
    public static final String NAMESPACE = "http://www.topografix.com/GPX/1/1";

    /* Type Definitions */
    /** @generated */
    public static final QName boundsType =
            new QName("http://www.topografix.com/GPX/1/1","boundsType");
    /** @generated */
    public static final QName copyrightType =
            new QName("http://www.topografix.com/GPX/1/1","copyrightType");
    /** @generated */
    public static final QName degreesType =
            new QName("http://www.topografix.com/GPX/1/1","degreesType");
    /** @generated */
    public static final QName dgpsStationType =
            new QName("http://www.topografix.com/GPX/1/1","dgpsStationType");
    /** @generated */
    public static final QName emailType =
            new QName("http://www.topografix.com/GPX/1/1","emailType");
    /** @generated */
    public static final QName extensionsType =
            new QName("http://www.topografix.com/GPX/1/1","extensionsType");
    /** @generated */
    public static final QName fixType =
            new QName("http://www.topografix.com/GPX/1/1","fixType");
    /** @generated */
    public static final QName gpxType =
            new QName("http://www.topografix.com/GPX/1/1","gpxType");
    /** @generated */
    public static final QName latitudeType =
            new QName("http://www.topografix.com/GPX/1/1","latitudeType");
    /** @generated */
    public static final QName linkType =
            new QName("http://www.topografix.com/GPX/1/1","linkType");
    /** @generated */
    public static final QName longitudeType =
            new QName("http://www.topografix.com/GPX/1/1","longitudeType");
    /** @generated */
    public static final QName metadataType =
            new QName("http://www.topografix.com/GPX/1/1","metadataType");
    /** @generated */
    public static final QName personType =
            new QName("http://www.topografix.com/GPX/1/1","personType");
    /** @generated */
    public static final QName ptsegType =
            new QName("http://www.topografix.com/GPX/1/1","ptsegType");
    /** @generated */
    public static final QName ptType =
            new QName("http://www.topografix.com/GPX/1/1","ptType");
    /** @generated */
    public static final QName rteType =
            new QName("http://www.topografix.com/GPX/1/1","rte");
    public static final QName rteptType =
            new QName("http://www.topografix.com/GPX/1/1","rtept");
    /** @generated */
    public static final QName trksegType =
            new QName("http://www.topografix.com/GPX/1/1","trkseg");
    public static final QName trkptType =
            new QName("http://www.topografix.com/GPX/1/1","trkpt");
    /** @generated */
    public static final QName trkType =
            new QName("http://www.topografix.com/GPX/1/1","trk");
    /** @generated */
    public static final QName wptType =
            new QName("http://www.topografix.com/GPX/1/1","wpt");

    /* Elements */
    /** @generated */
    public static final QName gpx =
            new QName("http://www.topografix.com/GPX/1/1","gpx");

    /* Attributes */

}
