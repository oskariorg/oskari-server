package org.oskari.capabilities.ogc;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.ogc.wmts.WMTSCapabilities;
import org.oskari.capabilities.ogc.wmts.WMTSCapabilitiesParserHelper;

import java.util.HashMap;
import java.util.Map;

import static org.oskari.capabilities.ogc.CapabilitiesConstants.FORMATS;
import static org.oskari.capabilities.ogc.CapabilitiesConstants.INFO_FORMATS;

@Oskari(OskariLayer.TYPE_WMTS)
public class WMTSCapabilitiesParser extends OGCCapabilitiesParser {
    private static final String NAMESPACE_WMTS = "http://www.opengis.net/wmts";
    private static final String ROOT_WMTS = "Capabilities";

    private static final String RESOURCE_URLS = "resourceUrls";
    private static final String TILEMATRIX = "tileMatrix";

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
                LayerCapabilities l = new LayerCapabilities(layer.getId(), layer.getTitle());
                l.setStyles(layer.getStyles(), layer.getDefaultStyle());

                l.addLayerSpecific(FORMATS, layer.getFormats());
                l.addLayerSpecific(INFO_FORMATS, layer.getInfoFormats());
                l.addLayerSpecific(RESOURCE_URLS, layer.getResourceUrls());
                l.addLayerSpecific(TILEMATRIX, layer.getLinks());
                return l;
            }).forEach(l -> layers.put(l.getName(), l));
            return layers;
        } catch (Exception e) {
            throw new ServiceException("Unable to parse layers for WMTS", e);
        }
    }
}
