package fi.nls.oskari.feedback;


import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;

public class ServiceParams {

    // Request methods
    public static final String API_METHOD_SERVICELIST = "serviceList";
    public static final String API_METHOD_SERVICEDEFINITION = "serviceDefinition";
    public static final String API_METHOD_POST_FEEDBACK = "postFeedback";
    public static final String API_METHOD_GET_FEEDBACK = "getFeedback";
    // Feedback service CRS
    public static final String OPEN311_EPSG = "EPSG:4326";

    public static final String REST_SERVICES = "services";
    public static final String REST_SERVICE_FORMAT = ".json?";
    public static final String REST_SERVICE_REQUEST = "requests";
    public static final String REST_SERVICE_DEFINITION = "services/";
    public static final String REST_SERVICE_EXTENSIONS = "extensions=";
    public static final String PARAM_API_KEY = "api_key=";
    public static final String DEFAULT_POST_CONTENTTYPE = "application/x-www-form-urlencoded; charset=utf-8";
    public static final String API_PARAM_KEY_LAT = "lat";
    public static final String API_PARAM_KEY_LONG = "long";
    public static final String API_PARAM_KEY_BBOX = "bbox";
    public static final String API_PARAM_KEY_EXTENDED_ATTRIBUTES = "extended_attributes";
    public static final String API_PARAM_KEY_GEOMETRY = "geometry";

    static final String PARAM_METHOD = "method";


