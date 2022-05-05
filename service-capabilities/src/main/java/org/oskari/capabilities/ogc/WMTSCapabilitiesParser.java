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

    public Class<? extends LayerCapabilities> getCapabilitiesClass() {
        return LayerCapabilitiesWMTS.class;
    }
    protected String getVersionParamName() {
        return "acceptVersions";
    }
    protected String getDefaultVersion() { return "1.0.0"; }

    protected Map<String, LayerCapabilities> parseLayers(String capabilities) throws ServiceException {
        try {
            WMTSCapabilities caps = WMTSCapabilitiesParserHelper.parseCapabilities(capabilities);
            Map<String, LayerCapabilities> layers = new HashMap<>();
            caps.getLayers().stream().map(layer -> {
                LayerCapabilitiesWMTS l = new LayerCapabilitiesWMTS(layer.getId(), layer.getTitle());
                l.setStyles(layer.getStyles(), layer.getDefaultStyle());
                l.setSrs(layer.getLinks().stream()
                        .map(link -> link.getTileMatrixSet().getCrs())
                        .collect(Collectors.toSet()));

                // should we prioritize png over jpg?
                l.setFormats(layer.getFormats());
                // GFI is not handled for WMTS at all in GetGeoPointDataHandler
                l.setInfoFormats(layer.getInfoFormats());
                l.setResourceUrls(layer.getResourceUrls());
                l.setTileMatrixLinks(layer.getLinks());
                return l;
            }).forEach(l -> layers.put(l.getName(), l));
            return layers;
        } catch (Exception e) {
            throw new ServiceException("Unable to parse layers for WMTS capabilities", e);
        }
    }
}
