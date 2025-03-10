package fi.nls.oskari.routing;

import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.routing.pojo.Agency;
import fi.nls.oskari.routing.pojo.Edge;
import fi.nls.oskari.routing.pojo.Estimated;
import fi.nls.oskari.routing.pojo.Leg;
import fi.nls.oskari.routing.pojo.LegGeometry;
import fi.nls.oskari.routing.pojo.Node;
import fi.nls.oskari.routing.pojo.Place;
import fi.nls.oskari.routing.pojo.PlanConnection;
import fi.nls.oskari.routing.pojo.Route;
import fi.nls.oskari.routing.pojo.ScheduledTime;
import fi.nls.oskari.routing.pojo.Step;
import fi.nls.oskari.routing.pojo.Stop;
import fi.nls.oskari.routing.pojo.Trip;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Initial parsing for WMTS capabilities in a way that admin-layerselector can interpret it.
 */
public class RouteParser {
    private static final Logger LOG = LogFactory.getLogger(RouteParser.class);

    private static final String PARAM_GEOJSON_FEATURES = "features";
    private static final String PARAM_GEOJSON_TYPE = "type";
    private static final String PARAM_GEOJSON_COORDINATES = "coordinates";
    private static final String PARAM_GEOJSON_GEOMETRY = "geometry";
    private static final String PARAM_GEOJSON_PROPERTIES = "properties";

    private static final String PARAM_LOCALE = "locale";
    private static final String PARAM_DATE = "date";
    private static final String PARAM_TIME = "time";
    private static final String PARAM_ARRIVE_BY = "arriveBy";
    private static final String PARAM_MAX_WALK_DISTANCE = "maxWalkDistance";
    private static final String PARAM_WHEELCHAIR = "wheelchair";
    private static final String PARAM_FROM_PLACE = "fromPlace";
    private static final String PARAM_TO_PLACE = "toPlace";
    private static final String PARAM_LON = "lon";
    private static final String PARAM_LAT = "lat";

    private static final String PARAM_FROM = "from";
    private static final String PARAM_FROM_NAME = "name";
    private static final String PARAM_FROM_LON = "lon";
    private static final String PARAM_FROM_LAT = "lat";
    private static final String PARAM_FROM_ORIG = "orig";
    private static final String PARAM_FROM_VERTEX_TYPE = "vertexType";

    private static final String PARAM_TO = "to";
    private static final String PARAM_TO_NAME = "name";
    private static final String PARAM_TO_LON = "lon";
    private static final String PARAM_TO_LAT = "lat";
    private static final String PARAM_TO_ORIG = "orig";
    private static final String PARAM_TO_VERTEX_TYPE = "vertexType";

    // itineraries
    private static final String PARAM_ITINERARIES = "itineraries";
    private static final String PARAM_ITINERARIES_DURATION = "duration";
    private static final String PARAM_ITINERARIES_START_TIME = "startTime";
    private static final String PARAM_ITINERARIES_END_TIME = "endTime";
    private static final String PARAM_ITINERARIES_WALK_TIME = "walkTime";
    private static final String PARAM_ITINERARIES_TRANSIT_TIME = "transitTime";
    private static final String PARAM_ITINERARIES_WAITING_TIME = "waitingTime";
    private static final String PARAM_ITINERARIES_WALK_DISTANCE = "walkDistance";
    private static final String PARAM_ITINERARIES_WALK_LIMIT_EXCEEDED = "walkLimitExceeded";
    private static final String PARAM_ITINERARIES_ELEVATION_LOST = "elevationLost";
    private static final String PARAM_ITINERARIES_ELEVATION_GAINED = "elevationGained";
    private static final String PARAM_ITINERARIES_TRANSFERS = "transfers";
    private static final String PARAM_ITINERARIES_TOO_SLOPED = "tooSloped";
    private static final String PARAM_ITINERARIES_LEGS = "legs";
    private static final String PARAM_ITINERARIES_GEOJSON = "geoJSON";

