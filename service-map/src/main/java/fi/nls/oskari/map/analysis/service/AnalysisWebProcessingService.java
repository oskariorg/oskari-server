package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.analysis.AnalysisParser;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.map.analysis.domain.AnalysisMethodParams;
import fi.nls.oskari.map.analysis.domain.DifferenceMethodParams;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.XmlHelper;
import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class AnalysisWebProcessingService {

    private static final Logger log = LogFactory.getLogger(
            AnalysisWebProcessingService.class);

    private static final String GEOSERVER_WPS_URL = "geoserver.wps.url";
    private static final String GEOJSON_LAYER2 = "layer2";
    private static final String GEOJSON_GEOMETRY = "geometry";
    private static final String GEOJSON_PROPERTIES = "properties";
    private static final String DELTA_FIELD_NAME = "Muutos_t2-t1";
    private static final Double NON_AUTHORIZED = -111111111.0D;

    /**
     * Get WPS results as wfs FeatureCollection
     * @param analysisLayer WPS method params
     * @return response of WPS (xml FeatureCollection)
     * @throws ServiceException
     */
    public String requestFeatureSet(final AnalysisLayer analysisLayer)  throws ServiceException {
        try {
            // 1) Get Analysis Specific WPS XML
            final AnalysisMethodParams methodParams = analysisLayer
                    .getAnalysisMethodParams();
            final Document doc = methodParams.getWPSXML2();
            return this.requestWPS(doc);
        } catch (Exception e) {
            throw new ServiceException("requestFeatureSet failed due to wps request build", e);
        }
    }

    /**
     *  Get WPS execute response
     * @param doc  WPS execute request (xml)
     * @return
     * @throws ServiceException
     */
    private String requestWPS(final Document doc)
            throws ServiceException {
        InputStream inp = null;
        try {
            final String wpsUrl = PropertyUtil.get(GEOSERVER_WPS_URL);
            final String wpsUser = PropertyUtil.get("geoserver.wms.user");
            final String wpsUserPass = PropertyUtil.get("geoserver.wms.pass");

            final HttpURLConnection connection = IOHelper.getConnection(wpsUrl, wpsUser, wpsUserPass);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type",
                    "application/xml; charset=UTF-8");

            final OutputStream outs = connection.getOutputStream();

            // 2) Transform XML to POST body
            // Use a Transformer for output
            final TransformerFactory tFactory = XmlHelper.newTransformerFactory();
            final Transformer transformer = tFactory.newTransformer();

            final DOMSource source = new DOMSource(doc);
            final StreamResult result = new StreamResult(outs);
            transformer.transform(source, result);

            if (log.isDebugEnabled()) {
                final DOMSource source2 = new DOMSource(doc);
                final StreamResult result2 = new StreamResult(System.out);
                transformer.transform(source2, result2);
            }
            // 4) Returned WPS result
            outs.close();
            inp = connection.getInputStream();
            final String results = IOHelper.readString(inp);
            // log.debug("We got results from GeoServer WPS", results);
            log.debug("We got results from GeoServer WPS");
            return results;

        } catch (Exception e) {
            throw new ServiceException("requestFeatureSet failed due to", e);
        } finally {
            try {
                if(inp != null) {
                    inp.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Request feature collection via WFS 2.0.0 GetFeature post
     * @param analysisLayer input data for GetFeature post xml
     * @return feature collection of delta values of predefined properties of two feature collections
     * @throws ServiceException
     */

    public String requestWFS2FeatureSet(final AnalysisLayer analysisLayer) throws ServiceException {
        String featureSet = null;
        try {
            // 1) Get Analysis Specific WFS 2.0 GetFeature XML
            final DifferenceMethodParams params = (DifferenceMethodParams) analysisLayer
                    .getAnalysisMethodParams();
            // Geojson response
            final String rawFeatureSet = this.requestWFS2(params.getServiceUrl(), params.getWFSXML2(), params.getServiceUser(), params.getServicePw());
            // Loop geojson features and process property difference values
            featureSet = processDifferenceValueFS(params.getTypeName(), params.getTypeName2(), params.getKeyA1(),
                    params.getFieldA1(), params.getFieldB1(), params.getNoDataValue(), rawFeatureSet);
            // Set fields order because geojson doesn't keep property order
            analysisLayer.setFields(this.FieldsOrder(params));

        } catch (Exception e) {
            throw new ServiceException("request GetFeature failed due to wfs 2.0 request build", e);
        }
        return featureSet;
    }

    /**
     * Post request to  WFS service or any
     * @param wfsUrl  request url
     * @param request_data  post data  (e.g. GetFeature xml)
     * @param user  username
     * @param pw
     * @return response in geojson format  (e.g. featureCollection )
     * @throws ServiceException
     */
    public String requestWFS2(final String wfsUrl, final String request_data, final String user, final String pw) throws ServiceException {
        String response = null;
        try {
            // GetFeature response
            response = IOHelper.httpRequestAction(wfsUrl, request_data,user, pw, null, null, "application/json");

        } catch (Exception e) {
            throw new ServiceException("request GetFeature failed due to", e);
        }
        return response;
    }

    /**
     *  Build new feature collection with delta values
     *  1. Loop WFS 2.0.0 response geojson (join of two feature collections)
     *  2. process new geojson with delta values (fieldA1 - fieldB1)
     *  3. encode geojson to gml feature collection (xml)
     *  @param lay1 {String} name of dataset 1
     *  @param lay2 {String} name of dataset 2
     * @param keyA1 {String} name of join property
     * @param fieldA1 {String}  field name of 1st feature collection
     * @param fieldB1 {String}  field name of 2nd feature collection
     * @param nodata {String} No data value; if fieldA1 value is nodata value, then delta is 0
     * @param rawJson {String} WFS 2.0.0 response geojson
     * @return gml feature collection (xml)
     */
    public String processDifferenceValueFS(String lay1, String lay2, String keyA1, String fieldA1, String fieldB1, String nodata, String rawJson) {


        String featureSet = null;
        FeatureCollection fc = null;
        double dnodata = Double.NaN;
        try {
            if (nodata != null) dnodata = Double.parseDouble(nodata);
        } catch (Exception e) {
            log.debug("Unable to parse nodata value in difference analyse",
                    e);

        }

        try {
            final JSONObject geoJson = JSONHelper.createJSONObject(rawJson);
            FeatureJSON fjs = new FeatureJSON();
            JSONArray newfeas = new JSONArray();

            JSONArray geofeas = geoJson.getJSONArray("features");

            // Loop json features and fix to user_layer_data structure
            for (int i = 0; i < geofeas.length(); i++) {

                JSONObject geofea = geofeas.optJSONObject(i);
                if (geofea == null) continue;
                if (!geofea.has(GEOJSON_GEOMETRY)) continue;
                if (!geofea.has(GEOJSON_PROPERTIES)) continue;
                JSONObject properties = geofea.getJSONObject(GEOJSON_PROPERTIES);
                if (!properties.has(fieldA1)) continue;
                // layer2 is alias name of joined feature to feature
                if (!properties.has(GEOJSON_LAYER2)) continue;
                final Object valueA = properties.get(fieldA1);
                final String layer2fea = properties.optString(GEOJSON_LAYER2);
                double delta = this.findValueDifference(valueA, layer2fea, fieldB1, dnodata);
                double valueB = valueToDouble(valueA);
                if(delta != NON_AUTHORIZED){
                    valueB = valueB - delta;
                    delta = -delta;
                }

                JSONObject newproperties = new JSONObject();
                newproperties.put(keyA1, properties.get(keyA1));
                newproperties.put("t1__" + lay1.replace(":","_") + "__" + fieldA1, valueA);
                newproperties.put("t2__" + lay2.replace(":","_") + "__" + fieldB1, valueB);

                newproperties.put(DELTA_FIELD_NAME, delta);
                geofea.remove(GEOJSON_PROPERTIES);
                geofea.put(GEOJSON_PROPERTIES, newproperties);
                newfeas.put(geofea);

            }
            // update geojson
            geoJson.remove("features");
            geoJson.put("features", newfeas);

            // to FeatureCollection
            String tempjs = JSONHelper.getStringFromJSON(geoJson, null);
            fc = fjs.readFeatureCollection(new ByteArrayInputStream(
                    tempjs.getBytes("utf-8")));

            org.geotools.xml.Configuration configuration = new org.geotools.gml3.GMLConfiguration();
            org.geotools.xml.Encoder encoder = new org.geotools.xml.Encoder(configuration);

            //output stream to serialize to
            OutputStream xml = new ByteArrayOutputStream();


            try {
                encoder.encode(fc, org.geotools.gml3.GML._FeatureCollection, xml);
                xml.close();
                featureSet = xml.toString();

            } catch (IOException e) {
                log.debug("GML encoding failed");
            }

        } catch (Exception e) {
            log.debug("Unable to process value differences",
                    e);
            return null;
        }

        return featureSet;
    }

    /**
     * Computes delta value of feature A field fieldA1 and feature B fieldB1
     *
     * @param valueA1 feature A field value of fieldA1
     * @param fea2    feature B in unstructured nonstandard format
     * @param fieldB1 field name of feature B
     * @param nodata  no data value, which is skipped in delta value computation (return value is 0d)
     * @return
     */
    private double findValueDifference(Object valueA1, String fea2, String fieldB1, double nodata) {
        double delta = 0d;
        double dA1 = 0d;
        double dB1 = 0d;

        try {
            dA1 = valueToDouble(valueA1);
            String atemp = fea2.substring(fea2.indexOf("["), fea2.length() - 1);
            String[] attributes = atemp.split(",");
            for (String attribute : attributes) {
                String[] parts = attribute.split("[:<=]+");
                if (parts.length > 1) {
                    if (parts[1].trim().equals(fieldB1)) {
                        dB1 = Double.parseDouble(parts[parts.length - 1]);
                    }
                }
            }
            delta = dA1 - dB1;
            if (!Double.isNaN(nodata) && (dA1 == nodata || dB1 == nodata)) delta = NON_AUTHORIZED;

        } catch (Exception e) {
            log.debug("delta value computation failed");
        }
        return delta;
    }

    private double valueToDouble(Object valueA1) {
        double dA1 = 0d;

        try {
            if (valueA1 instanceof String) {
                String temp = String.valueOf(valueA1);
                dA1 = Double.parseDouble(temp);
            }
            if (valueA1 instanceof Integer) {
                dA1 = ((Integer) valueA1).doubleValue();
            } else {

                dA1 = (Double) valueA1;
            }
        } catch (Exception e) {

        }
        return dA1;
    }
    private List<String> FieldsOrder(DifferenceMethodParams params) {
        List<String> fields = new ArrayList<String>();

        if (params.getMethod().equals(AnalysisParser.DIFFERENCE)){
            fields.add("t1__" + params.getTypeName().replace(":", "_") + "__" + params.getFieldA1());
            fields.add("t2__" + params.getTypeName2().replace(":", "_") + "__" + params.getFieldB1());
            fields.add(DELTA_FIELD_NAME);
            fields.add(params.getKeyA1());

        }
        return fields;

    }
}
