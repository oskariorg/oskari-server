package org.oskari.service.wfs.client;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.filter.v2_0.FESConfiguration;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.xml.Encoder;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Client code for WFS 2.0.0 services
 */
public class OskariWFS2Client {

    private static final Logger LOG = LogFactory.getLogger(OskariWFS2Client.class);

    private static final OskariGML32 OSKARI_GML32 = new OskariGML32();

    private OskariWFS2Client() {}

    /**
     * @return SimpleFeatureCollection containing the parsed Features, or null if all fails
     */
    public static SimpleFeatureCollection getFeatures(OskariLayer layer,
            ReferencedEnvelope bbox, CoordinateReferenceSystem crs, Filter filter) {
        String endPoint = layer.getUrl();
        String typeName = layer.getName();
        String user = layer.getUsername();
        String pass = layer.getPassword();

        int maxFeatures = OskariWFSClient.getMaxFeatures(layer);

        Map<String, String> query = getQueryParams(typeName, bbox, crs, maxFeatures, filter);

        byte[] response = new byte[0];

        if (OskariWFSClient.tryGeoJSON(layer)) {
            // First try GeoJSON
            query.put("OUTPUTFORMAT", "application/json");
            try {
                response = OskariWFSClient.getResponse(endPoint, user, pass, query);
                return OskariWFSClient.parseGeoJSON(new ByteArrayInputStream(response), crs);
            } catch (ServiceRuntimeException e) {
                if (!"400".equals(e.getMessageKey())) throw e;
                // fallback to gml
            } catch (IOException e) {
                if (!OskariWFSClient.isOutputFormatInvalid(new ByteArrayInputStream(response))) {
                    // If we can not determine that the exception was due to bad
                    // outputFormat parameter then don't bother trying GML
                    final String url = IOHelper.constructUrl(endPoint, query);
                    LOG.debug("Response from", url, "was:\n", new String(response, StandardCharsets.UTF_8));
                    throw new ServiceRuntimeException("Unable to parse GeoJSON from " + url, e);
                }
            }
        }

        // Fallback to GML
        query.remove("OUTPUTFORMAT");
        response = OskariWFSClient.getResponse(endPoint, user, pass, query);

        try {
            return OSKARI_GML32.decodeFeatureCollection(new ByteArrayInputStream(response), user, pass);
        } catch (Exception e) {
            final String url = IOHelper.constructUrl(endPoint, query);
            LOG.debug("Response from", url, "was:\n", new String(response, StandardCharsets.UTF_8));
            throw new ServiceRuntimeException("Unable to parse GML from " + url, e);
        }
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
            Encoder encoder = new Encoder(new FESConfiguration());
            encoder.setOmitXMLDeclaration(true);
            // https://docs.geoserver.org/stable/en/user/filter/syntax.html
            // ilter Encoding 2.0 is used in WFS 2.0
            return encoder.encodeAsString(filter, org.geotools.filter.v2_0.FES.Filter);
        } catch (IOException e) {
            throw new ServiceRuntimeException("Failed to encode filter!", e);
        }
    }

}