    // legs
    private static final String PARAM_LEGS_START_TIME = "startTime";
    private static final String PARAM_LEGS_END_TIME = "endTime";
    private static final String PARAM_LEGS_DEPARTURE_DELAY = "departureDelay";
    private static final String PARAM_LEGS_ARRIVAL_DELAY = "arrivalDelay";
    private static final String PARAM_LEGS_DISTANCE = "distance";
    private static final String PARAM_LEGS_PATHWAY = "pathway";
    private static final String PARAM_LEGS_MODE = "mode";
    private static final String PARAM_LEGS_ROUTE = "route";
    private static final String PARAM_LEGS_AGENCY_TIME_ZONE_OFFSET = "agencyTimeZoneOffset";
    private static final String PARAM_LEGS_INTERLINE_WIDTH_PREVIOUS_LEG = "interlineWithPreviousLeg";
    private static final String PARAM_LEGS_RENTED_BIKE = "rentedBike";
    private static final String PARAM_LEGS_TRANSIT_LEG = "transitLeg";
    private static final String PARAM_LEGS_DURATION = "duration";
    private static final String PARAM_LEGS_AGENCY_ID = "agencyId";
    private static final String PARAM_LEGS_AGENCY_NAME = "agencyName";
    private static final String PARAM_LEGS_AGENCY_URL = "agencyUrl";
    private static final String PARAM_LEGS_HEADSIGN = "headsign";
    private static final String PARAM_LEGS_REAL_TIME = "realTime";
    private static final String PARAM_LEGS_ROUTE_ID = "routeId";
    private static final String PARAM_LEGS_ROUTE_LONG_NAME = "routeLongName";
    private static final String PARAM_LEGS_ROUTE_SHORT_NAME = "routeShortName";
    private static final String PARAM_LEGS_ROUTE_TYPE = "routeType";
    private static final String PARAM_LEGS_SERVICE_DATE = "serviceDate";
    private static final String PARAM_LEGS_TRIP_ID = "tripId";
    private static final String PARAM_LEGS_FROM = "from";
    private static final String PARAM_LEGS_FROM_NAME = "name";
    private static final String PARAM_LEGS_FROM_STOP_ID = "stopId";
    private static final String PARAM_LEGS_FROM_STOP_CODE = "stopCode";
    private static final String PARAM_LEGS_FROM_LON = "lon";
    private static final String PARAM_LEGS_FROM_LAT = "lat";
    private static final String PARAM_LEGS_FROM_ARRIVAL = "arrival";
    private static final String PARAM_LEGS_FROM_DEPARTURE = "departure";
    private static final String PARAM_LEGS_FROM_ZONE_ID= "zoneId";
    private static final String PARAM_LEGS_FROM_STOP_INDEX = "stopIndex";
    private static final String PARAM_LEGS_FROM_STOP_SEQUENCE = "stopSequence";
    private static final String PARAM_LEGS_FROM_VERTEX_TYPE = "vertexType";
    private static final String PARAM_LEGS_TO = "to";
    private static final String PARAM_LEGS_TO_NAME = "name";
    private static final String PARAM_LEGS_TO_LON = "lon";
    private static final String PARAM_LEGS_TO_LAT = "lat";
    private static final String PARAM_LEGS_TO_ARRIVAL = "arrival";
    private static final String PARAM_LEGS_TO_ORIG = "orig";
    private static final String PARAM_LEGS_TO_VERTEX_TYPE = "vertexType";
    private static final String PARAM_LEGS_TO_ZONE_ID= "zoneId";
    private static final String PARAM_LEGS_TO_STOP_INDEX = "stopIndex";
    private static final String PARAM_LEGS_TO_STOP_SEQUENCE = "stopSequence";
    private static final String PARAM_LEGS_TO_STOP_ID = "stopId";
    private static final String PARAM_LEGS_TO_STOP_CODE = "stopCode";
    private static final String PARAM_LEGS_LEG_GEOMETRY = "legGeometry";
    private static final String PARAM_LEGS_LEG_GEOMETRY_POINTS = "points";
    private static final String PARAM_LEGS_LEG_GEOMETRY_LENGTH = "length";
    private static final String PARAM_LEGS_LEG_GEOJSON = "geoJSON";
    private static final String PARAM_LEGS_STEPS = "steps";
    private static final String PARAM_LEGS_STEPS_LON = "lon";
    private static final String PARAM_LEGS_STEPS_LAT = "lat";
    private static final String PARAM_LEG_STOPS = "intermediateStops";

    private static final String PARAM_LEG_STOP_NAME = "name";
    private static final String PARAM_LEG_STOP_STOPID = "stopId";
    private static final String PARAM_LEG_STOP_STOPCODE = "stopCode";
    private static final String PARAM_LEG_STOP_LON = "lon";
    private static final String PARAM_LEG_STOP_LAT = "lat";
    private static final String PARAM_LEG_STOP_ARRIVAL = "arrival";
    private static final String PARAM_LEG_STOP_DEPARTURE = "departure";
    private static final String PARAM_LEG_STOP_ZONEID = "zoneId";
    private static final String PARAM_LEG_STOP_STOPINDEX = "stopIndex";
    private static final String PARAM_LEG_STOP_STOPSEQUENCE = "stopSequence";
    private static final String PARAM_LEG_STOP_VERTEXTYPE = "vertexType";
    private static final String PARAM_LEG_STEP_AREA = "area";
    private static final String PARAM_LEG_STEP_ELEVATION = "elevation";
    private static final String PARAM_LEG_STEP_STREET_NAME = "streetName";
    private static final String PARAM_LEG_STEP_DISTANCE = "distance";

