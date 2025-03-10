package fi.nls.oskari.routing;

import org.apache.commons.text.StringSubstitutor;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanConnectionRequest {

    String[] AVAILABLE_TRANSFER_MODES = {"WALK", "BICYCLE", "CAR"};
    String[] AVAILABLE_TRANSIT_MODES = {"AIRPLANE", "BUS", "CABLE_CAR", "CARPOOL", "COACH", "FERRY", "FUNICULAR", "GONDOLA", "MONORAIL", "RAIL", "SUBWAY", "TAXI", "TRAM", "TROLLEYBUS"};

    String ALL_PUBLIC_TRANSPORTATIONS_MODE = "TRANSIT";

    String JSON_CONTENT_MODES = "modes";

    String JSON_CONTENT_TRANSIT = "transit";
    String JSON_CONTENT_TRANSFER = "transfer";

    public String getQuery(RouteParams params) {
        Map<String, String> planConnectionReplamentMap = getPlanconnectionReplacementMap(params);
        StringSubstitutor substitutor = new StringSubstitutor(planConnectionReplamentMap);

        String planConnectionQuery = """
            {
                planConnection(
                    ${transitModes}
                    ${dateTimeJSON}
                    locale: \"${lang}\"
            """;

        if (params.getIsWheelChair()) {
            planConnectionQuery += """
                preferences: {
                    accessibility: {
                        wheelchair: {
                            enabled: true
                        }
                    }
                }
            """;
        }

        planConnectionQuery += """
                origin: {
                    location: {
                        coordinate: {
                            longitude: ${fromLon}
                            latitude: ${fromLat}
                        }
                    }
                },
                destination: {
                    location: {
                        coordinate: {
                            longitude: ${toLon}
                            latitude: ${toLat}
                        }
                    }
                }) {
                    searchDateTime
                    edges {
                        node {
                            ${nodeStaticFields}
                            legs {
                                ${legStaticResultFields}
                                trip {
                                    gtfsId
                                }
                                route {
                                    gtfsId
                                    longName
                                    shortName
                                    type
                                }
                                from {
                                    ${fromPlaceStaticFields}
                                }
                                to {
                                    ${toPlaceStaticFields}
                                }
                                legGeometry {
                                    length
                                    points
                                }
                                start {
                                    ${startTime}
                                }
                                end {
                                    ${endTime}
                                }
                                agency {
                                    gtfsId
                                    name
                                    timezone
                                }
                                steps {
                                    area
                                    elevationProfile {
                                        distance
                                        elevation
                                    }
                                    streetName
                                    distance
                                    bogusName
                                    stayOn
                                    lon
                                    lat
                                    absoluteDirection
                                    relativeDirection
                                }
                            }
                        }
                    }
                }
            }
            """;
        String query = substitutor.replace(planConnectionQuery);
        try {
            JSONObject json = new JSONObject();
            json.put("query", query);
            return json.toString();

        } catch (JSONException e) {
            return null;
        }
    }

    private Map<String, String> getPlanconnectionReplacementMap(RouteParams params) {
        String modes = params.getMode();
        String transitModes = getTransitModes(modes);
        if (transitModes != null) {
            transitModes = transitModes + ", ";
        }
        if (transitModes == null) {
            transitModes = "";
        }

        String dateTimeJSON = getDateTimeJSON(params.getIsArriveBy(), params.getDate());

        Map<String, String> planConnectionReplamentMap = new HashMap<>();
        planConnectionReplamentMap.put("transitModes", transitModes);
        planConnectionReplamentMap.put("dateTimeJSON", dateTimeJSON);
        planConnectionReplamentMap.put("lang", params.getLang());
        planConnectionReplamentMap.put("fromLon", String.valueOf(params.getFrom().getX()));
        planConnectionReplamentMap.put("fromLat", String.valueOf(params.getFrom().getY()));
        planConnectionReplamentMap.put("toLon", String.valueOf(params.getTo().getX()));
        planConnectionReplamentMap.put("toLat", String.valueOf(params.getTo().getY()));
        planConnectionReplamentMap.put("nodeStaticFields", getNodeStaticResultFields());
        planConnectionReplamentMap.put("legStaticResultFields", getLegStaticResultFields());
        planConnectionReplamentMap.put("fromPlaceStaticFields", getPlaceStaticResultFields());
        planConnectionReplamentMap.put("toPlaceStaticFields", getPlaceStaticResultFields());
        planConnectionReplamentMap.put("startTime", getScheduledTimeStaticResultFields());
        planConnectionReplamentMap.put("endTime", getScheduledTimeStaticResultFields());

        return planConnectionReplamentMap;
    }

    private String getLegStaticResultFields() {
        return """
            mode
            distance
            duration
            interlineWithPreviousLeg
            realTime
            rentedBike
            serviceDate
            transitLeg
        """;

    }

    private String getNodeStaticResultFields() {
        return """
            elevationLost
            elevationGained
            waitingTime
            walkTime
            walkDistance
            duration
            numberOfTransfers
            start
            end
        """;
            
    }

    private String getScheduledTimeStaticResultFields() {
        return """
            scheduledTime
            estimated {
                delay
            }
        """;
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
                    transits.add("{ mode: " + transitMode + "}");
                }
            }
        }

        for (String transferMode : modesArray) {
            if (Arrays.stream(AVAILABLE_TRANSFER_MODES).anyMatch(transferMode::equals)) {
                transfers.add(transferMode);
            }
        }

        if (transits.size() > 0) {
            transitsString = JSON_CONTENT_TRANSIT + ": [" + String.join(", ", transits) + "]\n";
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
        return """
            name
            lon
            lat
            vertexType
            arrival {
               estimated {
                   delay
               }
               scheduledTime
            }
            departure {
                estimated {
                    delay
                }
               scheduledTime
            }
            stop {
                gtfsId
                code
                zoneId
            }
        """;
    }
}
