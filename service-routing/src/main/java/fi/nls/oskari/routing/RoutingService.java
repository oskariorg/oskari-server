package fi.nls.oskari.routing;

import java.util.List;

/**
 * Created by SMAKINEN on 26.6.2015.
 */
public interface RoutingService {

    List<RouteResponse> getRoute(RouteParams params);
}