    private static final String PARAM_LEG_STEP_BOGUS_NAME = "bogusName";
    private static final String PARAM_LEG_STEP_STAY_ON = "stayOn";
    private static final String PARAM_LEG_STEP_ABSOLUTE_DIRECTION = "absoluteDirection";
    private static final String PARAM_LEG_STEP_RELATIVE_DIRECTION = "relativeDirection";


    /**
     * Generate route plan
     * @param planConnection
     * @param params
     * @return route plan
     */
    public JSONObject mapPlanConnectionToPlan(PlanConnection planConnection, RouteParams params){
        final JSONObject planJSON = new JSONObject();

        try{
            // date
            Long date = getEpochFromString(planConnection.getSearchDateTime());
            planJSON.put(PARAM_DATE, date);

            // from
            planJSON.put(PARAM_FROM, getFromJSON(planConnection, params));

            // to
            planJSON.put(PARAM_TO, getToJSON(planConnection, params));

            // itineraries
            planJSON.put(PARAM_ITINERARIES, getItinerariesJSON(planConnection, params));


        } catch(JSONException ex){
            LOG.error("Cannot generate routing plan", ex);
        }

        return planJSON;
    }

    /**
     * Get from JSON
     * @param planConnection
     * @param params
     * @return from JSON
     */
    private JSONObject getFromJSON(PlanConnection planConnection, RouteParams params){
        // first node -> first leg -> from
        Edge firstEdge = planConnection.getEdges().get(0);
        fi.nls.oskari.routing.pojo.Leg firstLeg = firstEdge.getNode().getLegs().get(0);
        final Place from = firstLeg.getFrom();
        final JSONObject fromJSON = new JSONObject();
        final String sourceSRS = PropertyUtil.get("routing.srs");
        final String targetSRS = params.getSrs();

        try {
            fromJSON.put(PARAM_FROM_NAME, from.getName());
            Point newFrom;

            newFrom = ProjectionHelper.transformPoint(from.getLon(), from.getLat(), sourceSRS, targetSRS);
            fromJSON.put(PARAM_FROM_LON, newFrom.getLon());
            fromJSON.put(PARAM_FROM_LAT, newFrom.getLat());

            fromJSON.put(PARAM_FROM_ORIG, from.getOrig());
            fromJSON.put(PARAM_FROM_VERTEX_TYPE, from.getVertexType());
        } catch (JSONException ex) {
            LOG.error("Cannot get route from JSON", ex);
        }
        return fromJSON;
    }


    /**
     * Get itineraries JSON
     * @param planConnection
     * @param params
     * @return itineraries JSON
     */
    private JSONArray getItinerariesJSON(PlanConnection planConnection, RouteParams params){
        final List<Edge> edges = planConnection.getEdges();
        final JSONArray itinerariesJSON = new JSONArray();

        try {
            for (Edge edge : edges) {
                Node node = edge.getNode();
                JSONObject itineraryJSON = new JSONObject();
                itineraryJSON.put(PARAM_ITINERARIES_DURATION, node.getDuration());
                itineraryJSON.put(PARAM_ITINERARIES_START_TIME, node.getStart());
                itineraryJSON.put(PARAM_ITINERARIES_END_TIME, node.getEnd());
                itineraryJSON.put(PARAM_ITINERARIES_WALK_TIME, node.getWalkTime());
                // TODO: wasn't found as a field in the new api. We need to calculate this I guess? duration - waitingtime - walkingtime?
                float transitTime = node.getDuration() - node.getWaitingTime() - node.getWalkTime();
                itineraryJSON.put(PARAM_ITINERARIES_TRANSIT_TIME, transitTime);
                itineraryJSON.put(PARAM_ITINERARIES_WAITING_TIME, node.getWaitingTime());
                itineraryJSON.put(PARAM_ITINERARIES_WALK_DISTANCE, node.getWalkDistance());
//                itineraryJSON.put(PARAM_ITINERARIES_WALK_LIMIT_EXCEEDED, node.getWalkLimitExceeded()); // TODO: not found in the new itinerary-type.
                itineraryJSON.put(PARAM_ITINERARIES_ELEVATION_LOST, node.getElevationLost());
                itineraryJSON.put(PARAM_ITINERARIES_ELEVATION_GAINED, node.getElevationGained());
                itineraryJSON.put(PARAM_ITINERARIES_TRANSFERS, node.getNumberOfTransfers());
//                itineraryJSON.put(PARAM_ITINERARIES_TOO_SLOPED, node.getTooSloped()); // TODO: not found in the new itinerary-type.

                itineraryJSON.put(PARAM_ITINERARIES_GEOJSON, getItinerariesGeoJSON(node, params));
                itineraryJSON.put(PARAM_ITINERARIES_LEGS, getLegsJSON(node, params));
                itinerariesJSON.put(itineraryJSON);
            }
        } catch (JSONException ex){
            LOG.error("Cannot get route itineraries JSON", ex);
        }
        return itinerariesJSON;
    }

