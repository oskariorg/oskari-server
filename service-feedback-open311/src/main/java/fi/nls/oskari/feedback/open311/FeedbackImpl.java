package fi.nls.oskari.feedback.open311;


import fi.nls.oskari.feedback.FeedbackService;
import fi.nls.oskari.feedback.ServiceParams;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.domain.geo.Point;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Created by Oskari team on 12.4.2016.
 * Manages Open311 feedback requests
 */
public class FeedbackImpl implements FeedbackService {

    static final String KEY_SERVICELIST = "serviceList";
    static final String KEY_SERVICEDEFINITION = "serviceDefinition";
    static final String KEY_POSTFEEDBACK = "postFeedback";
    static final String KEY_GETFEEDBACK = "getFeedback";
    static final String PARAM_API_KEY = "api_key=";
    static final String PARAM_POST_CONTENTTYPE = "application/x-www-form-urlencoded; charset=utf-8";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private static final String PARAM_GEOJSON_FEATURES = "features";
    private static final String PARAM_GEOJSON_TYPE = "type";
    private static final String PARAM_GEOJSON_COORDINATES = "coordinates";
    private static final String PARAM_GEOJSON_GEOMETRY = "geometry";
    private static final String PARAM_GEOJSON_PROPERTIES = "properties";

    private static final Logger log = LogFactory.getLogger(
            FeedbackImpl.class);

    /**
     * Get Open311 request results
     *
     * @param params  request parameters  (params.getMethod() is used to switch to unique action
     * @return
     */
    @Override
    public JSONObject getServiceResult(ServiceParams params) {
        JSONObject result = new JSONObject();
        switch (params.getMethod()) {
            case ServiceParams.API_METHOD_SERVICELIST:
                result = getServiceList(params);
                break;
            case ServiceParams.API_METHOD_SERVICEDEFINITION:
                result = getServiceDefinition(params);
                break;
            case ServiceParams.API_METHOD_POST_FEEDBACK:
                result = postServiceFeedback(params);
                break;
            case ServiceParams.API_METHOD_GET_FEEDBACK:
                result = getServiceFeedback(params);
                break;

            default:
                log.warn("Unknown Open311 method in parameters");
                break;
        }
        return result;
    }

    /**
     * Get Open311 feedback service list (groups of feedback items)
     * @param params request parameters
     * @return
     */
    public JSONObject getServiceList(ServiceParams params) {

        JSONArray resultArr = new JSONArray();
        JSONObject result = new JSONObject();
        String resultString = null;
        // String requestUrl = "https://asiointi.hel.fi/palautews/rest/v1/services.json?locale=fi_FI";
        String requestUrl = params.getBaseUrl() + params.REST_SERVICES + params.REST_SERVICE_FORMAT +
                "locale=" + params.getLocale();

        try {
            final Map<String, String> headers = new HashMap<String, String>();
            headers.put("Accecpt", "application/json");

            resultString = getURLResponse(requestUrl, headers);
            resultArr = JSONHelper.createJSONArray(resultString);
            JSONHelper.putValue(result, KEY_SERVICELIST, resultArr);
        } catch (Exception e) {
            log.debug("Open311 service  list request failed", e);
        }

        return result;
    }

    /**
     * Get definition of of one feedback service
     * @param params params.getServiceId() is Open311 service_code
     * @return
     */
    public JSONObject getServiceDefinition(ServiceParams params) {

        JSONObject result = new JSONObject();
        String resultString = null;
        // String requestUrl = "https://asiointi.hel.fi/palautews/rest/v1/services/246.json?locale=fi_FI";
        String requestUrl = params.getBaseUrl() + params.REST_SERVICE_DEFINITION + params.getServiceId() +
                params.REST_SERVICE_FORMAT + "locale=" + params.getLocale();

        try {
            final Map<String, String> headers = new HashMap<String, String>();
            headers.put("Accecpt", "application/json");
            resultString = getURLResponse(requestUrl, headers);
            result = JSONHelper.createJSONObject(KEY_SERVICEDEFINITION, JSONHelper.createJSONObject(resultString));
        } catch (Exception e) {
            log.debug("Open311 service definition request failed", e);
        }

        return result;
    }

