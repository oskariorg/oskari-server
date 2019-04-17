package org.oskari.service.wfs.client;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.wfs3.OskariWFS3Client;

import fi.nls.oskari.service.ServiceRuntimeException;

public class OskariWFSClient {

    private static final String WFS_3_VERSION = "3.0.0";

    public SimpleFeatureCollection getFeatures(
            String endPoint, String version,
            String user, String pass,
            String typeName, ReferencedEnvelope bbox,
            CoordinateReferenceSystem crs, int maxFeatures,
            Filter filter) throws ServiceRuntimeException {
        switch (version) {
        case WFS_3_VERSION:
            return OskariWFS3Client.getFeatures(endPoint, user, pass, typeName, bbox, crs, maxFeatures);
        default:
            return OskariWFS110Client.getFeatures(endPoint, user, pass, typeName, bbox, crs, maxFeatures, filter);
        }
    }

}