    public JSONObject getItinerariesGeoJSON(Node node, RouteParams params) {
        final JSONObject featureCollection = new JSONObject();
        final List<Leg> legs = node.getLegs();
        final String targetSRS = params.getSrs();
        try {
            featureCollection.put(PARAM_GEOJSON_TYPE, "FeatureCollection");
            JSONArray featureList = new JSONArray();

            for (Leg leg: legs) {
                JSONObject feature = parseGeoJson(leg, targetSRS);
                featureList.put(feature);
            }
            featureCollection.put(PARAM_GEOJSON_FEATURES, featureList);

        } catch (JSONException e) {
            LOG.error("can't save json object: " + e.toString());
        }
        return featureCollection;
    }

    /**
     * Get to JSON
     * @param planConnection
     * @param params
     * @return to JSON
     */
    private JSONObject getToJSON(PlanConnection planConnection, RouteParams params){
        Edge lastEdge = planConnection.getEdges().get(planConnection.getEdges().size() - 1);
        fi.nls.oskari.routing.pojo.Leg lastLeg = lastEdge.getNode().getLegs().get(lastEdge.getNode().getLegs().size() - 1);
        final Place to = lastLeg.getTo();

        final JSONObject toJSON = new JSONObject();
        final String sourceSRS = PropertyUtil.get("routing.srs");
        final String targetSRS = params.getSrs();

        try {
            toJSON.put(PARAM_TO_NAME, to.getName());
            Point newTo;

            newTo = ProjectionHelper.transformPoint(to.getLon(), to.getLat(), sourceSRS, targetSRS);
            toJSON.put(PARAM_TO_LON, newTo.getLon());
            toJSON.put(PARAM_TO_LAT, newTo.getLat());

            toJSON.put(PARAM_TO_ORIG, to.getOrig());
            toJSON.put(PARAM_TO_VERTEX_TYPE, to.getVertexType());
        } catch (JSONException ex) {
            LOG.error("Cannot get route to JSON", ex);
        }
        return toJSON;
    }

    /**
     * Get legs JSON
     * @param node
     * @param params
     * @return legs JSON
     */
    public JSONArray getLegsJSON(Node node, RouteParams params) {
        final List<Leg> legs = node.getLegs();
        final JSONArray legsJSON = new JSONArray();
        final String sourceSRS = PropertyUtil.get("routing.srs");
        final String targetSRS = params.getSrs();
        try {
            for (Leg leg:legs) {
                JSONObject legJSON = new JSONObject();

                addAgencyFields(leg, legJSON);

                addLegStartFields(leg, legJSON);

                addLegEndFields(leg, legJSON);

                addLegRouteFields(leg, legJSON);

                legJSON.put(PARAM_LEGS_DISTANCE, leg.getDistance());

                legJSON.put(PARAM_LEGS_DURATION, leg.getDuration());
                legJSON.put(PARAM_LEGS_HEADSIGN, leg.getHeadsign());
                legJSON.put(PARAM_LEGS_INTERLINE_WIDTH_PREVIOUS_LEG, leg.isInterlineWithPreviousLeg());
                legJSON.put(PARAM_LEGS_MODE, leg.getMode());
                //legJSON.put(PARAM_LEGS_PATHWAY, leg.getPathway()); // TODO: can't find this in the new api. Or old.
                legJSON.put(PARAM_LEGS_REAL_TIME, leg.isRealTime());
                legJSON.put(PARAM_LEGS_RENTED_BIKE, leg.isRentedBike());

                legJSON.put(PARAM_LEGS_SERVICE_DATE, leg.getServiceDate());
                legJSON.put(PARAM_LEGS_TRANSIT_LEG, leg.isTransitLeg());

                legJSON.put(PARAM_LEGS_TRIP_ID, Optional.ofNullable(leg.getTrip()).map(Trip::getGtfsId).orElse(null));

                legJSON.put(PARAM_LEGS_FROM, getLegFromJSON(leg, sourceSRS, targetSRS));
                legJSON.put(PARAM_LEGS_TO, getLegToJSON(leg, sourceSRS, targetSRS));

                LegGeometry geometry = leg.getLegGeometry();
                JSONObject geometryJSON = new JSONObject();
                geometryJSON.put(PARAM_LEGS_LEG_GEOJSON, parseGeoJson(leg, targetSRS));
                geometryJSON.put(PARAM_LEGS_LEG_GEOMETRY_LENGTH, geometry.getLength());
                geometryJSON.put(PARAM_LEGS_LEG_GEOMETRY_POINTS, geometry.getPoints());
                legJSON.put(PARAM_LEGS_LEG_GEOMETRY, geometryJSON);

                legJSON.put(PARAM_LEGS_STEPS, getLegStepsJSON(leg, sourceSRS, targetSRS));

                legJSON.put(PARAM_LEG_STOPS, getLegStopsJSON(leg, sourceSRS, targetSRS));

                legsJSON.put(legJSON);
            }
        } catch (JSONException ex){
            LOG.error("Cannot get itineraries legs JSON", ex);
        }

        return legsJSON;
    }

