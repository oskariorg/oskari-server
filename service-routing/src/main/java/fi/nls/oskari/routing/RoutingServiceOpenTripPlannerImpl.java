package fi.nls.oskari.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.routing.pojo.PlanConnection;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import static fi.nls.oskari.util.IOHelper.HEADER_ACCEPT_LANGUAGE;
import static fi.nls.oskari.util.IOHelper.getConnection;
import static fi.nls.oskari.util.IOHelper.setContentType;
import static fi.nls.oskari.util.IOHelper.writeHeader;
import static fi.nls.oskari.util.IOHelper.writeToConnection;

/**
 * Created by SMAKINEN on 26.6.2015.
 */
public class RoutingServiceOpenTripPlannerImpl implements RoutingService {
    private static final Logger LOGGER = LogFactory.getLogger(RoutingServiceOpenTripPlannerImpl.class);

    public static final String PARAM_ERROR = "error";
    public static final String PARAM_ERRORS = "errors";
    public static final String PARAM_ERROR_MESSAGE = "message";

    private static final String PROPERTY_USER = "routing.user";
    private static final String PROPERTY_PASSWORD = "routing.password";

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public RouteResponse getRoute(RouteParams params) throws ServiceException {
        RouteParser parser = new RouteParser();

        //Transform coordinates for the route service
        final String targetSRS = PropertyUtil.getNecessary("routing.srs", "No target SRS provided!");
        final String sourceSRS = params.getSrs();
        final Point newFrom = ProjectionHelper.transformPoint(params.getFrom().getX(), params.getFrom().getY(), sourceSRS, targetSRS);
        final Point newTo = ProjectionHelper.transformPoint(params.getTo().getX(), params.getTo().getY(), sourceSRS, targetSRS);

        params.setFrom(newFrom.getLon(), newFrom.getLat());
        params.setTo(newTo.getLon(), newTo.getLat());

        PlanConnectionRequest planConnectionRequest = new PlanConnectionRequest();
        String apiResponseString = null;
        RouteResponse result = new RouteResponse();
        try {
            final String username = PropertyUtil.getOptional(PROPERTY_USER);
            final String password = PropertyUtil.getOptional(PROPERTY_PASSWORD);
            final String contentType = "application/json";
            String planConnectionRequestQuery =  planConnectionRequest.getQuery(params);
            String url = PropertyUtil.getNecessary("routing.url", "No routing URL configured to properties!");
            HttpURLConnection connection = getConnection(url, username, password);
            setContentType(connection, contentType);
            writeHeader(connection, HEADER_ACCEPT_LANGUAGE, params.getLang());
            writeToConnection(connection, planConnectionRequestQuery.getBytes("UTF-8"));

            apiResponseString = IOHelper.readString(connection.getInputStream(), "UTF-8");
            if(!isErrorMessage(apiResponseString)){
                JSONObject responseData, planConnectionJSON = null;
                try {
                    responseData = new JSONObject(apiResponseString);
                    JSONObject routeData = responseData.has("data") ? responseData.getJSONObject("data") : null;
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
                result.setRequestParameters(parser.generateRequestParameters(params));
                result.setPlan(parser.mapPlanConnectionToPlan(planConnection, params));
                result.setRoutingErrors(parser.generateRoutingErrors(planConnection));
                result.setPlanConnectionQueryString(planConnectionRequestQuery);
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
                result.setPlanConnectionQueryString(planConnectionRequestQuery);
                result.setErrorMessage(this.getErrorMessage(apiResponseString));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private String getErrorMessage(String errorsJSON) {
        try {
            JSONObject errorsJSONObject = new JSONObject(errorsJSON);
            JSONArray errors = null;
            JSONObject firstError = null;
            if (errorsJSONObject.has(PARAM_ERRORS)) {
                errors = errorsJSONObject.getJSONArray(PARAM_ERRORS);
            }

            if (errors != null && errors.length() > 0) {
                firstError = errors.getJSONObject(0);
            }

            // return the message of the first error in the array if available.
            if(firstError.has(PARAM_ERROR_MESSAGE)) {
                return firstError.getString(PARAM_ERROR_MESSAGE);
            }
        } catch (JSONException ex){
            LOGGER.warn("Cannot extract error message from response.", ex);
        }

        return "ERROR";

    }

    /**
     * Check at if route repsonse contains error
     * @param response route response
     * @return true if contains error, other false
     */
    public boolean isErrorMessage(String response){
        try {
            JSONObject job = new JSONObject(response);
            if(job.has(PARAM_ERROR) || job.has(PARAM_ERRORS)) {
                return true;
            }
        } catch(JSONException ex){
            LOGGER.warn("Cannot check route error message", ex);
        }
        return false;
    }
}
