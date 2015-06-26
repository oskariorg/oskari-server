package fi.nls.oskari.routing;

/**
 * Created by SMAKINEN on 26.6.2015.
 */
public interface RoutingService {

    Route getRouteBetween(double lon, double lat);
}