    private void addLegRouteFields(Leg leg, JSONObject legJSON) {
        try {
            Route route = leg.getRoute();
            String gtfsId = Optional.ofNullable(route).map(Route::getGtfsId).orElse(null);
            String longName = Optional.ofNullable(route).map(Route::getLongName).orElse(null);
            String shortName = Optional.ofNullable(route).map(Route::getShortName).orElse(null);
            Integer type = Optional.ofNullable(route).map(Route::getType).orElse(null);

            legJSON.put(PARAM_LEGS_ROUTE, shortName); // TODO: there is no property "route:String" anymore... this seems to map to route -> short name but is it always the same?
            legJSON.put(PARAM_LEGS_ROUTE_ID, gtfsId);
            legJSON.put(PARAM_LEGS_ROUTE_LONG_NAME, longName);
            legJSON.put(PARAM_LEGS_ROUTE_SHORT_NAME, shortName);
            legJSON.put(PARAM_LEGS_ROUTE_TYPE, type);
        } catch(JSONException ex) {
            LOG.error("Failed to add route fields for leg ", ex);
        }
    }

    private void addAgencyFields(Leg leg, JSONObject legJSON) {
        try {
            Agency legAgency = leg.getAgency();
            String gtfsId = Optional.ofNullable(legAgency).map(Agency::getGtfsId).orElse(null);
            String name = Optional.ofNullable(legAgency).map(Agency::getName).orElse(null);
            Long timezoneOffset = Optional.ofNullable(legAgency).map(Agency::getTimeZoneOffset).orElse(null);
            String url = Optional.ofNullable(legAgency).map(Agency::getUrl).orElse(null);
            legJSON.put(PARAM_LEGS_AGENCY_ID, gtfsId);
            legJSON.put(PARAM_LEGS_AGENCY_NAME, name);
            legJSON.put(PARAM_LEGS_AGENCY_TIME_ZONE_OFFSET, timezoneOffset);
            legJSON.put(PARAM_LEGS_AGENCY_URL, url);
        } catch(JSONException ex) {
            LOG.error("Failed to add agency fields for leg ", ex);
        }
    }
    private void addLegStartFields(Leg leg, JSONObject legJSON) {
        try {
            ScheduledTime start = leg.getStart();

            Long delay = Optional.ofNullable(start).map(ScheduledTime::getEstimated).map(Estimated::getDelayMilliseconds).orElse(null);
            String scheduledTime = Optional.ofNullable(start).map(ScheduledTime::getScheduledTime).orElse(null);
            legJSON.put(PARAM_LEGS_DEPARTURE_DELAY, delay);
            legJSON.put(PARAM_LEGS_START_TIME, scheduledTime);
        } catch(JSONException ex) {
            LOG.error("Failed to add start fields for leg ", ex);
        }
    }

    private void addLegEndFields(Leg leg, JSONObject legJSON) {
        try {
            ScheduledTime end = leg.getEnd();
            Long delay = Optional.ofNullable(end).map(ScheduledTime::getEstimated).map(Estimated::getDelayMilliseconds).orElse(null);
            String scheduledTime = Optional.ofNullable(end).map(ScheduledTime::getScheduledTime).orElse(null);
            legJSON.put(PARAM_LEGS_ARRIVAL_DELAY, delay);
            legJSON.put(PARAM_LEGS_END_TIME, scheduledTime);
        } catch(JSONException ex) {
            LOG.error("Failed to add end fields for leg ", ex);
        }
    }

