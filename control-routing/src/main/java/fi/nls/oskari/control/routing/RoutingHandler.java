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
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
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


    private static final String PARAM_LANG = "lang";
    private static final String PARAM_FROM_LAT = "fromlat";
    private static final String PARAM_FROM_LON = "fromlon";
    private static final String PARAM_TO_LAT = "tolat";
    private static final String PARAM_TO_LON = "tolon";
    private static final String PARAM_SRS = "srs";
    private static final String PARAM_DATE = "date";
    private static final String PARAM_TIME = "time";
    private static final String PARAM_ARRIVEBY = "arriveby";
    private static final String PARAM_MODE = "mode";
    private static final String PARAM_MAX_WALK_DISTANCE = "maxwalkdistance";
    private static final String PARAM_WHEELCHAIR = "wheelchair";

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
        routeparams.setIsArriveBy("true".equals(params.getHttpParam(PARAM_ARRIVEBY)));
        routeparams.setIsWheelChair("true".equals(params.getHttpParam(PARAM_WHEELCHAIR)));

        routeparams.setSrs(params.getHttpParam(PARAM_SRS));
        routeparams.setLang(params.getHttpParam(PARAM_LANG));
        routeparams.setMaxWalkDistance(ConversionHelper.getLong(params.getHttpParam(PARAM_MAX_WALK_DISTANCE, PropertyUtil.get("routing.default.maxwalkdistance")), 1000));
        routeparams.setMode(params.getHttpParam(PARAM_MODE, PropertyUtil.get("routing.default.mode")));

        RouteResponse result = service.getRoute(routeparams);

        ResponseHelper.writeResponse(params, result.toJSON());

    }
}
