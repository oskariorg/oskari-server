package fi.nls.oskari.routing;

import fi.nls.oskari.service.ServiceException;
import org.apache.commons.text.StringSubstitutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanConnectionRequestHelper {
    // Can't have more than one transfer mode at one request.
    public static final String[] AVAILABLE_TRANSFER_MODES = {"WALK", "BICYCLE", "CAR"};
    public static final String[] AVAILABLE_TRANSIT_MODES = {"AIRPLANE", "BUS", "CABLE_CAR", "CARPOOL", "COACH", "FERRY", "FUNICULAR", "GONDOLA", "MONORAIL", "RAIL", "SUBWAY", "TAXI", "TRAM", "TROLLEYBUS"};

    public static final String ALL_PUBLIC_TRANSPORTATIONS_MODE = "TRANSIT";

    public static final String JSON_CONTENT_MODES = "modes";

    public static final String JSON_CONTENT_TRANSIT = "transit";
    public static final String JSON_CONTENT_TRANSFER = "transfer";
    public static final String JSON_CONTENT_DIRECT = "direct";
    public static final String JSON_CONTENT_EGRESS = "egress";
    public static final String JSON_CONTENT_ACCESS = "access";

    public static String getTransitModes(String paramModes) throws ServiceException {
        if (paramModes == null) {
            return null;
        }

        String[] modesArray = paramModes.split(",");

        List<String> transits = new ArrayList();
        List<String> transfers = new ArrayList();

        String transitsString = "";
        String transfersString = "";
        String directString = "";
        boolean directOnly = false;

        if (Arrays.stream(modesArray).anyMatch(ALL_PUBLIC_TRANSPORTATIONS_MODE::equals)) {
            // TRANSIT - found -> add all.
            Arrays.stream(AVAILABLE_TRANSIT_MODES).forEach((transitMode) -> {
                transits.add("{ mode: " + transitMode + "}");
            });
        } else {
            // Check transit modes one by one
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

        // If we have JUST CAR or JUST BICYCLE we should set the direct-mode too and probably also set "directOnly" to true
        if (transits.size() == 0 && transfers.size() == 1) {
            directString = JSON_CONTENT_DIRECT + ": [" + transfers.get(0)+ "]";
            directOnly = true;
        }

        if (transfers.size() > 1) {
            throw new ServiceException("Can't have more then one transfer mode selected. You had " + String.join(", ", transfers));
        }

        // If transfermode is set it needs to be exactly one (CAR, BICYCLE OR WALK). And if set to CAR OR BICYCLE egress and access need to be set as well.
        // So we're setting egress and access for any mode.
        if (transfers.size() == 1) {
            transfersString = JSON_CONTENT_TRANSFER + ": [" +  transfers.get(0) + "]";
            transfersString += JSON_CONTENT_EGRESS + ": [" + transfers.get(0)  + "]";
            transfersString += JSON_CONTENT_ACCESS + ": [" + transfers.get(0)  + "]";
        }

        if (!transitsString.isEmpty() || !transfersString.isEmpty() || !directString.isEmpty()) {
            String returnValue = """
                modes: {
                    directOnly: ${directOnly}
                    ${directString}
                    transit: {
                        ${transitsString}
                        ${transfersString}
                    }
                }
            """;

            Map<String, String> replacements = new HashMap<>();
            replacements.put("directOnly", String.valueOf(directOnly));
            replacements.put("directString", directString);
            replacements.put("transitsString", transitsString);
            replacements.put("transfersString", transfersString);

            StringSubstitutor substitutor = new StringSubstitutor(replacements);
            String replaced = substitutor.replace(returnValue);
            return replaced;
        }

        return null;
    }

}