    private String method = null;
    private String baseUrl = null;
    private String apiKey = null;
    private String serviceId = null;
    private String baseUrlExtensions = "";
    private String postContentType = null;
    private String locale = PropertyUtil.getDefaultLanguage();
    private String sourceEpsg = "EPSG:3067";  // default
    private JSONObject PostServiceRequest = null;
    private JSONObject GetServiceRequests = null;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        // Add /, if not
        if (!baseUrl.substring(baseUrl.length() - 1).equals("/")) {
            this.baseUrl = baseUrl + "/";
        }
    }

    public String getBaseUrlExtensions() {
        return baseUrlExtensions;
    }

    public void setBaseUrlExtensions(String baseUrlExtensions) {
        this.baseUrlExtensions = REST_SERVICE_EXTENSIONS + baseUrlExtensions;
    }

    public String getPostContentType() {
        return postContentType;
    }

    public void setPostContentType(String postContentType) {
        this.postContentType = postContentType;
    }

    public JSONObject getGetServiceRequests() {
        return GetServiceRequests;
    }

    /* json content                                                              required item
    service_request_id 	To call multiple Service Requests at once,
                        multiple service_request_id can be declared; comma delimited. 	No
                        This overrides all other arguments.
    service_code 	    Specify the service type by calling the unique ID of the service_code. 	No
                        This defaults to all service codes when not declared;
                        can be declared multiple times, comma delimited
    start_date 	        Earliest requested_datetime to include in the search.
                        When provided with end_date, allows one to search for requests
                        which have a requested_datetime that matches a given range,
                        but may not span more than 90 days. 	                      No
                        Must use W3C format, e.g 2010-01-01T00:00:00Z.
    end_date 	        Latest requested_datetime to include in the search.
                        When provided with start_date, allows one to search for requests
                        which have a requested_datetime that matches a given range,
                        but may not span more than 90 days. 	                      No
                        Must use W3C format, e.g 2010-01-01T00:00:00Z.
    status 	            Allows one to search for requests which have a specific status.
                        This defaults to all statuses; can be declared multiple times, comma delimited. 	No
    extensions          The endpoint provides supplemental details about service requests that are in addition to
                        the ones described in the standard specification.
                        These data are nested in the 'extended_attributes' parameter in the Service Request response.
                        In order to retrieve the new supplemental details,
                        add the query parameter “extensions=true” to the request 	No
                        See chapter on Extensions.
    service_object_type	Describes the point of interest reference which is used
                        for identifying the request object. 	No
                        See chapter on Service Objects.
    service_object_id	Describes the ID of the service object 	No
                        If service_object_id is included in the request,
                        then service_object_type must be included.
    lat             	Latitude 	No
                        Location based search if lat, long and radius are given.
    long            	Longitude 	No
                        Location based search if lat, long and radius are given.
    radius           	Radius (meters) in which location based search performed. 	No
                        Location based search if lat, long and radius are given.
    updated_after       Earliest updated_datetime to include in search.
                        Allows one to search for requests which have an updated_datetime between
                        the updated_after time and updated_before time (or now).
                        This is useful for downloading a changeset that includes changes to older requests or
                        to just query very recent changes. 	No
                        Must use w3 format, eg 2010-01-01T00:00:00Z.
    updated_before      Latest updated_datetime to include in search.
                        Allows one to search for requests which have an updated_datetime between
                        the updated_after time and the updated_before time.
                        This is useful for downloading a changeset that includes changes to older requests or
                        to just query very recent changes. 	No
    bbox                Optional  Location based search if bbox is given &bbox=24.87,60.20,24.90,60.22 No
     */
    public void setGetServiceRequests(JSONObject getServiceRequests) {
        GetServiceRequests = getServiceRequests;
        PostServiceRequest = null;
    }


    public JSONObject getPostServiceRequest() {
        return PostServiceRequest;
    }

    /* Json content                                                  Required item
        api_key 	    Api key for submitting service requests 	        Yes (imported via properties)
        service_code 	The unique identifier for the service request type 	Yes
        description 	A full description of the service request. 	        Yes
                        This is free form text having min 10 and max 5,000 characters.
                        This may contain line breaks, but not html or code.
        title       	Title of the service requests 	No
        lat 	Latitude using the (WGS84) projection. 	Yes
        long 	Longitude using the (WGS84) projection. Yes
        service_object_type                             No
        service_object_id                               No
        address_string 	Human readable address or description of the location. 	No
        email 	 	No
        first_name 	No
        last_name 	No
        phone 	 	No
        media_url 	A URL to media associated with the request, e.g. an image 	No
        media 	Array of file uploads 	No
        geometry    geojson geometry   No
         */
    public void setPostServiceRequest(JSONObject postServiceRequest) {
        PostServiceRequest = postServiceRequest;
        GetServiceRequests = null;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getApiKey() {
        if (apiKey == null) {
            return "";
        }
        return PARAM_API_KEY + apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        if (locale != null && !locale.isEmpty()) {
            this.locale = locale;
        }
    }

    public String getSourceEpsg() {
        return sourceEpsg;
    }

    public void setSourceEpsg(String sourceEpsg) {
        if (sourceEpsg != null && !sourceEpsg.isEmpty()) {
            this.sourceEpsg = sourceEpsg;
        }
    }

    public String getLon() {
        Object lon = null;
        if (this.PostServiceRequest != null) {
            lon = JSONHelper.get(this.PostServiceRequest, API_PARAM_KEY_LONG);
        } else if (this.GetServiceRequests != null) {
            lon = JSONHelper.get(this.GetServiceRequests, API_PARAM_KEY_LONG);
        }
        if (lon instanceof String) {
            return lon.toString();
        } else if (lon instanceof Double) {
            return Double.toString((Double) lon);
        } else if (lon instanceof Long) {
            return Long.toString((Long) lon);
        } else if (lon instanceof Integer) {
            return Integer.toString((Integer) lon);
        }
        return null;
    }

    public String getBbox() {
        return JSONHelper.getStringFromJSON(this.GetServiceRequests, API_PARAM_KEY_BBOX, null);
    }

    public void setBbox(String bbox) {
        JSONHelper.putValue(this.GetServiceRequests, API_PARAM_KEY_BBOX, bbox);
    }

    public JSONObject getGeometry() {
        Object geom = JSONHelper.get(this.PostServiceRequest, API_PARAM_KEY_GEOMETRY);
        if (geom instanceof String) {
            return JSONHelper.createJSONObject(geom.toString());
        } else if (geom instanceof JSONObject) {
            return (JSONObject) geom;
        }
        return null;
    }

    public void setGeometry(String gjgeom) {
        JSONHelper.putValue(this.PostServiceRequest, API_PARAM_KEY_GEOMETRY, gjgeom);
    }

    public String getLat() {
        Object lat = null;
        if (this.PostServiceRequest != null) {
            lat = JSONHelper.get(this.PostServiceRequest, API_PARAM_KEY_LAT);
        } else if (this.GetServiceRequests != null) {
            lat = JSONHelper.get(this.GetServiceRequests, API_PARAM_KEY_LAT);
        }
        if (lat instanceof String) {
            return lat.toString();
        } else if (lat instanceof Double) {
            return Double.toString((Double) lat);
        } else if (lat instanceof Long) {
            return Long.toString((Long) lat);
        } else if (lat instanceof Integer) {
            return Integer.toString((Integer) lat);
        }
        return null;
    }

    public void setLon(String lon) {
        if (this.PostServiceRequest != null) {
            JSONHelper.putValue(this.PostServiceRequest, API_PARAM_KEY_LONG, lon);
        } else if (this.GetServiceRequests != null) {
            JSONHelper.putValue(this.GetServiceRequests, API_PARAM_KEY_LONG, lon);
        }
    }

    public void setLat(String lat) {
        if (this.PostServiceRequest != null) {
            JSONHelper.putValue(this.PostServiceRequest, API_PARAM_KEY_LAT, lat);
        } else if (this.GetServiceRequests != null) {
            JSONHelper.putValue(this.GetServiceRequests, API_PARAM_KEY_LAT, lat);
        }
    }

    public JSONObject toJSON() {

        return JSONHelper.createJSONObject(PARAM_METHOD, this.getMethod());

    }
}