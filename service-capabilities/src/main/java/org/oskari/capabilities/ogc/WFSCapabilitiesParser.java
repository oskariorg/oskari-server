package org.oskari.capabilities.ogc;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.ServiceConnectInfo;
import org.oskari.capabilities.ogc.api.OGCAPIFeaturesService;
import org.oskari.capabilities.ogc.wfs.WFSCapsParser;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Oskari(OskariLayer.TYPE_WFS)
public class WFSCapabilitiesParser extends OGCCapabilitiesParser {

    private static final String OGC_API_VERSION = "3.0.0";

    protected String getVersionParamName() {
        return "acceptVersions";
    }
    protected String getDefaultVersion() { return "1.1.0"; }
    protected String getExpectedContentType(String version) {
        if (OGC_API_VERSION.equals(version)) {
            return "json";
        }
        return getExpectedContentType();
    }

    public Map<String, LayerCapabilities> getLayersFromService(ServiceConnectInfo src) throws IOException, ServiceException {
        if (OGC_API_VERSION.equals(src.getVersion())) {
            try {
                OGCAPIFeaturesService service = OGCAPIFeaturesService.fromURL(src.getUrl(), src.getUser(), src.getPass());
                listToMap(getOGCAPIFeatures(service));
            } catch (JsonParseException | JsonMappingException e) {
                // Don't attach JsonParseException as its an IOException which is detected as root cause and wrong error is sent to user
                throw new ServiceException("Error parsing response from url: " + src.getUrl() + " Message: " + e.getMessage());
            } catch (Exception e) {
                throw new ServiceException("Error occured getting OAPIF capabilities", e);
            }
        }
        return super.getLayersFromService(src);
    }

    public Map<String, LayerCapabilities> parseLayers(String xml) throws ServiceException {
        return parseLayers(xml, getDefaultVersion());
    }

    public Map<String, LayerCapabilities> parseLayers(String response, String version) throws ServiceException {
        try {
            List<LayerCapabilitiesWFS> caps;
            if (OGC_API_VERSION.equals(version)) {
                caps = getOGCAPIFeatures(response);
            } else {
                // TODO: fetch describe feature type
                caps = WFSCapsParser.parseCapabilities(response);
            }
            return listToMap(caps);
        } catch (Exception e) {
            throw new ServiceException("Unable to parse layers for WFS capabilities", e);
        }
    }

    protected Map<String, LayerCapabilities> listToMap(List<LayerCapabilitiesWFS> caps) {
        Map<String, LayerCapabilities> layers = new HashMap<>();
        caps.forEach(layer -> layers.put(layer.getName(), layer));
        return layers;
    }

    protected List<LayerCapabilitiesWFS> getOGCAPIFeatures(String json) {
        try {
            OGCAPIFeaturesService service = OGCAPIFeaturesService.fromJSON(json);
            return getOGCAPIFeatures(service);
        } catch (IOException e) {}
        return Collections.emptyList();
    }

    protected List<LayerCapabilitiesWFS> getOGCAPIFeatures(OGCAPIFeaturesService service) {
        return service.getCollections().stream().map(collection -> {
                    String name = collection.getId();
                    String title = collection.getTitle();

                    LayerCapabilitiesWFS featureType = new LayerCapabilitiesWFS(name, title);
                    featureType.setVersion(OGC_API_VERSION);
                    featureType.setDescription(collection.getDescription());
                    featureType.setSrs(service.getSupportedEpsgCodes(name));
                    featureType.setSupportedCrsURIs(service.getSupportedCrsURIs(name));

                    featureType.setFormats(service.getSupportedFormats(name));
                    return featureType;
                })
                .collect(Collectors.toList());
    }
}
