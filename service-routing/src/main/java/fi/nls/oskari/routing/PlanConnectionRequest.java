package fi.nls.oskari.routing;

import org.json.JSONException;
import org.json.JSONObject;

public class PlanConnectionRequest {
String planConnectionQuery = "  {\n" +
        "    planConnection(\n" +
        "      modes: {\n" +
        "        directOnly: false,\n" +
        "        transit: {\n" +
        "          transfer: WALK\n" +
        "          transit: {\n" +
        "            mode:TRAM\n" +
        "          }\n" +
        "        }\n" +
        "      }\n" +
        "      origin:\n" +
        "      {\n" +
        "        location: {\n" +
        "          coordinate: {\n" +
        "            longitude: 24.929199444,\n" +
        "            latitude: 60.168486667\n" +
        "          }\n" +
        "        }\n" +
        "      },\n" +
        "      destination:\n" +
        "      {\n" +
        "        location: {\n" +
        "          coordinate: {\n" +
        "            longitude: 24.940325556,\n" +
        "            latitude: 60.170438611\n" +
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

    public String getQuery(RouteParams params) {
        String query = "  {\n" +
        "    planConnection(\n" +
        "      modes: {\n" +
        "        directOnly: false,\n" +
        "        transit: {\n" +
        "          transfer: WALK\n" +
        "          transit: {\n" +
        "            mode:TRAM\n" +
        "          }\n" +
        "        }\n" +
        "      }\n" +
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
}
