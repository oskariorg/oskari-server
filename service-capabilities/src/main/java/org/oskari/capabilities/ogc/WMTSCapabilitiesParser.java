package org.oskari.capabilities.ogc;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;

@Oskari(OskariLayer.TYPE_WMTS)
public class WMTSCapabilitiesParser extends OGCCapabilitiesParser {
    private static final String NAMESPACE_WMTS = "http://www.opengis.net/wmts";
    private static final String ROOT_WMTS = "Capabilities";

    protected String getVersionParamName() {
        return "acceptVersions";
    }
    protected void validateCapabilities(String version, String ns, String name)
            throws ServiceException {
        checkNamespaceStartsWith(ns, NAMESPACE_WMTS);
        checkRootElementNameEquals(name, ROOT_WMTS);
    }
}