    private JSONArray getLegStopsJSON(Leg leg, String sourceSRS, String targetSRS) {
        // Intermediate stops
        List<Place> places = leg.getIntermediatePlaces();
        JSONArray stopsJSON = new JSONArray();
        if (places == null) {
            return stopsJSON;
        }
        try {
            for (Place intermediatePlace : places) {
                JSONObject stopJSON = new JSONObject();
                // convert coordinates
                if (intermediatePlace.getLat() != null && intermediatePlace.getLon() != null) {
                    Point stopPoint;
                    stopPoint = ProjectionHelper.transformPoint(intermediatePlace.getLon(), intermediatePlace.getLat(), sourceSRS, targetSRS);
                    stopJSON.put(PARAM_LEG_STOP_LON, stopPoint.getLon());
                    stopJSON.put(PARAM_LEG_STOP_LAT, stopPoint.getLat());
                }

                String stopGtsfId = Optional.ofNullable(intermediatePlace.getStop()).map(Stop::getGtfsId).orElse(null);
                String stopCode = Optional.ofNullable(intermediatePlace.getStop()).map(Stop::getCode).orElse(null);
                String stopZoneId = Optional.ofNullable(intermediatePlace.getStop()).map(Stop::getZoneId).orElse(null);

                stopJSON.put(PARAM_LEG_STOP_STOPID, stopGtsfId);
                stopJSON.put(PARAM_LEG_STOP_STOPCODE, stopCode);
                stopJSON.put(PARAM_LEG_STOP_ZONEID, stopZoneId);

                stopJSON.put(PARAM_LEG_STOP_NAME, intermediatePlace.getName());
                stopJSON.put(PARAM_LEG_STOP_ARRIVAL, intermediatePlace.getArrival());
                stopJSON.put(PARAM_LEG_STOP_DEPARTURE, intermediatePlace.getDeparture());
                // stopJSON.put(PARAM_LEG_STOP_STOPINDEX, intermediatePlace.getStopIndex()); // TODO: not found in new api
                // stopJSON.put(PARAM_LEG_STOP_STOPSEQUENCE, intermediatePlace.getStopSequence());//TODO: not found in new api
                stopJSON.put(PARAM_LEG_STOP_VERTEXTYPE, intermediatePlace.getVertexType());
                stopsJSON.put(stopJSON);
            }
        } catch(Exception e) {
            LOG.error("Cannot get intermediate places JSON for leg", e);
        }

        return stopsJSON;

    }

    private JSONArray getLegStepsJSON(Leg leg, String sourceSRS, String targetSRS) {
        List<Step> steps = leg.getSteps();
        JSONArray stepsJSON = new JSONArray();

        try {
            for (int i = 0; i < steps.size(); i++) {
                Step step = steps.get(i);
                JSONObject stepJSON = new JSONObject();
                stepJSON.put(PARAM_LEG_STEP_AREA, step.isArea());
                stepJSON.put(PARAM_LEG_STEP_ELEVATION, step.getElevationProfile());
                stepJSON.put(PARAM_LEG_STEP_STREET_NAME, step.getStreetName());
                stepJSON.put(PARAM_LEG_STEP_DISTANCE, step.getDistance());
                stepJSON.put(PARAM_LEG_STEP_BOGUS_NAME, step.getBogusName());
                stepJSON.put(PARAM_LEG_STEP_STAY_ON, step.isStayOn());
                stepJSON.put(PARAM_LEG_STEP_ABSOLUTE_DIRECTION, step.getAbsoluteDirection());
                stepJSON.put(PARAM_LEG_STEP_RELATIVE_DIRECTION, step.getAbsoluteDirection());

                if (step.getLat() != null && step.getLon() != null) {
                    String stepLon = step.getLon().toString();
                    String stepLat = step.getLat().toString();
                    Point stepPoint = ProjectionHelper.transformPoint(stepLon, stepLat, sourceSRS, targetSRS);
                    stepJSON.put(PARAM_LEGS_STEPS_LON, stepPoint.getLon());
                    stepJSON.put(PARAM_LEGS_STEPS_LAT, stepPoint.getLat());
                }
                stepsJSON.put(stepJSON);
            }
        } catch(Exception e) {
            LOG.error("Cannot get steps for leg", e);
        }

        return stepsJSON;
    }

