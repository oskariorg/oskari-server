package org.oskari.service.wfs.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import fi.nls.oskari.service.ServiceRuntimeException;
import org.oskari.geojson.GeoJSONReader2;
import org.oskari.geojson.GeoJSONSchemaDetector;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OskariWFSClient {
    private static final Logger LOG = LogFactory.getLogger(OskariWFS110Client.class);
    private static final String EXC_HANDLING_OUTPUTFORMAT = "outputformat";
    private static final TypeReference<HashMap<String, Object>> TYPE_REF = new TypeReference<HashMap<String, Object>>() {};
    private static final ObjectMapper OM = new ObjectMapper();
    private static final int MAX_REDIRECTS = 5;

    public SimpleFeatureCollection getFeatures(
            String endPoint, String version,
            String user, String pass,
            String typeName, ReferencedEnvelope bbox,
            CoordinateReferenceSystem crs, int maxFeatures,
            Filter filter, List<String> formats) throws ServiceRuntimeException {
        return new OskariWFSLoadCommand(endPoint, version, user, pass,
                typeName, bbox, crs, maxFeatures, filter, formats).execute();
    }

    // Common methods for WFS 1.1.0 and 2.0.0 clients
    protected static String getBBOX(ReferencedEnvelope bbox) {
        if (bbox == null) {
            return null;
        }
        String srsName = bbox.getCoordinateReferenceSystem()
                .getIdentifiers()
                .iterator()
                .next()
                .toString();
        return String.format(Locale.US, "%f,%f,%f,%f,%s",
                bbox.getMinX(), bbox.getMinY(),
                bbox.getMaxX(), bbox.getMaxY(),
                srsName);
    }

    protected static byte[] getResponse(String endPoint,
                                        String user, String pass, Map<String, String> query) {
        try {
            HttpURLConnection conn = getConnection(endPoint, user, pass, query);
            return IOHelper.readBytes(conn);
        } catch (IOException e) {
            throw new ServiceRuntimeException("Unable to read response", e);
        }
    }

    protected static HttpURLConnection getConnection(String endPoint,
                                                     String user, String pass, Map<String, String> query) throws IOException {
        HttpURLConnection conn = IOHelper.getConnection(endPoint, user, pass, query);
        conn = IOHelper.followRedirect(conn, user, pass, query, MAX_REDIRECTS);
        int sc = conn.getResponseCode();
        if (sc != 200) {
            throw new ServiceRuntimeException("Unexpected status code " + sc, Integer.toString(sc));
        }
        return conn;
    }

    protected static SimpleFeatureCollection parseGeoJSON(InputStream in,
                                                        CoordinateReferenceSystem crs) throws IOException {
        Map<String, Object> geojson = OM.readValue(in, TYPE_REF);
        boolean ignoreGeometryProperties = true;
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(geojson, crs, ignoreGeometryProperties);
        return GeoJSONReader2.toFeatureCollection(geojson, schema);
    }

    protected static boolean isOutputFormatInvalid(InputStream in) {
        try {
            OWSException ex = OWSExceptionReportParser.parse(in);
            return isExceptionDueToInvalidOutputFormat(ex);
        } catch (Exception e) {
            LOG.debug(e);
            return false;
        }
    }

    /**
     * We might get:
     * <ows:ExceptionReport xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:ows="http://www.opengis.net/ows" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/ows http://schemas.opengis.net/ows/1.1.0/owsExceptionReport.xsd" version="1.1.0">
     * <ows:Exception exceptionCode="InvalidParameterValue" locator="Unknown">
     * <ows:ExceptionText>
     * <![CDATA[ OutputFormat 'application/json' not supported. ]]>
     * </ows:ExceptionText>
     * </ows:Exception>
     * </ows:ExceptionReport>
     * @param ex
     * @return
     */
    protected static boolean isExceptionDueToInvalidOutputFormat(OWSException ex) {
        if (ex.getExceptionCode().equalsIgnoreCase("InvalidParameterValue")) {
            if (EXC_HANDLING_OUTPUTFORMAT.equalsIgnoreCase(ex.getLocator())) {
                return true;
            }
            if (ex.getExceptionText() != null &&
                    ex.getExceptionText().toLowerCase().contains(EXC_HANDLING_OUTPUTFORMAT)) {
                return true;
            }
        }
        return false;
    }
}
