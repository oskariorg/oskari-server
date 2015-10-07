package fi.nls.oskari.routing;

import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Initial parsing for WMTS capabilities in a way that admin-layerselector can interpret it.
 */
public class RouteParser {
    private static final Logger LOG = LogFactory.getLogger(RouteParser.class);

    private static final String PARAM_TYPE = "type";
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
    private static final String PARAM_LEGS_LEG_GEOMETRY = "legGeometry";
    private static final String PARAM_LEGS_LEG_GEOMETRY_POINTS = "points";
    private static final String PARAM_LEGS_LEG_GEOMETRY_LENGTH = "length";
    private static final String PARAM_LEGS_ALERTS = "alerts";
    private static final String PARAM_LEGS_ALERTS_ALERT_HEADER_TEXT = "alertHeaderText";
    private static final String PARAM_LEGS_STEPS = "steps";
    private static final String PARAM_LEGS_STEPS_DISTANCE = "distance";
    private static final String PARAM_LEGS_STEPS_RELATIVE_DIRECTION = "relativeDirection";
    private static final String PARAM_LEGS_STEPS_STREET_NAME = "streetName";
    private static final String PARAM_LEGS_STEPS_ABSOLUTE_DIRECTION = "absoluteDirection";
    private static final String PARAM_LEGS_STEPS_STAY_ON = "stayOn";
    private static final String PARAM_LEGS_STEPS_AREA = "area";
    private static final String PARAM_LEGS_STEPS_BOGUS_NAME = "bogusName";
    private static final String PARAM_LEGS_STEPS_LON = "lon";
    private static final String PARAM_LEGS_STEPS_LAT = "lat";
    private static final String PARAM_LEGS_STEPS_ELEVATION = "elevation";
    private static final String PARAM_LEGS_STEPS_ALERTS = "alerts";
    private static final String PARAM_LEGS_STEPS_ALERT_HEADER_TEXT = "alertHeaderText";


    public JSONObject generatePlan(Route route, RouteParams params){
        final JSONObject planJSON = new JSONObject();
        final Plan plan = route.getPlan();

        try{
            // date
            planJSON.put(PARAM_DATE, plan.getDate());

            // from
            planJSON.put(PARAM_FROM, getFromJSON(plan, params));

            // to
            planJSON.put(PARAM_TO, getToJSON(plan,params));

            // itineraries
            planJSON.put(PARAM_ITINERARIES, getItinerariesJSON(plan, params));


        } catch(JSONException ex){
            LOG.error("Cannot generate routing plan", ex);
        }

        return planJSON;
    }

    private JSONObject getFromJSON(Plan plan, RouteParams params){
        final From from = plan.getFrom();
        final JSONObject fromJSON = new JSONObject();
        final String sourceSRS = PropertyUtil.get("routing.srs");
        final String targetSRS = params.getSrs();

        try {
            fromJSON.put(PARAM_FROM_NAME, from.getName());
            final Point newFrom = ProjectionHelper.transformPoint(from.getLon(), from.getLat(), sourceSRS, targetSRS);
            fromJSON.put(PARAM_FROM_LON, newFrom.getLon());
            fromJSON.put(PARAM_FROM_LAT, newFrom.getLat());
            fromJSON.put(PARAM_FROM_ORIG, from.getOrig());
            fromJSON.put(PARAM_FROM_VERTEX_TYPE, from.getVertexType());
        } catch (JSONException ex){
            LOG.error("Cannot get route from JSON", ex);
        }
        return fromJSON;
    }

