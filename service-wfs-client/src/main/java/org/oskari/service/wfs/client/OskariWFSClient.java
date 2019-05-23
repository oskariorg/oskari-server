package org.oskari.service.wfs.client;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import fi.nls.oskari.service.ServiceRuntimeException;

public class OskariWFSClient {

    public SimpleFeatureCollection getFeatures(
            String endPoint, String version,
            String user, String pass,
            String typeName, ReferencedEnvelope bbox,
            CoordinateReferenceSystem crs, int maxFeatures,
            Filter filter) throws ServiceRuntimeException {
        return new OskariWFSLoadCommand(endPoint, version, user, pass,
                typeName, bbox, crs, maxFeatures, filter).execute();
    }

}
