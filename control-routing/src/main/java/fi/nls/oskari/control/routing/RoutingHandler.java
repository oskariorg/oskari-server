package fi.nls.oskari.control.routing;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.routing.Route;
import fi.nls.oskari.routing.RoutingService;
import fi.nls.oskari.routing.RoutingServiceOpenTripPlannerImpl;
import fi.nls.oskari.util.ResponseHelper;

/**
 * Created by SMAKINEN on 26.6.2015.
 */
public class RoutingHandler extends ActionHandler {

    private RoutingService service = new RoutingServiceOpenTripPlannerImpl();

    private static final String PARAM_LAT = "lat";
    private static final String PARAM_LON = "lon";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        Route route = service.getRouteBetween(
                params.getRequiredParamDouble(PARAM_LON),
                params.getRequiredParamDouble(PARAM_LAT));
        // TODO: serialize route to response
        ResponseHelper.writeResponse(params, route);
    }
}
