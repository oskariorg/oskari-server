package org.oskari.service.wfs.client;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import fi.nls.oskari.util.JSONHelper;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.xsd.Encoder;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceRuntimeException;

/**
 * Client code for WFS 1.1.0 services
 */
public class OskariWFS110Client {

    private static final OskariGML OSKARI_GML = new OskariGML();

    private OskariWFS110Client() {}

    /**
     * @return SimpleFeatureCollection containing the parsed Features
     * @throws ServiceRuntimeException if everything fails
     */
    public static SimpleFeatureCollection getFeatures(OskariLayer layer,
            ReferencedEnvelope bbox, CoordinateReferenceSystem crs, Filter filter) {
        String endPoint = layer.getUrl();
        String typeName = layer.getName();
        String user = layer.getUsername();
        String pass = layer.getPassword();
        boolean tryGeoJSON = OskariWFSClient.tryGeoJSON(layer);
        int maxFeatures = OskariWFSClient.getMaxFeatures(layer);
        Map<String, String> query = getQueryParams(typeName, bbox, crs, maxFeatures, filter);
        // attach any extra params added for layer (for example properties=[prop name we are interested in])
        query.putAll(JSONHelper.getObjectAsMap(layer.getParams()));
        return OskariWFSClient.getFeatures(endPoint, user, pass, query, crs, tryGeoJSON, OSKARI_GML);
    }

    protected static Map<String, String> getQueryParams(String typeName, ReferencedEnvelope bbox,
            CoordinateReferenceSystem crs, int maxFeatures, Filter filter) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("SERVICE", "WFS");
        parameters.put("VERSION", "1.1.0");
        parameters.put("REQUEST", "GetFeature");
        parameters.put("TYPENAME", typeName);
        parameters.put("SRSNAME", crs.getIdentifiers().iterator().next().toString());
        if (filter == null) {
            parameters.put("BBOX", OskariWFSClient.getBBOX(bbox));
        } else {
            parameters.put("FILTER", getFilter(filter));
        }
        parameters.put("MAXFEATURES", Integer.toString(maxFeatures));
        return parameters;
    }

    protected static String getFilter(Filter filter) {
        if (filter == null) {
            return null;
        }
        try {
            // https://docs.geoserver.org/stable/en/user/filter/syntax.html
            // Filter Encoding 1.1 is used in WFS 1.1
            Encoder encoder = new Encoder(new org.geotools.filter.v1_1.OGCConfiguration());
            encoder.setOmitXMLDeclaration(true);
            return encoder.encodeAsString(filter, org.geotools.filter.v1_1.OGC.Filter);
        } catch (IOException e) {
            throw new ServiceRuntimeException("Failed to encode filter!", e);
        }
    }
}