    private JSONObject getToJSON(Plan plan, RouteParams params){
        final To to = plan.getTo();
        final JSONObject toJSON = new JSONObject();
        final String sourceSRS = PropertyUtil.get("routing.srs");
        final String targetSRS = params.getSrs();

        try {
            toJSON.put(PARAM_TO_NAME, to.getName());
            final Point newTo = ProjectionHelper.transformPoint(to.getLon(), to.getLat(), sourceSRS, targetSRS);
            toJSON.put(PARAM_TO_LON, newTo.getLon());
            toJSON.put(PARAM_TO_LAT, newTo.getLat());
            toJSON.put(PARAM_TO_ORIG, to.getOrig());
            toJSON.put(PARAM_TO_VERTEX_TYPE, to.getVertexType());
        } catch (JSONException ex){
            LOG.error("Cannot get route to JSON", ex);
        }
        return toJSON;
    }

    private JSONArray getItinerariesJSON(Plan plan, RouteParams params){
        final List<Itinerary> itineraries = plan.getItineraries();
        final JSONArray itinerariesJSON = new JSONArray();

        try {
            for (Itinerary itinerary:itineraries) {
                JSONObject itineraryJSON = new JSONObject();
                itineraryJSON.put(PARAM_ITINERARIES_DURATION, itinerary.getDuration());
                itineraryJSON.put(PARAM_ITINERARIES_START_TIME, itinerary.getStartTime());
                itineraryJSON.put(PARAM_ITINERARIES_END_TIME, itinerary.getEndTime());
                itineraryJSON.put(PARAM_ITINERARIES_WALK_TIME, itinerary.getWalkTime());
                itineraryJSON.put(PARAM_ITINERARIES_TRANSIT_TIME, itinerary.getTransitTime());
                itineraryJSON.put(PARAM_ITINERARIES_WAITING_TIME, itinerary.getWaitingTime());
                itineraryJSON.put(PARAM_ITINERARIES_WALK_DISTANCE, itinerary.getWalkDistance());
                itineraryJSON.put(PARAM_ITINERARIES_WALK_LIMIT_EXCEEDED, itinerary.getWalkLimitExceeded());
                itineraryJSON.put(PARAM_ITINERARIES_ELEVATION_LOST, itinerary.getElevationLost());
                itineraryJSON.put(PARAM_ITINERARIES_ELEVATION_GAINED, itinerary.getElevationGained());
                itineraryJSON.put(PARAM_ITINERARIES_TRANSFERS, itinerary.getTransfers());
                itineraryJSON.put(PARAM_ITINERARIES_TOO_SLOPED, itinerary.getTooSloped());
                itineraryJSON.put(PARAM_ITINERARIES_LEGS, getLegsJSON(itinerary, params));
                itinerariesJSON.put(itineraryJSON);
            }
        } catch (JSONException ex){
            LOG.error("Cannot get route itineraries JSON", ex);
        }
        return itinerariesJSON;
    }

