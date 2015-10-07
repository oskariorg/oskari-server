package fi.nls.oskari.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by SMAKINEN on 26.6.2015.
 */
public class RoutingServiceOpenTripPlannerImpl implements RoutingService {
    private static final Logger LOGGER = LogFactory.getLogger(RoutingServiceOpenTripPlannerImpl.class);


    private static final String PARAM_ERROR = "error";
    private static final String PARAM_ERROR_MESSAGE = "message";
    private static final String PARAM_FROM_PLACE = "fromPlace";
    private static final String PARAM_TO_PLACE = "toPlace";
    private static final String PARAM_MODE = "mode";
    private static final String PARAM_MAX_WALK_DISTANCE = "maxWalkDistance";
    private static final String PARAM_WHEELCHAIR = "wheelchair";
    private static final String PARAM_LOCALE = "locale";
    private static final String PARAM_DATE = "date";
    private static final String PARAM_TIME = "time";
    private static final String PARAM_ARRIVE_BY = "arriveBy";

    private static final String PROPERTY_USER = "routing.user";
    private static final String PROPERTY_PASSWORD = "routing.password";



    ObjectMapper mapper = new ObjectMapper();

    @Override
    public RouteResponse getRoute(RouteParams params) {
        RouteParser parser = new RouteParser();
        Map<String, String> requestParams = new HashMap<String, String>();

        //Transform coordinates for the route service
        final String targetSRS = PropertyUtil.get("routing.srs");
        final String sourceSRS = params.getSrs();
        final Point newFrom = ProjectionHelper.transformPoint(params.getFrom().getX(), params.getFrom().getY(), sourceSRS, targetSRS);
        final Point newTo = ProjectionHelper.transformPoint(params.getTo().getX(), params.getTo().getY(), sourceSRS, targetSRS);

        final String from =  newFrom.getLonToString() + "," + newFrom.getLatToString();
        requestParams.put(PARAM_FROM_PLACE, from);

        final String to = newTo.getLonToString() + "," + newTo.getLatToString();
        requestParams.put(PARAM_TO_PLACE, to);

        setupDateAndTime(params, requestParams);

        // mode can be a one of this or combine: BUSISH, TRAINISH, AIRPLANE, BICYCLE, WALK, TRANSIT, CAR, CAR_PARK, BICYCLE_PARK
        requestParams.put(PARAM_MODE, params.getMode());
        requestParams.put(PARAM_MAX_WALK_DISTANCE, Long.toString(params.getMaxWalkDistance()));
        requestParams.put(PARAM_WHEELCHAIR, params.getIsWheelChair().toString());
        requestParams.put(PARAM_LOCALE, params.getLang());


        final String requestUrl = IOHelper.constructUrl(PropertyUtil.get("routing.url"), requestParams);
        RouteResponse result = new RouteResponse();

        try {
            LOGGER.debug(requestUrl);
            final Map<String, String> headers = new HashMap<String,String>();
            headers.put("Accecpt", "application/json");
            String routeJson = null;
            final String username = PropertyUtil.getOptional(PROPERTY_USER);
            final String password = PropertyUtil.getOptional(PROPERTY_PASSWORD);
            if(username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                routeJson = IOHelper.getURL(requestUrl, username, password, headers, "UTF-8");
            } else {
                routeJson = IOHelper.getURL(requestUrl,headers, "UTF-8");
            }

            Route route = mapper.readValue(routeJson,Route.class);
            RouteResponse response = new RouteResponse();

            if(!isErrorMessage(routeJson)){
                response.setRequestParameters(parser.generateRequestParameters(route, params));
                response.setPlan(parser.generatePlan(route, params));
                response.setSuccess(true);
            } else {
                response.setSuccess(false);
                try {
                    JSONObject error = new JSONObject(routeJson);
                    if(error.has(PARAM_ERROR_MESSAGE)) {
                        response.setErrorMessage(error.getString(PARAM_ERROR_MESSAGE));
                    } else {
                        response.setErrorMessage("ERROR");
                    }
                } catch (JSONException ex){
                    LOGGER.warn("Cannot set error message to route response", ex);
                }
            }






            for(Itinerary itinerary : route.getPlan().getItineraries()){
                RouteResponse routeresponse = new RouteResponse();
                final JSONObject responseGeoJson = parser.parseGeoJson(itinerary, params.getSrs());
                routeresponse.setGeoJson(responseGeoJson);
/*
                final JSONObject responseRoute = parser.parseRoute(itinerary, params.getSrs());
                routeresponse.setInstructions(responseRoute);
                */
                result.add(routeresponse);
            }
            //Route route = mapper.readValue(routeJson, mapper.getTypeFactory().constructCollectionType(List.class, mapper.getTypeFactory().constructCollectionType(List.class, Route.class)));



        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }





    /**
     * Check at if route repsonse contains error
     * @param response route response
     * @return true if contains error, other false
     */
    private boolean isErrorMessage(String response){
        try {
            JSONObject job = new JSONObject(response);
            if(job.has(PARAM_ERROR)) {
                return true;
            }
        } catch(JSONException ex){
            LOGGER.warn("Cannot check route error message", ex);
        }
        return false;
    }

    /**
     * Setup date and time parameters
     * @param params
     * @param requestParams
     */
    private void setupDateAndTime(RouteParams params, Map<String, String> requestParams) {
        if (params.getDate() == null) {
            return;
        }
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
        SimpleDateFormat timeAmPmFormatter = new SimpleDateFormat("a", Locale.ENGLISH);

        final String date = dateFormatter.format(params.getDate());
        requestParams.put(PARAM_DATE, date);
        final String time = timeFormatter.format(params.getDate());
        final String amOrPm = timeAmPmFormatter.format(params.getDate());
        requestParams.put(PARAM_TIME, time + amOrPm);

        if (params.getIsArriveBy()) {
            requestParams.put(PARAM_ARRIVE_BY, "true");
        } else {
            requestParams.put(PARAM_ARRIVE_BY, "false");
        }

    }
}
