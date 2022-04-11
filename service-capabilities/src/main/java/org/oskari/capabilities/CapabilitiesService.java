package org.oskari.capabilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.ServiceRuntimeException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class CapabilitiesService {
    private static final Logger LOG = LogFactory.getLogger(CapabilitiesService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Map<String, LayerCapabilities> getLayersFromService(ServiceConnectInfo connectInfo) throws IOException, ServiceException {
        String layerType = connectInfo.getType();
        CapabilitiesParser parser = getParser(layerType);
        if (parser == null) {
            throw new ServiceException("Unrecognized type: " + layerType);
        }
        return parser.getLayersFromService(connectInfo);
    }

    public static CapabilitiesUpdateResult updateCapabilities(OskariLayer layer, Set<String> systemCRSs) {
        List<OskariLayer> layers = new ArrayList<>(1);
        layers.add(layer);
        List<CapabilitiesUpdateResult> results = updateCapabilities(layers, systemCRSs);
        return results.get(0);
    }

    public static List<CapabilitiesUpdateResult> updateCapabilities(List<OskariLayer> layers, Set<String> systemCRSs) {
        List<CapabilitiesUpdateResult> results = new ArrayList<>(layers.size());

        Map<ServiceConnectInfo, List<OskariLayer>> layersByUTV = layers.stream()
                .filter(layer -> {
                    boolean hasParser = getParser(layer.getType()) != null;
                    if (!hasParser) {
                        results.add(CapabilitiesUpdateResult.err(layer, CapabilitiesUpdateResult.ERR_LAYER_TYPE_UNSUPPORTED + "/" + layer.getType()));
                    }
                    return hasParser;
                })
                .collect(groupingBy(layer -> ServiceConnectInfo.fromLayer(layer)));

        for (ServiceConnectInfo utv : layersByUTV.keySet()) {
            List<OskariLayer> layersFromOneService = layersByUTV.get(utv);
            Map<String, LayerCapabilities> serviceCaps;
            try {
                serviceCaps = getLayersFromService(utv);
            } catch (IOException | ServiceException e) {
                layersFromOneService.stream().forEach(layer -> {
                    if (e instanceof IOException) {
                        results.add(CapabilitiesUpdateResult.err(layer, CapabilitiesUpdateResult.ERR_FAILED_TO_FETCH_CAPABILITIES + "/" + utv.getUrl()));
                    } else {
                        results.add(CapabilitiesUpdateResult.err(layer, CapabilitiesUpdateResult.ERR_FAILED_TO_PARSE_CAPABILITIES + "/" + e.getMessage()));
                    }
                });
                continue;
            }

            layersFromOneService.stream().forEach(layer -> {
                LayerCapabilities capsForSingleLayer = serviceCaps.get(layer.getName());
                if (capsForSingleLayer == null) {
                    LOG.warn("Error finding layer with name:", layer.getName(), "from Capabilities for service, url:", utv.getUrl(),
                            "type:", utv.getType(), "version:", utv.getVersion());
                    results.add(CapabilitiesUpdateResult.err(layer, CapabilitiesUpdateResult.ERR_LAYER_NOT_FOUND_IN_CAPABILITIES+ "/" + layer.getName() + " from " + utv.getUrl()));
                    return;
                }
                layer.setCapabilities(toJSON(capsForSingleLayer, systemCRSs));
                results.add(CapabilitiesUpdateResult.ok(layer));
            });
        }
        return results;
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

    private static CapabilitiesParser getParser(String layerType) {
        return (CapabilitiesParser) OskariComponentManager
                .getComponentsOfType(CapabilitiesParser.class)
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
     * Helper for getting short EPSG-code from possible crs-uri or from longer format
     * urn:ogc:def:crs:EPSG::32635  --> EPSG:32635
     *
     * Note! Base code copy/pasted from ProjectionHelper.shortSyntaxEpsg() for making most services work without GeoTools as dependency
     *
     * @param crs
     * @return  epsg in short syntax
     */
    public static String shortSyntaxEpsg(String crs) {
        if (crs == null) {
            return null;
        }
        if (crs.startsWith("http")) {
            // assume it's the last bit in urls
            // http://www.opengis.net/def/crs/EPSG/0/3067
            int lastPartStartsAt = crs.lastIndexOf('/') + 1;
            if (lastPartStartsAt == 0) {
                // nope, something is not right -> return as is
                return crs;
            }
            return "EPSG:" + crs.substring(lastPartStartsAt);
        }
        // try parsing for something like
        //  "urn:ogc:def:crs:EPSG:6.3:3067"
        //  "urn:ogc:def:crs:EPSG:6.18:3:3857"
        //  "urn:ogc:def:crs:EPSG::3575"
        //  "urn:x-ogc:def:crs:EPSG:3067"
        String[] epsg = crs.toUpperCase().split("EPSG");
        if (epsg.length > 1) {
            String[] code = epsg[1].split(":");
            if (code.length > 1) {
                // get the last token
                return "EPSG:" + code[code.length - 1];
            }
        }
        return crs;
    }
}