    /**
     * Post user's feedback data to Open311 service
     * @param params
     * @return
     */
    public JSONObject postServiceFeedback(ServiceParams params) {

        JSONObject result = new JSONObject();
        String resultString = null;
        String requestUrl = params.getBaseUrl() + params.REST_SERVICE_REQUEST + params.REST_SERVICE_FORMAT +
                "locale=" + params.getLocale();
        /*   sample feedback post data
             api_key=xyz&service_code=001&lat=37.76524078&long=122.4212043&address_string=Unioninkatu 25
                &email=jaakko.rajaniemi%40hel.fi&first_name=Jaakko&last_name=Rajaniemi&phone=111111111&
                description=Large+sinkhole+is+destroying+the+street
                &media_url=http%3A%2F%2Ffarm3.static.flickr.com%2F2002%2F225ed4760.jpg  */
        String requestData = parsePostFeedbackPostData(params);

        try {
            final HttpURLConnection con = IOHelper.getConnection(requestUrl);
            //IOHelper.trustAllCerts(con);
            //IOHelper.trustAllHosts(con);
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
            HttpURLConnection.setFollowRedirects(false);
            con.setUseCaches(false);
            con.setRequestProperty(HEADER_CONTENT_TYPE, PARAM_POST_CONTENTTYPE);
            IOHelper.writeToConnection(con, requestData);
            resultString = IOHelper.readString(con);

            JSONArray resultArr = JSONHelper.createJSONArray(resultString);
            JSONHelper.putValue(result, KEY_POSTFEEDBACK, resultArr);
        } catch (Exception e) {
            log.debug("Open311 feedback post request failed", e);
        }

        return result;

    }

    /**
     * Get posted feedbacks
     * @param params  filter values for requested feedbacks (see parseGetFeedbackUrl())
     * @return
     */
    public JSONObject getServiceFeedback(ServiceParams params) {

        JSONObject result = new JSONObject();
        String resultString = null;
        String requestUrl = parseGetFeedbackUrl(params);

        try {
            final Map<String, String> headers = new HashMap<String, String>();
            headers.put("Accecpt", "application/json");

            resultString = getURLResponse(requestUrl, headers);

            JSONArray resultArr = JSONHelper.createJSONArray(resultString);
            JSONObject geojsFeatures = parseFeatureCollection(resultArr, params);

            JSONHelper.putValue(result, KEY_GETFEEDBACK, geojsFeatures);
        } catch (Exception e) {
            log.debug("Posted feedbacks request failed (Open311)", e);
        }

        return result;
    }

    /**
     * Builds url for to get  posted feedbacks
     * @param params  filter params in params.getGetServiceRequests()
     * @return
     */
    public String parseGetFeedbackUrl(ServiceParams params) {

        //String requestUrl = "https://asiointi.hel.fi/palautews/rest/v1/requests.json?start_date=2016-01-01T00:00:00Z&end_date=2016-04-12T00:00:00Z&status=closed&locale=fi_FI";
        String requestUrl = params.getBaseUrl() + params.REST_SERVICE_REQUEST + params.REST_SERVICE_FORMAT +
                "locale=" + params.getLocale();
        StringBuilder urlBuilder = new StringBuilder(requestUrl);

        try {
            if (params.getGetServiceRequests() != null) {
                // Loop request filter params
                final Iterator<String> paramNames = params.getGetServiceRequests().keys();
                while (paramNames.hasNext()) {
                    String key = (String) paramNames.next();
                    urlBuilder.append("&" + key + "=" + JSONHelper.getStringFromJSON(params.getGetServiceRequests(), key, ""));
                }
            }
        } catch (Exception e) {
            log.debug("Open311 get feedback query params setup failed", e);
        }

        return urlBuilder.toString();
    }

    /**
     * Parse post data for posting feedback data
     * @param params post data values in params.getPostServiceRequest()
     * @return
     */
    public String parsePostFeedbackPostData(ServiceParams params) {


        StringBuilder urlBuilder = new StringBuilder(PARAM_API_KEY + params.getApiKey());

        try {
            if (params.getPostServiceRequest() != null) {
                // Loop request params
                final Iterator<String> paramNames = params.getPostServiceRequest().keys();
                while (paramNames.hasNext()) {
                    String key = (String) paramNames.next();
                    urlBuilder.append("&" + key + "=" + JSONHelper.getStringFromJSON(params.getPostServiceRequest(), key, ""));
                }
            }
        } catch (Exception e) {
            log.debug("Open311 posting feedback data params setup failed", e);
        }

        return urlBuilder.toString();
    }

