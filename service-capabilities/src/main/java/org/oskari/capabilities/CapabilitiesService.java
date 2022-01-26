package org.oskari.capabilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.ServiceRuntimeException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.capabilities.ogc.OGCCapabilitiesParser;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class CapabilitiesService {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Map<String, LayerCapabilities> getLayersFromService(ServiceConnectInfo connectInfo) throws IOException, ServiceException {
        String layerType = connectInfo.getType();
        OGCCapabilitiesParser parser = getParser(layerType);
        if (parser == null) {
            throw new ServiceException("Unrecognized type: " + layerType);
        }
        return parser.getLayersFromService(connectInfo);
    }

    public static JSONObject toJSON(LayerCapabilities caps, Set<String> systemCRSs) {
        try {
            String raw = MAPPER.writeValueAsString(caps);
            JSONObject json = new JSONObject(raw);

            json.put("srs", new JSONArray(filterSRS(caps.getSrs(), systemCRSs)));
            return json;
        } catch (Exception e) {
            throw new ServiceRuntimeException("Error serializing capabilities as JSON", e);
        }
    }

    private static OGCCapabilitiesParser getParser(String layerType) {
        return (OGCCapabilitiesParser) OskariComponentManager
                .getComponentsOfType(OGCCapabilitiesParser.class)
                .get(layerType);
    }

    // filter out projections that are not used by the instance
    private static Set<String> filterSRS(Set<String> available, Set<String> systemCRSs) {
        if (available == null ||systemCRSs == null) {
            return Collections.emptySet();
        }
        return available.stream()
                .map(srs -> shortSyntaxEpsg(srs))
                .filter(srs -> srs != null)
                .filter(srs -> systemCRSs.contains(srs))
                .collect(Collectors.toSet());
    }

    /**
     * Return epsg short
     * urn:ogc:def:crs:EPSG::32635  --> EPSG:32635
     * @param crs
     * @return  epsg in short syntax
     */
    private static String shortSyntaxEpsg(String crs) {
        if (crs == null) {
            return null;
        }
        // try own parsing for something like
        //  "urn:ogc:def:crs:EPSG:6.3:3067"
        //  "urn:ogc:def:crs:EPSG:6.18:3:3857"
        //  "urn:ogc:def:crs:EPSG::3575"
        String[] epsg = crs.toUpperCase().split("EPSG");
        if (epsg.length > 1) {
            String[] code = epsg[1].split(":");
            if (code.length > 2) {
                // get the last token
                return "EPSG:" + code[code.length - 1];
            }
        }
        return crs;
    }
}
