package org.oskari.capabilities;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import org.oskari.capabilities.ogc.OGCCapabilitiesParser;

import java.io.IOException;

public class RawCapabilitiesHelper {
    public static RawCapabilitiesResponse getCapabilities(OskariLayer layer) throws ActionException {
        CapabilitiesParser parser = CapabilitiesService.getParser(layer.getType());
        if (parser == null) {
            throw new ActionParamsException("Unsupported layer type: " + layer.getType());
        }
        if (!(parser instanceof OGCCapabilitiesParser)) {
            throw new ActionParamsException("Only OGC layers support capabilities");
        }
        OGCCapabilitiesParser ogcParser = (OGCCapabilitiesParser) parser;
        String url = ogcParser.contructCapabilitiesUrl(layer.getUrl(), layer.getVersion());
        try {
            return ogcParser.fetchCapabilities(url, layer.getUsername(), layer.getPassword(), ogcParser.getExpectedContentType(layer.getVersion()));
        } catch (IOException | ServiceException  ex) {
            throw new ActionException("Error fetching capabilities", ex);
        }
    }
}
