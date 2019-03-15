package org.oskari.service.wfs.client;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class OskariWFSClient {

    private static final String WFS_3_VERSION = "3.0.0";

    private OskariWFSClient() {}

    public static SimpleFeatureCollection tryGetFeatures(
            String endPoint, String version,
            String user, String pass,
            String typeName, ReferencedEnvelope bbox,
            CoordinateReferenceSystem crs, Integer maxFeatures,
            Filter filter) {
        switch (version) {
        case WFS_3_VERSION:
            return OskariWFS3Client.tryGetFeatures(endPoint, user, pass, typeName, bbox, crs, maxFeatures);
        default:
            return OskariWFS110Client.tryGetFeatures(endPoint, user, pass, typeName, bbox, crs, maxFeatures, filter);
        }
    }

}
