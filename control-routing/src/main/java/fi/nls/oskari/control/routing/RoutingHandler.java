package fi.nls.oskari.control.routing;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.routing.*;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by SMAKINEN on 26.6.2015.
 */

@OskariActionRoute("Routing")
public class RoutingHandler extends ActionHandler {

    private RoutingService service = new RoutingServiceOpenTripPlannerImpl();

    private static final String PARAM_SRS = "srs";
    private static final String PARAM_LANG = "lang";
    private static final String PARAM_FROM_LAT = "fromlat";
    private static final String PARAM_FROM_LON = "fromlon";
    private static final String PARAM_TO_LAT = "tolat";
    private static final String PARAM_TO_LON = "tolon";
    private static final String PARAM_DATE = "date";
    private static final String PARAM_TIME = "time";
    private static final String PARAM_TIMETYPE = "timetype";
    private static final String PARAM_VIA_TIME = "via_time";
    private static final String PARAM_ZONE = "zone";
    private static final String PARAM_TRANSPORT_TYPES = "transport_types";


    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        RouteParams routeparams = new RouteParams();


        routeparams.setFrom(params.getRequiredParamDouble(PARAM_FROM_LON), params.getRequiredParamDouble(PARAM_FROM_LAT));
        routeparams.setTo(params.getRequiredParamDouble(PARAM_TO_LON), params.getRequiredParamDouble(PARAM_TO_LAT));

        // TODO: validate values
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH:mm");
        final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
        final String dateTime =
                params.getHttpParam(PARAM_DATE, dateFormatter.format(new Date())) +
                params.getHttpParam(PARAM_TIME, timeFormatter.format(new Date()));

        try {
            final Date date = sdf.parse(dateTime);
            routeparams.setDate(date);

        } catch (ParseException e) {
            throw new ActionParamsException("Couldn't parse date");
        }
        routeparams.setIsDepartureTime("departure".equals(params.getHttpParam(PARAM_TIMETYPE)));
        routeparams.setMinutesSpentInVia(params.getHttpParam(PARAM_VIA_TIME, 3));
        routeparams.setTicketZone(params.getHttpParam(PARAM_ZONE));
        routeparams.setTransportTypes(params.getHttpParam(PARAM_TRANSPORT_TYPES));
        routeparams.setSrs(params.getHttpParam(PARAM_SRS));
        routeparams.setLang(params.getHttpParam(PARAM_LANG));
        List<RouteResponse> result = service.getRoute(routeparams);
        JSONArray response = new JSONArray();

        for (RouteResponse routeresponse : result) {
            JSONObject routeResult = new JSONObject();
            JSONHelper.putValue(routeResult, "geoJson", routeresponse.getGeoJson());
            JSONHelper.putValue(routeResult, "instructions", routeresponse.getInstructions());
            response.put(routeResult);
        }

        ResponseHelper.writeResponse(params, response);

    }
}
