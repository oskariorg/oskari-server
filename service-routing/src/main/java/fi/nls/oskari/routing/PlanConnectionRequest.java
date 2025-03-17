package fi.nls.oskari.routing;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import org.apache.commons.text.StringSubstitutor;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static fi.nls.oskari.routing.PlanConnectionRequestHelper.getTransitModes;

public class PlanConnectionRequest {

    private static final Logger LOG = LogFactory.getLogger(PlanConnectionRequest.class);

    public String getQuery(RouteParams params) throws ServiceException{
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
                    routingErrors {
                        code
                        description
                    }
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

    private Map<String, String> getPlanconnectionReplacementMap(RouteParams params) throws ServiceException {
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
