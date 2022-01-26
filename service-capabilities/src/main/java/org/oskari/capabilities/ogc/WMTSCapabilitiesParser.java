package org.oskari.capabilities.ogc;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.ogc.wmts.WMTSCapabilities;
import org.oskari.capabilities.ogc.wmts.WMTSCapabilitiesParserHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

    protected Map<String, LayerCapabilities> parseLayers(String capabilities) throws ServiceException {
        try {
            WMTSCapabilities caps = WMTSCapabilitiesParserHelper.parseCapabilities(capabilities);
            Map<String, LayerCapabilities> layers = new HashMap<>();
            caps.getLayers().stream().map(layer -> {
                LayerCapabilitiesWMTS l = new LayerCapabilitiesWMTS(layer.getId(), layer.getTitle());
                l.setStyles(layer.getStyles(), layer.getDefaultStyle());
                l.setSrs(layer.getLinks().stream()
                        // TODO: normalize crs to short format
                        .map(link -> link.getTileMatrixSet().getCrs())
                        .collect(Collectors.toSet()));

                // should we prioritize png over jpg?
                l.setFormats(layer.getFormats());
                // GFI is not handled for WMTS at all in GetGeoPointDataHandler
                l.setInfoFormats(layer.getInfoFormats());
                l.setResourceUrls(layer.getResourceUrls());
                l.setTileMatrices(layer.getLinks());
                return l;
            }).forEach(l -> layers.put(l.getName(), l));
            return layers;
        } catch (Exception e) {
            throw new ServiceException("Unable to parse layers for WMTS capabilities", e);
        }
    }
}
