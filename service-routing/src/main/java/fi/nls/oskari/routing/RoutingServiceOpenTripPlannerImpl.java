package fi.nls.oskari.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.routing.pojo.PlanConnection;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Created by SMAKINEN on 26.6.2015.
 */
public class RoutingServiceOpenTripPlannerImpl implements RoutingService {
    private static final Logger LOGGER = LogFactory.getLogger(RoutingServiceOpenTripPlannerImpl.class);

    public static final String PARAM_ERROR = "error";
    public static final String PARAM_ERROR_MESSAGE = "message";
    public static final String PARAM_FROM_PLACE = "fromPlace";
    public static final String PARAM_TO_PLACE = "toPlace";
    public static final String PARAM_MODE = "mode";
    public static final String PARAM_MAX_WALK_DISTANCE = "maxWalkDistance";
    public static final String PARAM_WHEELCHAIR = "wheelchair";
    public static final String PARAM_LOCALE = "locale";
    public static final String PARAM_DATE = "date";
    public static final String PARAM_TIME = "time";
    public static final String PARAM_ARRIVE_BY = "arriveBy";
    public static final String PARAM_SHOW_INTERMEDIATE_STOPS = "showIntermediateStops";

    private static final String PROPERTY_USER = "routing.user";
    private static final String PROPERTY_PASSWORD = "routing.password";

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public RouteResponse getRoute(RouteParams params) {
        RouteParser parser = new RouteParser();

        //Transform coordinates for the route service
        final String targetSRS = PropertyUtil.get("routing.srs");
        final String sourceSRS = params.getSrs();
        final Point newFrom = ProjectionHelper.transformPoint(params.getFrom().getX(), params.getFrom().getY(), sourceSRS, targetSRS);
        final Point newTo = ProjectionHelper.transformPoint(params.getTo().getX(), params.getTo().getY(), sourceSRS, targetSRS);

        params.setFrom(newFrom.getLon(), newFrom.getLat());
        params.setTo(newTo.getLon(), newTo.getLat());

        PlanConnectionRequest planConnectionRequest = new PlanConnectionRequest();
        String routeJSONString = null;
        // Route route = null;
        RouteResponse result = new RouteResponse();
        try {
            // TODO: The old implementation had optional username & password here -> check if they could still be needed or even if they could be used? Probably not?
            String planConnectionRequestQuery =  planConnectionRequest.getQuery(params);
            HttpURLConnection conn  = IOHelper.post(PropertyUtil.get("routing.url"), "application/json", planConnectionRequestQuery.getBytes("UTF-8"));
            routeJSONString = IOHelper.readString(conn.getInputStream(), "UTF-8");

            if(!isErrorMessage(routeJSONString)){
                JSONObject routeJSON, planConnectionJSON = null;
                try {
                    routeJSON = new JSONObject(routeJSONString);
                    JSONObject routeData = routeJSON.has("data") ? routeJSON.getJSONObject("data") : null;
                    if (routeData != null) {
                        planConnectionJSON = routeData.has("planConnection") ? routeData.getJSONObject("planConnection") : null;
                    }
                } catch(JSONException e) {
                    LOGGER.error("Cannot parse response to JSONObject", e);
                }
                PlanConnection planConnection = null;
                if (planConnectionJSON != null) {
                    planConnection = mapper.readValue(planConnectionJSON.toString(), PlanConnection.class);
                }
                // TODO: why do we need the request parameters in result...?
                // result.setRequestParameters(parser.generateRequestParameters(route, params));
                result.setPlan(parser.mapPlanConnectionToPlan(planConnection, params));
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
                try {
                    JSONObject error = new JSONObject(routeJSONString);
                    if(error.has(PARAM_ERROR_MESSAGE)) {
                        result.setErrorMessage(error.getString(PARAM_ERROR_MESSAGE));
                    } else {
                        result.setErrorMessage("ERROR");
                    }
                } catch (JSONException ex){
                    LOGGER.warn("Cannot set error message to route response", ex);
                }
            }
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
    public boolean isErrorMessage(String response){
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
}
