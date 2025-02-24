package fi.nls.oskari.routing;

import org.json.JSONException;
import org.json.JSONObject;

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
/*
        routeparams.setIsArriveBy("true".equals(params.getHttpParam(PARAM_ARRIVEBY)));
        routeparams.setIsWheelChair("true".equals(params.getHttpParam(PARAM_WHEELCHAIR)));
        routeparams.setIsShowIntermediateStops("true".equals(params.getHttpParam(PARAM_SHOW_INTERMEDIATE_STOPS)));

        routeparams.setSrs(params.getHttpParam(PARAM_SRS));
        routeparams.setLang(params.getHttpParam(PARAM_LANGUAGE));
        routeparams.setMaxWalkDistance(ConversionHelper.getLong(params.getHttpParam(PARAM_MAX_WALK_DISTANCE, PropertyUtil.get("routing.default.maxwalkdistance")), 1000000));
*/

        String modes = params.getMode();
        // TODO: remake the whole jsonpart to get that from a jsonobject and stringify the rest
        String transitModes = getTransitModes(modes);
        if (transitModes != null) {
            transitModes = transitModes + ", ";
        }

        String query = "  {\n" +
        "    planConnection(\n" +
                transitModes +
        "      origin:\n" +
        "      {\n" +
        "        location: {\n" +
        "          coordinate: {\n" +
        "            longitude: " + params.getFrom().getX()+",\n" +
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
        "          elevationLost\n" +
        "          elevationGained\n" +
        "          waitingTime\n" +
        "          walkTime\n" +
        "          walkDistance\n" +
        "          duration\n" +
        "          numberOfTransfers\n" +
        "          start\n" +
        "          end\n" +
        "          legs {\n" +
        "            legGeometry {\n" +
        "              length\n" +
        "              points\n" +
        "            }\n" +
        "            mode\n" +
        "            distance\n" +
        "            start {\n" +
        "              scheduledTime\n" +
        "            }\n" +
        "            end {\n" +
        "              scheduledTime\n" +
        "            }\n" +
        "            steps {\n" +
        "              area\n" +
        "              elevationProfile {\n" +
        "                distance\n" +
        "                elevation\n" +
        "              }\n" +
        "              streetName\n" +
        "              distance\n" +
        "              bogusName\n" +
        "              stayOn\n" +
        "              lon\n" +
        "              lat\n" +
        "              absoluteDirection\n" +
        "              relativeDirection\n" +
        "            }\n" +
        "          }\n" +
        "      \t}\n" +
        "    \t}\n" +
        "  \t}\n" +
        "  }\n";


        try {
            JSONObject json = new JSONObject();
            json.put("query", query);
            return json.toString();

        } catch (JSONException e) {
            return null;
        }
    }

    private String getTransitModes(String paramModes) {
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
}
