package org.oskari.capabilities.ogc;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;

@Oskari(OskariLayer.TYPE_WFS)
public class WFSCapabilitiesParser extends OGCCapabilitiesParser {

    private static final String NAMESPACE_WFS = "http://www.opengis.net/wfs";
    private static final String ROOT_WFS = "WFS_Capabilities";

    protected String getVersionParamName() {
        return "acceptVersions";
    }
    protected void validateCapabilities(String version, String ns, String name)
            throws ServiceException {

        checkNamespaceStartsWith(ns, NAMESPACE_WFS);
        checkRootElementNameEquals(name, ROOT_WFS);
    }
}
