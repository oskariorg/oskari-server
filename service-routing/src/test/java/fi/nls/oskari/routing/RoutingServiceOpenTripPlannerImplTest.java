package fi.nls.oskari.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RoutingServiceOpenTripPlannerImplTest {

    @org.junit.Test
    public void testParseRoute() throws Exception {

        RouteParser parser = new RouteParser();
        ObjectMapper mapper = new ObjectMapper();

        PropertyUtil.addProperty("routing.srs", "EPSG:2392");

        RouteParams routeparams = new RouteParams();

        routeparams.setSrs("EPSG:3067");

        final String routeJson = IOHelper.readString(getClass().getResourceAsStream("route2.json"));
        List<List<Route>> routeList = mapper.readValue(routeJson, mapper.getTypeFactory().constructCollectionType(List.class, mapper.getTypeFactory().constructCollectionType(List.class, Route.class)));

        List<RouteResponse> result = new ArrayList<>();
        for (Route route : routeList.get(0)) {
            RouteResponse routeresponse = new RouteResponse();
            final JSONObject responseGeoJson = parser.parseGeoJson(route, routeparams.getSrs());
            routeresponse.setGeoJson(responseGeoJson);

            try {
                final JSONObject responseRoute = parser.parseRoute(route);
                routeresponse.setInstructions(responseRoute);
                result.add(routeresponse);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
        JSONArray response = new JSONArray();

        for (RouteResponse routeresponse : result) {
            JSONObject routeResult = new JSONObject();
            JSONHelper.putValue(routeResult, "geoJson", routeresponse.getGeoJson());
            JSONHelper.putValue(routeResult, "instructions", routeresponse.getInstructions());
            response.put(routeResult);
        }
        System.out.print(result);

    }
}