    private JSONObject getLegToJSON(Leg leg, String sourceSRS, String targetSRS) {
        Place to = leg.getTo();
        JSONObject toJSON = new JSONObject();
        try {
            toJSON.put(PARAM_LEGS_TO_ARRIVAL, getEpochFromString(to.getArrival().getScheduledTime()));

            Point newTo = ProjectionHelper.transformPoint(to.getLon(), to.getLat(), sourceSRS, targetSRS);
            toJSON.put(PARAM_LEGS_TO_LON, newTo.getLon());
            toJSON.put(PARAM_LEGS_TO_LAT, newTo.getLat());

            toJSON.put(PARAM_LEGS_TO_NAME, to.getName());
            toJSON.put(PARAM_LEGS_TO_ORIG, to.getOrig());

            String stopGtsfId = Optional.ofNullable(to.getStop()).map(Stop::getGtfsId).orElse(null);
            String stopCode = Optional.ofNullable(to.getStop()).map(Stop::getCode).orElse(null);
            String stopZoneId = Optional.ofNullable(to.getStop()).map(Stop::getZoneId).orElse(null);
            toJSON.put(PARAM_LEGS_TO_STOP_ID, stopGtsfId);
            toJSON.put(PARAM_LEGS_TO_STOP_CODE, stopCode);
            toJSON.put(PARAM_LEGS_TO_ZONE_ID, stopZoneId);

            // toJSON.put(PARAM_LEGS_TO_STOP_INDEX, to.getStopIndex()); // TODO: not found in new api
            // toJSON.put(PARAM_LEGS_TO_STOP_SEQUENCE, to.getStopSequence()); // TODO: not found in new api
            toJSON.put(PARAM_LEGS_TO_VERTEX_TYPE, to.getVertexType());

        } catch (JSONException e) {
            LOG.error("Cannot parse To-property of leg", e);
        }

        return toJSON;
    }

    private JSONObject getLegFromJSON(Leg leg, String sourceSRS, String targetSRS) {
        Place from = leg.getFrom();
        JSONObject fromJSON = new JSONObject();
        try {
            fromJSON.put(PARAM_LEGS_FROM_ARRIVAL, getEpochFromString(from.getArrival().getScheduledTime()));
            fromJSON.put(PARAM_LEGS_FROM_DEPARTURE, getEpochFromString(from.getDeparture().getScheduledTime()));
            Point newFrom;

            newFrom = ProjectionHelper.transformPoint(from.getLon(), from.getLat(), sourceSRS, targetSRS);
            fromJSON.put(PARAM_LEGS_FROM_LON, newFrom.getLon());
            fromJSON.put(PARAM_LEGS_FROM_LAT, newFrom.getLat());

            fromJSON.put(PARAM_LEGS_FROM_NAME, from.getName());

            String stopGtsfId = Optional.ofNullable(from.getStop()).map(Stop::getGtfsId).orElse(null);
            String stopCode = Optional.ofNullable(from.getStop()).map(Stop::getCode).orElse(null);
            String stopZoneId = Optional.ofNullable(from.getStop()).map(Stop::getZoneId).orElse(null);
            fromJSON.put(PARAM_LEGS_FROM_STOP_ID, stopGtsfId);
            fromJSON.put(PARAM_LEGS_FROM_STOP_CODE, stopCode);
            fromJSON.put(PARAM_LEGS_FROM_ZONE_ID, stopZoneId);

            // fromJSON.put(PARAM_LEGS_FROM_STOP_INDEX, from.getStopIndex()); // TODO: no match in new api?.
            // fromJSON.put(PARAM_LEGS_FROM_STOP_SEQUENCE, from.getStopSequence()); // TODO: no match in new api?.
            fromJSON.put(PARAM_LEGS_FROM_VERTEX_TYPE, from.getVertexType());

        } catch(JSONException e) {
            LOG.error("Cannot parse From-property of leg", e);
        }
        return fromJSON;

    }

    private Long getEpochFromString(String date) {
        OffsetDateTime odt = OffsetDateTime.parse(date);
        return odt.toEpochSecond();
    }

