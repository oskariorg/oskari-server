package org.oskari.capabilities.ogc;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.ogc.wms.WMSCapsParser;
import org.oskari.capabilities.ogc.wms.WMSCapsParser1_1_1;
import org.oskari.capabilities.ogc.wms.WMSCapsParser1_3_0;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Oskari(OskariLayer.TYPE_WMS)
public class WMSCapabilitiesParser extends OGCCapabilitiesParser {

    private static final String NAMESPACE_WMS = "http://www.opengis.net/wms";

    protected String getDefaultVersion() { return "1.3.0"; }

    protected void validateCapabilities(String version, String ns, String name)
            throws ServiceException {

        if (version == null) {
            // Layer didn't specify a version - response could be of any WMS version
            if (ns != null) {
                // Can only be 1.3.0
                checkNamespaceStartsWith(ns, NAMESPACE_WMS);
                checkRootElementNameEquals(name, WMSCapsParser1_3_0.ROOT_EL);
            } else {
                checkRootElementNameEquals(name, WMSCapsParser1_1_1.ROOT_EL);
            }
        } else if ("1.3.0".equals(version)) {
            checkNamespaceStartsWith(ns, NAMESPACE_WMS);
            checkRootElementNameEquals(name, WMSCapsParser1_3_0.ROOT_EL);
        } else {
            checkRootElementNameEquals(name, WMSCapsParser1_1_1.ROOT_EL);
        }
    }

    public Map<String, LayerCapabilities> parseLayers(String xml) throws ServiceException {
        try {
            List<LayerCapabilitiesWMS> layers = WMSCapsParser.parseCapabilities(xml);
            Map<String, LayerCapabilities> value = new HashMap<>();
            addLayers(value, layers, null);
            return value;
        } catch (Exception e) {
            throw new ServiceException("Unable to parse layers for WMS capabilities", e);
        }
    }

    private void addLayers(Map<String, LayerCapabilities> value, List<LayerCapabilitiesWMS> layers, String parent) {
        if (layers == null) {
            return;
        }
        layers.stream().forEach(l -> {
            l.setParent(parent);
            if (!l.isGroupLayer()) {
                value.put(l.getName(), l);
            }
            addLayers(value, l.getLayers(), l.getName());
        });
    }
}
