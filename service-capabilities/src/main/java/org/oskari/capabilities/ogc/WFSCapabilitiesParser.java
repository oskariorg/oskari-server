package org.oskari.capabilities.ogc;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.ogc.wfs.WFSCapsParser;

import javax.xml.stream.XMLStreamException;
import java.util.*;

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

    public Map<String, LayerCapabilities> parseLayers(String xml) throws ServiceException {
        return parseLayers(xml, getDefaultVersion());
    }

    public Map<String, LayerCapabilities> parseLayers(String response, String version) throws ServiceException {
        Map<String, LayerCapabilities> layers = new HashMap<>();
        try {
            List<LayerCapabilitiesWFS> caps;
            if (OGC_API_VERSION.equals(version)) {
                caps = getOGCAPIFeatures(response);
            } else {
                caps = getLegacyFeatures(response);
            }
            caps.forEach(layer -> layers.put(layer.getName(), layer));
        } catch (Exception e) {
            throw new ServiceException("Unable to parse layers for WFS capabilities", e);
        }
        return layers;
    }

    private List<LayerCapabilitiesWFS> getOGCAPIFeatures(String json) {
        // TODO: OGC API parsing
        return new ArrayList<>();
    }

    private List<LayerCapabilitiesWFS> getLegacyFeatures(String xml) throws XMLStreamException {
        // TODO: fetch describe feature type
        return WFSCapsParser.parseCapabilities(xml);
    }

}
