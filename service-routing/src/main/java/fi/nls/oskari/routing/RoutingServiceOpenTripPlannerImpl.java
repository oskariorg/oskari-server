package fi.nls.oskari.routing;

import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
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

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<RouteResponse> getRoute(RouteParams params) {

        RouteParser parser = new RouteParser();

        Map<String, String> requestParams = new HashMap<String, String>();

        requestParams.put("user", PropertyUtil.get("routing.user"));
        requestParams.put("pass", PropertyUtil.get("routing.password"));
        requestParams.put("request", "route");
        requestParams.put("detail", "full");

        //Transform coordinates for the route service
        String targetSRS = PropertyUtil.get("routing.srs");
        String sourceSRS = params.getSrs();
        Point newFrom = ProjectionHelper.transformPoint(params.getFrom().getX(), params.getFrom().getY(), sourceSRS, targetSRS);
        Point newTo = ProjectionHelper.transformPoint(params.getTo().getX(), params.getTo().getY(), sourceSRS, targetSRS);

        final String from = newFrom.getLatToString() + "," + newFrom.getLonToString();
        requestParams.put("from", from);

        final String to = newTo.getLatToString() + "," + newTo.getLonToString();
        requestParams.put("to", to);


        if (params.getDate() != null) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat timeFormatter = new SimpleDateFormat("HHmm");
            final String date = dateFormatter.format(params.getDate());
            requestParams.put("date", date);
            final String time = timeFormatter.format(params.getDate());
            requestParams.put("time", time);
            // if don't have time, we can't have timetype
            // departure is default
            if (!params.getIsDepartureTime()) {
                requestParams.put("timetype", "arrival");
            }
        }

        if (params.getVia() != null) {
            final String via = params.getVia().getX() + "," + params.getVia().getY();
            requestParams.put("via", via);
            final String viatime = "" + params.getMinutesSpentInVia();
            requestParams.put("via_time", viatime);
        }

        if (params.getTicketZone() !=null) {
            requestParams.put("zone", params.getTicketZone());
        }

        if (params.getTransportTypes() != null) {
            requestParams.put("transport_types", params.getTransportTypes());
        }


        final String requestUrl = IOHelper.constructUrl(PropertyUtil.get("routing.url"), requestParams);

        List<RouteResponse> result = new ArrayList<>();

        try {
            final String routeJson = IOHelper.getURL(requestUrl);

            List<List<Route>> routeList = mapper.readValue(routeJson, mapper.getTypeFactory().constructCollectionType(List.class, mapper.getTypeFactory().constructCollectionType(List.class, Route.class)));

            //TODO routeList includes three optional routes --> add them all to result
            for (Route route : routeList.get(0)) {
                RouteResponse routeresponse = new RouteResponse();
                final JSONObject responseGeoJson = parser.parseGeoJson(route, params.getSrs());
                routeresponse.setGeoJson(responseGeoJson);

                try {
                    final JSONObject responseRoute = parser.parseRoute(route);
                    routeresponse.setInstructions(responseRoute);
                    result.add(routeresponse);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}
