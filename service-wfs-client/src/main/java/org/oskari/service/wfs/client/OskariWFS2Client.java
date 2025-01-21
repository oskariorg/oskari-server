package org.oskari.service.wfs.client;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.xsd.Encoder;
import org.geotools.api.filter.Filter;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Client code for WFS 2.0.0 services
 */
public class OskariWFS2Client {

    private static final OskariGML32 OSKARI_GML32 = new OskariGML32();

    private OskariWFS2Client() {}

    /**
     * @return SimpleFeatureCollection containing the parsed Features, or null if all fails
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
        return OskariWFSClient.getFeatures(endPoint, user, pass, query, crs, tryGeoJSON, OSKARI_GML32);
    }

    protected static Map<String, String> getQueryParams(String typeName, ReferencedEnvelope bbox,
            CoordinateReferenceSystem crs, int maxFeatures, Filter filter) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("SERVICE", "WFS");
        parameters.put("VERSION", "2.0.0");
        parameters.put("REQUEST", "GetFeature");
        parameters.put("TYPENAMES", typeName);
        parameters.put("SRSNAME", crs.getIdentifiers().iterator().next().toString());
        if (filter == null) {
            parameters.put("BBOX", OskariWFSClient.getBBOX(bbox));
        } else {
            parameters.put("FILTER", getFilter(filter));
        }
        parameters.put("COUNT", Integer.toString(maxFeatures));
        return parameters;
    }


    protected static String getFilter(Filter filter) {
        if (filter == null) {
            return null;
        }
        try {
            // https://docs.geoserver.org/stable/en/user/filter/syntax.html
            // Filter Encoding 2.0 is used in WFS 2.0
            Encoder encoder = new Encoder(new org.geotools.filter.v2_0.FESConfiguration());
            encoder.setOmitXMLDeclaration(true);
            return encoder.encodeAsString(filter, org.geotools.filter.v2_0.FES.Filter);
        } catch (IOException e) {
            throw new ServiceRuntimeException("Failed to encode filter!", e);
        }
    }

}