    /**
     * Transforms source feedback location to Open311 CRS
     * @param serviceParams
     * @return
     */
    public Boolean transformFeedbackLocation(ServiceParams serviceParams) {
        Boolean done = false;

        if (serviceParams.getLon() != null && serviceParams.getLat() != null) {
            try {
                final Point fbLocation = ProjectionHelper.transformPoint(serviceParams.getLon(), serviceParams.getLat(), serviceParams.getSourceEpsg(), serviceParams.OPEN311_EPSG);
                serviceParams.setLon(fbLocation.getLatToString());
                serviceParams.setLat(fbLocation.getLonToString());
                done = true;
            } catch (Exception e) {
                log.error("Transforming feedback location point failed", e);
            }
        }

        return done;
    }

    /**
     * Parse geojson featurecollection for the response of Open311 feedback data
     * @param feedback
     * @param serviceParams
     * @return geojson featureCollection
     */
    public JSONObject parseFeatureCollection(JSONArray feedback, ServiceParams serviceParams) {
        final JSONObject featureCollection = new JSONObject();

        try {
            featureCollection.put(PARAM_GEOJSON_TYPE, "FeatureCollection");
            JSONArray featureList = new JSONArray();

            featureCollection.put(PARAM_GEOJSON_FEATURES, featureList);


            for (int i = 0; i < feedback.length(); i++) {
                JSONObject feature = feedback.getJSONObject(i);
                JSONObject gjfeature = parseGeoJson(feature, serviceParams);

                if (gjfeature != null) {
                    featureList.put(gjfeature);
                }
            }
            featureCollection.put(PARAM_GEOJSON_FEATURES, featureList);
        } catch (Exception e) {
            log.debug("Open311 get feedback query params setup failed", e);
        }


        return featureCollection;
    }

    /**
     * Parse geojson feature for one Open311 service feedback data
     * @param feature
     * @param serviceParams
     * @return geojson feature
     */
    public JSONObject parseGeoJson(JSONObject feature, ServiceParams serviceParams) {
        final JSONObject gjfeature = new JSONObject();

        try {
            // Has   coordinates
            if (!feature.has(ServiceParams.API_PARAM_KEY_LAT) || !feature.has(ServiceParams.API_PARAM_KEY_LONG)) {
                log.warn("no lon, lat coordinates in the feedback (Open311) feature");
                return null;
            }

            JSONObject point = new JSONObject();
            point.put(PARAM_GEOJSON_TYPE, "Point");


            JSONArray coordinates = parseCoordinates(feature, serviceParams);
            point.put(PARAM_GEOJSON_COORDINATES, coordinates);
            gjfeature.put(PARAM_GEOJSON_TYPE, "Feature");
            gjfeature.put(PARAM_GEOJSON_GEOMETRY, point);

            JSONObject properties = new JSONObject();
            final Iterator<String> items = feature.keys();
            while (items.hasNext()) {
                String key = (String) items.next();
                if (!key.equals(serviceParams.API_PARAM_KEY_LAT) && !key.equals(serviceParams.API_PARAM_KEY_LONG)) {
                    properties.put(key, JSONHelper.getStringFromJSON(feature, key, ""));
                }

            }
            gjfeature.put(PARAM_GEOJSON_PROPERTIES, properties);
        } catch (JSONException e) {
            log.error(e + "can't parse json object: " + e.getMessage());
        }

        return gjfeature;
    }

    /**
     * Parse and transform geojson coordinates for one Open311 location point
     * @param feature  Open311 service feature
     * @param serviceParams
     * @return  coordinates array
     */
    public JSONArray parseCoordinates(JSONObject feature, ServiceParams serviceParams) {
        final JSONArray coordinates = new JSONArray();

        try {
            double lon = feature.optDouble(serviceParams.API_PARAM_KEY_LONG, 0.0D);
            double lat = feature.optDouble(serviceParams.API_PARAM_KEY_LAT, 0.0D);
            final Point fbLocation = ProjectionHelper.transformPoint(lat, lon, serviceParams.OPEN311_EPSG, serviceParams.getSourceEpsg());
            coordinates.put(fbLocation.getLon());
            coordinates.put(fbLocation.getLat());

        } catch (JSONException e) {
            log.error("can't parse feedback coordinates : " + e.getMessage());
        }

        return coordinates;
    }
    private String getURLResponse(String url, Map<String, String> headers) {
        try {
        final HttpURLConnection con = IOHelper.getConnection(url);
        //IOHelper.trustAllCerts(con);
        //IOHelper.trustAllHosts(con);
        IOHelper.writeHeaders(con, headers);
        return IOHelper.readString(con.getInputStream(), "UTF-8");
    } catch (IOException e) {
        log.error("can't get Open311 feedback response: " + e.getMessage());
    }
        return null;
    }
}