    public JSONArray getLegsJSON(Itinerary itinerary, RouteParams params) {
        final List<Leg> legs = itinerary.getLegs();
        final JSONArray legsJSON = new JSONArray();
        final String sourceSRS = PropertyUtil.get("routing.srs");
        final String targetSRS = params.getSrs();
        try {
            for (Leg leg:legs) {
                JSONObject legJSON = new JSONObject();

                legJSON.put(PARAM_LEGS_AGENCY_ID, leg.getAgencyId());
                legJSON.put(PARAM_LEGS_AGENCY_NAME, leg.getAgencyName());
                legJSON.put(PARAM_LEGS_AGENCY_TIME_ZONE_OFFSET, leg.getAgencyTimeZoneOffset());
                legJSON.put(PARAM_LEGS_AGENCY_URL, leg.getAgencyUrl());
                legJSON.put(PARAM_LEGS_ARRIVAL_DELAY, leg.getArrivalDelay());
                legJSON.put(PARAM_LEGS_DEPARTURE_DELAY, leg.getDepartureDelay());
                legJSON.put(PARAM_LEGS_DISTANCE, leg.getDistance());
                legJSON.put(PARAM_LEGS_DURATION, leg.getDuration());
                legJSON.put(PARAM_LEGS_END_TIME, leg.getEndTime());
                legJSON.put(PARAM_LEGS_HEADSIGN, leg.getHeadsign());
                legJSON.put(PARAM_LEGS_INTERLINE_WIDTH_PREVIOUS_LEG, leg.getInterlineWithPreviousLeg());
                legJSON.put(PARAM_LEGS_MODE, leg.getMode());
                legJSON.put(PARAM_LEGS_PATHWAY, leg.getPathway());
                legJSON.put(PARAM_LEGS_REAL_TIME, leg.getRealTime());
                legJSON.put(PARAM_LEGS_RENTED_BIKE, leg.getRentedBike());
                legJSON.put(PARAM_LEGS_ROUTE, leg.getRoute());
                legJSON.put(PARAM_LEGS_ROUTE_ID, leg.getRouteId());
                legJSON.put(PARAM_LEGS_ROUTE_LONG_NAME, leg.getRouteLongName());
                legJSON.put(PARAM_LEGS_ROUTE_SHORT_NAME, leg.getRouteShortName());
                legJSON.put(PARAM_LEGS_ROUTE_TYPE, leg.getRouteType());
                legJSON.put(PARAM_LEGS_SERVICE_DATE, leg.getServiceDate());
                legJSON.put(PARAM_LEGS_START_TIME, leg.getStartTime());
                legJSON.put(PARAM_LEGS_TRANSIT_LEG, leg.getTransitLeg());
                legJSON.put(PARAM_LEGS_TRIP_ID, leg.getTripId());

                From_ from = leg.getFrom();
                JSONObject fromJSON = new JSONObject();
                fromJSON.put(PARAM_LEGS_FROM_ARRIVAL, from.getArrival());
                fromJSON.put(PARAM_LEGS_FROM_DEPARTURE, from.getDeparture());
                // FIXME lon lat change SRS to map projection
                fromJSON.put(PARAM_LEGS_FROM_LAT, from.getLat());
                fromJSON.put(PARAM_LEGS_FROM_LON, from.getLon());
                fromJSON.put(PARAM_LEGS_FROM_NAME, from.getName());
                fromJSON.put(PARAM_LEGS_FROM_STOP_CODE, from.getStopCode());
                fromJSON.put(PARAM_LEGS_FROM_STOP_ID, from.getStopId());
                fromJSON.put(PARAM_LEGS_FROM_STOP_INDEX, from.getStopIndex());
                fromJSON.put(PARAM_LEGS_FROM_STOP_SEQUENCE, from.getStopSequence());
                fromJSON.put(PARAM_LEGS_FROM_VERTEX_TYPE, from.getVertexType());
                fromJSON.put(PARAM_LEGS_FROM_ZONE_ID, from.getZoneId());
                legJSON.put(PARAM_LEGS_FROM, fromJSON);

                // FIXME check these
                legJSON.put(PARAM_LEGS_MODE, leg.getLegGeometry());
                legJSON.put(PARAM_LEGS_MODE, leg.getSteps());
                legJSON.put(PARAM_LEGS_MODE, leg.getTo());
                legsJSON.put(legJSON);
            }
        } catch (JSONException ex){
            LOG.error("Cannot get itineraries legs JSON", ex);
        }

        return legsJSON;
    }


