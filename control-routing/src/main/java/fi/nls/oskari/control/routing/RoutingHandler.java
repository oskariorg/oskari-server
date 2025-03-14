package fi.nls.oskari.control.routing;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.routing.RouteParams;
import fi.nls.oskari.routing.RouteResponse;
import fi.nls.oskari.routing.RoutingService;
import fi.nls.oskari.routing.RoutingServiceOpenTripPlannerImpl;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static fi.nls.oskari.control.ActionConstants.PARAM_LANGUAGE;
import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;

/**
 * Created by SMAKINEN on 26.6.2015.
 */

@OskariActionRoute("Routing")
public class RoutingHandler extends ActionHandler {

    private RoutingService service = new RoutingServiceOpenTripPlannerImpl();


    private static final String PARAM_FROM_LAT = "fromlat";
    private static final String PARAM_FROM_LON = "fromlon";
    private static final String PARAM_TO_LAT = "tolat";
    private static final String PARAM_TO_LON = "tolon";
    private static final String PARAM_DATE = "date";
    private static final String PARAM_TIME = "time";
    private static final String PARAM_ARRIVEBY = "arriveby";
    private static final String PARAM_MODE = "mode";
    private static final String PARAM_MAX_WALK_DISTANCE = "maxwalkdistance";
    private static final String PARAM_WHEELCHAIR = "wheelchair";
    public static final String PARAM_SHOW_INTERMEDIATE_STOPS = "showIntermediateStops";

    public static final String ROUTING_ERRORS = "routingErrors";

    public static final String ROUTING_QUERY = "query";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        RouteParams routeparams = new RouteParams();
        routeparams.setFrom(params.getRequiredParamDouble(PARAM_FROM_LON), params.getRequiredParamDouble(PARAM_FROM_LAT));
        routeparams.setTo(params.getRequiredParamDouble(PARAM_TO_LON), params.getRequiredParamDouble(PARAM_TO_LAT));

        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
        final String dateTime =
                params.getHttpParam(PARAM_DATE, LocalDateTime.now(ZoneId.systemDefault()).format(dateFormatter)) + " " +
                params.getHttpParam(PARAM_TIME, LocalDateTime.now(ZoneId.systemDefault()).format(timeFormatter));
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd H:mm");
            LocalDateTime ldt = LocalDateTime.parse(dateTime, dateTimeFormatter);
            OffsetDateTime odt = ldt.atOffset(ldt.atZone(ZoneId.systemDefault()).getOffset());
            routeparams.setDate(odt);

        } catch (DateTimeParseException e) {
            throw new ActionParamsException("Couldn't parse date");
        }
        routeparams.setIsArriveBy("true".equals(params.getHttpParam(PARAM_ARRIVEBY)));
        routeparams.setIsWheelChair("true".equals(params.getHttpParam(PARAM_WHEELCHAIR)));

        routeparams.setSrs(params.getHttpParam(PARAM_SRS));
        routeparams.setLang(params.getHttpParam(PARAM_LANGUAGE));
        routeparams.setMode(params.getHttpParam(PARAM_MODE, PropertyUtil.get("routing.default.mode")));

        try {
            RouteResponse result = service.getRoute(routeparams);

            JSONObject response = result.toJSON();
            if(params.getUser().isAdmin()) {
                JSONHelper.putValue(response, "otpUrl", result.getRequestUrl());
                JSONHelper.putValue(response, ROUTING_ERRORS, result.getRoutingErrors());
                JSONHelper.putValue(response, ROUTING_QUERY, result.getPlanConnectionQueryString());
            }

            ResponseHelper.writeResponse(params, response);
        } catch(ServiceException ex) {
            throw new ActionException(ex.getMessage(), ex);
        }

    }
}
