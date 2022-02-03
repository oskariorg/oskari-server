package org.oskari.capabilities.ogc;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import org.oskari.capabilities.LayerCapabilities;

import java.util.Collections;
import java.util.Map;

// commented out until we have an implementation here for parseLayers()
// @Oskari(OskariLayer.TYPE_WMS)
public class WMSCapabilitiesParser extends OGCCapabilitiesParser {

    private static final String NAMESPACE_WMS = "http://www.opengis.net/wms";

    private static final String ROOT_WMS_LESS_THAN_130 = "WMT_MS_Capabilities";
    private static final String ROOT_WMS_130 = "WMS_Capabilities";

    protected void validateCapabilities(String version, String ns, String name)
            throws ServiceException {

        if (version == null) {
            // Layer didn't specify a version - response could be of any WMS version
            if (ns != null) {
                // Can only be 1.3.0
                checkNamespaceStartsWith(ns, NAMESPACE_WMS);
                checkRootElementNameEquals(name, ROOT_WMS_130);
            } else {
                checkRootElementNameEquals(name, ROOT_WMS_LESS_THAN_130);
            }
        } else if ("1.3.0".equals(version)) {
            checkNamespaceStartsWith(ns, NAMESPACE_WMS);
            checkRootElementNameEquals(name, ROOT_WMS_130);
        } else {
            checkRootElementNameEquals(name, ROOT_WMS_LESS_THAN_130);
        }
    }

    public Map<String, LayerCapabilities> parseLayers(String xml) throws ServiceException {
        return Collections.emptyMap();
    }
}