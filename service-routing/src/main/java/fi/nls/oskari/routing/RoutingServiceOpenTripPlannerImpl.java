package fi.nls.oskari.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by SMAKINEN on 26.6.2015.
 */
public class RoutingServiceOpenTripPlannerImpl implements RoutingService {
    private static final Logger LOGGER = LogFactory.getLogger(RoutingServiceOpenTripPlannerImpl.class);

    private static final String PARAM_FROM_PLACE = "fromPlace";
    private static final String PARAM_TO_PLACE = "toPlace";
    private static final String PARAM_MODE = "mode";
    private static final String PARAM_MAX_WALK_DISTANCE = "maxWalkDistance";
    private static final String PARAM_WHEELCHAIR = "wheelchair";
    private static final String PARAM_LOCALE = "locale";
    private static final String PROPERTY_USER = "routing.user";
    private static final String PROPERTY_PASSWORD = "routing.password";


    ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<RouteResponse> getRoute(RouteParams params) {
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
        List<RouteResponse> result = new ArrayList<>();

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
            LOGGER.debug(routeJson);

            // FIXME route parsing not working anymore because of service change
            // Point parsing: https://github.com/opentripplanner/OpenTripPlanner/blob/e35ceb7042bd81889b947e38afe30a98a0d8b042/src/main/java/org/opentripplanner/util/PolylineEncoder.java
            // Geometry is Google encoded polyline, need convert to points and then generate geoJSON
            // https://developers.google.com/maps/documentation/utilities/polylineutility


            Route route = mapper.readValue(routeJson,Route.class);

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

            //TODO routeList includes three optional routes --> add them all to result
            /*
            for (Route route : routeList.get(0)) {
                RouteResponse routeresponse = new RouteResponse();
                final JSONObject responseGeoJson = parser.parseGeoJson(route, params.getSrs());
                routeresponse.setGeoJson(responseGeoJson);

                final JSONObject responseRoute = parser.parseRoute(route);
                routeresponse.setInstructions(responseRoute);
                result.add(routeresponse);

            }
            */

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private void setupDateAndTime(RouteParams params, Map<String, String> requestParams) {
        if (params.getDate() == null) {
            return;
        }
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm");
        // FIXME use en_US format
        SimpleDateFormat timeAmPmFormatter = new SimpleDateFormat("a");

        final String date = dateFormatter.format(params.getDate());
        requestParams.put("date", date);
        final String time = timeFormatter.format(params.getDate());
        final String amOrPm = timeAmPmFormatter.format(params.getDate());
        requestParams.put("time", time + amOrPm);

        if (params.getIsArriveBy()) {
            requestParams.put("arriveBy", "true");
        } else {
            requestParams.put("arriveBy", "false");
        }

    }
}