    /**
     * Generate request parameters
     * @param params
     * @return request parameters
     */
    public JSONObject generateRequestParameters(RouteParams params){
        final JSONObject requestParameters = new JSONObject();
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        try{
            requestParameters.put(PARAM_DATE, params.getDate().format(dateFormatter));
            requestParameters.put(PARAM_WHEELCHAIR, params.getIsWheelChair());
            requestParameters.put(PARAM_ARRIVE_BY, params.getIsArriveBy());
            requestParameters.put(PARAM_TIME, params.getDate().format(timeFormatter));
            requestParameters.put(PARAM_LOCALE, params.getLang());
            requestParameters.put(PARAM_FROM_PLACE, getPointJSON(params.getFrom().getX(), params.getFrom().getY()));
            requestParameters.put(PARAM_TO_PLACE, getPointJSON(params.getTo().getX(), params.getTo().getY()));
        } catch (JSONException ex) {
            LOG.error("Cannot generate routing request parameters", ex);
        }

        return requestParameters;
    }

    /**
     * Get point JSON
     * @param x
     * @param y
     * @return
     */
    private JSONObject getPointJSON(final Double x, final Double y){
        JSONObject pointJSON = new JSONObject();

        try {
            pointJSON.put(PARAM_LON, x);
            pointJSON.put(PARAM_LAT, y);
        } catch (JSONException e) {
            LOG.error("can't save json object: " + e.toString());
        }

        return pointJSON;
    }

    /**
     * Parse geoJSON
     * @param leg
     * @param targetSRS
     * @return leg geoJSON
     */
    public JSONObject parseGeoJson(Leg leg, String targetSRS) {
        JSONObject feature = new JSONObject();

        try {

            JSONObject line = new JSONObject();
            line.put(PARAM_GEOJSON_TYPE, "LineString");

            LegGeometry legGeom = leg.getLegGeometry();
            String encodedPolyLine = legGeom.getPoints();
            JSONArray coordinates = decode(encodedPolyLine, targetSRS);
            line.put(PARAM_GEOJSON_COORDINATES, coordinates);
            feature.put(PARAM_GEOJSON_TYPE, "Feature");
            feature.put(PARAM_GEOJSON_GEOMETRY, line);

            JSONObject properties = new JSONObject();
            properties.put(PARAM_LEGS_MODE, leg.getMode());
            properties.put(PARAM_LEGS_DISTANCE, leg.getDistance());
            properties.put(PARAM_LEGS_START_TIME, leg.getStart().getScheduledTime());
            properties.put(PARAM_LEGS_END_TIME, leg.getEnd().getScheduledTime());
            feature.put(PARAM_GEOJSON_PROPERTIES, properties);
        } catch (JSONException e) {
            LOG.error(e + "can't save json object: " + e.getMessage());
        }

        LOG.debug(feature.toString());

        return feature;
    }



    /**
     * Decode Google encoded polyline to points
     * @param pointString
     * @param targetSRS
     * @return JSONArray of points
     */
    private static JSONArray decode(String pointString, String targetSRS) {
        double lat = 0;
        double lon = 0;

        int strIndex = 0;
        JSONArray coordinates = new JSONArray();
        try {
            final String currentSRS = PropertyUtil.get("routing.srs");

            while (strIndex < pointString.length()) {
                int[] rLat = decodeSignedNumberWithIndex(pointString, strIndex);
                lat = lat + rLat[0] * 1e-5;
                strIndex = rLat[1];

                int[] rLon = decodeSignedNumberWithIndex(pointString, strIndex);
                lon = lon + rLon[0] * 1e-5;
                strIndex = rLon[1];

                Point coordsInAppSRS;
                JSONArray coordinate;
                coordsInAppSRS = ProjectionHelper.transformPoint(lon, lat, currentSRS, targetSRS);
                coordinate = new JSONArray("[" + coordsInAppSRS.getLonToString() + "," + coordsInAppSRS.getLatToString() + "]");

                coordinates.put(coordinate);
            }
        } catch (JSONException e){
            LOG.error(e + "can't get points: " + e.getMessage());
        }

        return coordinates;
    }

    /**
     * Decode signed number with index
     * @param value
     * @param index
     * @return
     */
    private static int[] decodeSignedNumberWithIndex(String value, int index) {
        int[] r = decodeNumberWithIndex(value, index);
        int sgn_num = r[0];
        if ((sgn_num & 0x01) > 0) {
            sgn_num = ~(sgn_num);
        }
        r[0] = sgn_num >> 1;
        return r;
    }

    /**
     * Decode number with index
     * @param value
     * @param index
     * @return
     */
    private static int[] decodeNumberWithIndex(String value, int index) {

        if (value.length() == 0)
            throw new IllegalArgumentException("string is empty");

        int num = 0;
        int v = 0;
        int shift = 0;

        do {
            v = value.charAt(index++) - 63;
            num |= (v & 0x1f) << shift;
            shift += 5;
        } while (v >= 0x20);

        return new int[] { num, index };
    }
}