    public JSONObject generateRequestParameters(Route route, RouteParams params){
        final JSONObject requestParameters = new JSONObject();
        final RequestParameters rp = route.getRequestParameters();
        final String sourceSRS = PropertyUtil.get("routing.srs");
        final String targetSRS = params.getSrs();

        try{
            requestParameters.put(PARAM_DATE, rp.getDate());
            requestParameters.put(PARAM_WHEELCHAIR, rp.getWheelchair());
            requestParameters.put(PARAM_ARRIVE_BY, rp.getArriveBy());
            requestParameters.put(PARAM_MAX_WALK_DISTANCE, rp.getMaxWalkDistance());
            requestParameters.put(PARAM_TIME, rp.getTime());
            requestParameters.put(PARAM_LOCALE, rp.getLocale());

            final String[] fromPoints = rp.getFromPlace().split(",");
            final String[] toPoints = rp.getToPlace().split(",");

            final Point newFrom = ProjectionHelper.transformPoint(fromPoints[0], fromPoints[1], sourceSRS, targetSRS);
            final Point newTo = ProjectionHelper.transformPoint(toPoints[0], toPoints[1], sourceSRS, targetSRS);

            requestParameters.put(PARAM_FROM_PLACE, getPointJSON(newFrom.getLon(), newFrom.getLat()));
            requestParameters.put(PARAM_TO_PLACE, getPointJSON(newTo.getLon(), newTo.getLat()));
        } catch(JSONException ex){
            LOG.error("Cannot generate routing request parameters", ex);
        }

        return requestParameters;
    }

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

    public JSONObject parseGeoJson(Itinerary params, String targetSRS) {

        LOG.debug("----------------------Trying to parse geoJson");

        JSONObject featureCollection = new JSONObject();

        List<Leg> legs = params.getLegs();

        try {
            featureCollection.put("type", "FeatureCollection");
            JSONArray featureList = new JSONArray();

            for (Leg leg: legs) {
                JSONObject line = new JSONObject();
                line.put(PARAM_TYPE, "LineString");

                LegGeometry legGeom = leg.getLegGeometry();
                String encodedPolyLine = legGeom.getPoints();
                JSONArray coordinates = decode(encodedPolyLine, targetSRS);

                line.put("coordinates", coordinates);
                JSONObject feature = new JSONObject();
                feature.put(PARAM_TYPE, "Feature");
                feature.put("geometry", line);

                JSONObject properties = new JSONObject();
                properties.put("transportType", leg.getMode());
                feature.put("properties", properties);

                featureList.put(feature);
            }
            featureCollection.put("features", featureList);

        } catch (JSONException e) {
            LOG.error("can't save json object: " + e.toString());
        }

        return featureCollection;
    }

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

                Point coordsInAppSRS = ProjectionHelper.transformPoint(lat, lon, currentSRS, targetSRS);
                JSONArray coordinate = new JSONArray("[" + coordsInAppSRS.getLonToString() + "," + coordsInAppSRS.getLatToString() + "]");
                coordinates.put(coordinate);
            }
        } catch (JSONException e){
            LOG.error("can't get points: " + e.toString());
        }

        return coordinates;
    }

    private static int[] decodeSignedNumberWithIndex(String value, int index) {
        int[] r = decodeNumberWithIndex(value, index);
        int sgn_num = r[0];
        if ((sgn_num & 0x01) > 0) {
            sgn_num = ~(sgn_num);
        }
        r[0] = sgn_num >> 1;
        return r;
    }
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


    public JSONObject parseRoute (Route params) {

        JSONObject instructions = new JSONObject();
        /*
        JSONArray legList = new JSONArray();

        try {
            Double routeLength = params.getLength();
            Number routeDuration = params.getDuration();

            instructions.put("length", routeLength);
            instructions.put("duration", routeDuration);

            List<Leg> legs = params.getLegs();

            for (Leg leg : legs) {
                JSONObject routeLeg = new JSONObject();

                Integer legLength = leg.getLength();
                routeLeg.put("length", legLength);

                Number legDuration = leg.getDuration();
                routeLeg.put("duration", legDuration);

                // transport type can be "walk" or number
                String legTransportType = leg.getType();
                routeLeg.put("type", legTransportType);

                String lineCode = leg.getCode();
                if (lineCode != null) {
                    routeLeg.put("code", lineCode);
                }
                legList.put(routeLeg);
            }
            instructions.put("legs", legList);
        } catch (JSONException e) {
            LOG.error("couldn,t parse instructions" + e.toString());
        }
*/
        return instructions;
    }



}
