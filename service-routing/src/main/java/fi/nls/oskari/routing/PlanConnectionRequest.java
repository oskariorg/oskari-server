package fi.nls.oskari.routing;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlanConnectionRequest {

    String[] AVAILABLE_TRANSFER_MODES = {"WALK", "BICYCLE", "CAR"};
    String[] AVAILABLE_TRANSIT_MODES = {"AIRPLANE", "BUS", "CABLE_CAR", "CARPOOL", "COACH", "FERRY", "FUNICULAR", "GONDOLA", "MONORAIL", "RAIL", "SUBWAY", "TAXI", "TRAM", "TROLLEYBUS"};

    String ALL_PUBLIC_TRANSPORTATIONS_MODE = "TRANSIT";

    String JSON_CONTENT_MODES = "modes";

    String JSON_CONTENT_TRANSIT = "transit";
    String JSON_CONTENT_TRANSFER = "transfer";

    public String getQuery(RouteParams params) {
        String modes = params.getMode();
        String transitModes = getTransitModes(modes);
        if (transitModes != null) {
            transitModes = transitModes + ", ";
        }
        if (transitModes == null) {
            transitModes = "";
        }

        String dateTimeJSON = getDateTimeJSON(params.getIsArriveBy(), params.getDate());

        String query = "  {\n" +
        "    planConnection(\n" +
                transitModes + "\n" +
                dateTimeJSON + "\n" +
        "       locale: \"" + params.getLang()+ "\"";

        if (params.getIsWheelChair()) {
            query += "preferences: { "+
                "   accessibility: {"+
                "       wheelChair: { " +
                "           enabled: true "+
                "       }"+
                "  }" +
                "}";
        }
        query +=
        "   origin: {\n" +
        "      location: {\n" +
        "          coordinate: {\n" +
        "          longitude: " + params.getFrom().getX()+",\n" +
        "            latitude: " + params.getFrom().getY()+"\n" +
        "          }\n" +
        "        }\n" +
        "      },\n" +
        "      destination:\n" +
        "      {\n" +
        "        location: {\n" +
        "          coordinate: {\n" +
        "            longitude: " + params.getTo().getX() + ",\n" +
        "            latitude: " + params.getTo().getY() + "\n" +
        "          }\n" +
        "        }\n" +
        "      }) {\n" +
        "      searchDateTime\n" +
        "      edges {\n" +
        "        node {\n" +
                   getNodeStaticResultFields() +
        "          legs {\n" +
                     getLegStaticResultFields() +
        "            trip \n{"+
        "              gtfsId\n\n"+
        "            }\n"+
        "            route {\n"+
        "               gtfsId\n\n"+
        "               longName\n"+
        "               shortName\n"+
        "               type\n"+
        "            }\n"+
        "            from {\n" +
                        getPlaceStaticResultFields() +
        "           }\n" +
        "           to {\n" +
                        getPlaceStaticResultFields() +
        "           }\n" +
        "           legGeometry {\n" +
        "             length\n" +
        "             points\n" +
        "           }\n" +
        "           start {\n" +
                        getScheduledTimeStaticResultFields() +
        "           }\n" +
        "           end {\n" +
                        getScheduledTimeStaticResultFields() +
        "           }\n" +
        "           agency {\n" +
        "             gtfsId\n\n" +
        "             name\n" +
        "             timezone\n" +
        "           }\n" +
        "           steps {\n" +
        "             area\n" +
        "             elevationProfile {\n" +
        "               distance\n" +
        "               elevation\n" +
        "             }\n" +
        "             streetName\n" +
        "             distance\n" +
        "             bogusName\n" +
        "             stayOn\n" +
        "             lon\n" +
        "             lat\n" +
        "             absoluteDirection\n" +
        "             relativeDirection\n" +
        "          }\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}\n";

        try {
            JSONObject json = new JSONObject();
            json.put("query", query);
            return json.toString();

        } catch (JSONException e) {
            return null;
        }
    }

    private String getLegStaticResultFields() {
        return
        "mode\n" +
        "distance\n" +
        "duration\n" +
        "interlineWithPreviousLeg\n" +
        "realTime\n" +
        "rentedBike\n" +
        "serviceDate\n" +
        "transitLeg\n";

    }

    private String getNodeStaticResultFields() {
        return "elevationLost\n" +
        "elevationGained\n" +
        "waitingTime\n" +
        "walkTime\n" +
        "walkDistance\n" +
        "duration\n" +
        "numberOfTransfers\n" +
        "start\n" +
        "end\n";
    }

    private String getScheduledTimeStaticResultFields() {
        return "" +
        "scheduledTime\n" +
        "estimated {\n" +
        "   delay\n" +
        "}\n";
    }

    private String getTransitModes(String paramModes) {
        if (paramModes == null) {
            return null;
        }

        String[] modesArray = paramModes.split(",");

        List<String> transits = new ArrayList();
        List<String> transfers = new ArrayList();

        String transitsString = null;
        String transfersString = null;

        // generic "TRANSIT" not found -> check if we find any listed public transportations here - BUS, RAIL etc.
        // Adding NO excplicit transit mode(s) means anything goes.
        if (Arrays.stream(modesArray).noneMatch(ALL_PUBLIC_TRANSPORTATIONS_MODE::equals)) {
            for (String transitMode : modesArray) {
                if (Arrays.stream(AVAILABLE_TRANSIT_MODES).anyMatch(transitMode::equals)) {
                    transits.add(transitMode);
                }
            }
        }

        for (String transferMode : modesArray) {
            if (Arrays.stream(AVAILABLE_TRANSFER_MODES).anyMatch(transferMode::equals)) {
                transfers.add(transferMode);
            }
        }

        if (transits.size() > 0) {
            transitsString = JSON_CONTENT_TRANSIT + ": [" + String.join(", ", transits) + "]";
        }
        if (transfers.size() > 0) {
            transfersString = JSON_CONTENT_TRANSFER + ": [" + String.join(", ", transfers) + "]";
        }

        if (transitsString != null || transfersString != null) {
            String returnValue = "modes: { transit: {";
            if (transitsString != null) {
                returnValue += transitsString;
            }
            if (transfersString != null) {
                returnValue += transfersString;
            }
            returnValue += "}}";
            return returnValue;
        }

        return null;
    }

    private String getDateTimeJSON(Boolean isArriveBy, OffsetDateTime date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmXXX");
        String dateTimeString = "dateTime: {";
        if (isArriveBy) {
            dateTimeString += "latestArrival: \"" + date.format(formatter) + "\"";
        } else {
            dateTimeString += "earliestDeparture: \"" + date.format(formatter) + "\"";
        }
        dateTimeString += "}";
        return dateTimeString;
    }

    private String getPlaceStaticResultFields() {
        return
        "name\n" +
        "lon\n" +
        "lat\n" +
        "vertexType\n" +
        "arrival {\n" +
        "   estimated {\n" +
        "       delay\n" +
        "   }\n" +
        "   scheduledTime\n" +
        "}\n" +
        "departure {\n" +
        "    estimated {\n" +
        "        delay\n" +
        "    }\n" +
        "   scheduledTime\n" +
        "}\n" +
        "stop {\n" +
        "    gtfsId\n\n" +
        "    code\n" +
        "    zoneId\n" +
        "}\n";
    }
}
