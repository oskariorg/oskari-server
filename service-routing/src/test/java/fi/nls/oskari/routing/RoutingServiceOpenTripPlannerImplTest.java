package fi.nls.oskari.routing;


import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.routing.pojo.Route;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class RoutingServiceOpenTripPlannerImplTest {
    private static final Logger LOGGER = LogFactory.getLogger(RoutingServiceOpenTripPlannerImplTest.class);
    private static final RoutingServiceOpenTripPlannerImpl ROUTING_SERVICE = new RoutingServiceOpenTripPlannerImpl();

    public static final String ROUTING_SRS = "EPSG:4326";
    private static final String MAP_SRS = "EPSG:3067";
    private static final String JSON_ENCODING = "UTF-8";

    @BeforeClass
    public static void initialize()throws Exception{
        PropertyUtil.addProperty("routing.srs", ROUTING_SRS);
    }

    @AfterClass
    public static void goAway() throws Exception{
        PropertyUtil.clearProperties();
    }

    @org.junit.Test
    public void testParseRouteThatIsOk() throws Exception {
        RouteParser parser = new RouteParser();
        ObjectMapper mapper = new ObjectMapper();

        RouteParams routeparams = new RouteParams();
        routeparams.setSrs(MAP_SRS);

        String routeJson = IOHelper.readString(getClass().getResourceAsStream("route2.json"), JSON_ENCODING);
        routeJson = routeJson.replaceAll("\\\\", "\\\\\\\\");


        Route route = mapper.readValue(routeJson, Route.class);

        RouteResponse response = new RouteResponse();

        if(!ROUTING_SERVICE.isErrorMessage(routeJson)){
            response.setRequestParameters(parser.generateRequestParameters(route, routeparams));
            response.setPlan(parser.generatePlan(route, routeparams));
            response.setSuccess(true);
        } else {
            response.setSuccess(false);
            try {
                JSONObject error = new JSONObject(routeJson);
                if(error.has(RoutingServiceOpenTripPlannerImpl.PARAM_ERROR_MESSAGE)) {
                    response.setErrorMessage(error.getString(RoutingServiceOpenTripPlannerImpl.PARAM_ERROR_MESSAGE));
                } else {
                    response.setErrorMessage("ERROR");
                }
            } catch (JSONException ex){
                LOGGER.warn("Cannot set error message to route response", ex);
            }
        }
        System.out.print(response.toJSON());

    }

    @org.junit.Test
    public void testParseRouteThatIsNok() throws Exception {

        RouteParser parser = new RouteParser();
        ObjectMapper mapper = new ObjectMapper();

        RouteParams routeparams = new RouteParams();
        routeparams.setSrs(MAP_SRS);

        String routeJson = IOHelper.readString(getClass().getResourceAsStream("route_error.json"), JSON_ENCODING);
        routeJson = routeJson.replaceAll("\\\\", "\\\\\\\\");


        Route route = mapper.readValue(routeJson, Route.class);

        RouteResponse response = new RouteResponse();

        if(!ROUTING_SERVICE.isErrorMessage(routeJson)){
            response.setRequestParameters(parser.generateRequestParameters(route, routeparams));
            response.setPlan(parser.generatePlan(route, routeparams));
            response.setSuccess(true);
        } else {
            response.setSuccess(false);
            try {
                JSONObject error = new JSONObject(routeJson);
                if(error.has(RoutingServiceOpenTripPlannerImpl.PARAM_ERROR_MESSAGE)) {
                    response.setErrorMessage(error.getString(RoutingServiceOpenTripPlannerImpl.PARAM_ERROR_MESSAGE));
                } else {
                    response.setErrorMessage("ERROR");
                }
            } catch (JSONException ex){
                LOGGER.warn("Cannot set error message to route response", ex);
            }
        }
        System.out.print(response.toJSON());

    }
}
