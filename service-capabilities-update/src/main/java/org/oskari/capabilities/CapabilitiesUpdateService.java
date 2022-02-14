package org.oskari.capabilities;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;

import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

/**
 * This might get deprecated as most of the code is in CapabiliesService.
 * This currently handles saving things to database and we are likely to get rid of that and just use
 * the parsed capabilities in JSON format.
 * @deprecated
 */
@Deprecated
public class CapabilitiesUpdateService {

    private static final Logger LOG = LogFactory.getLogger(UpdateCapabilitiesJob.class);

    private static final String ERR_LAYER_TYPE_UNSUPPORTED = "Layer type not supported for update";
    private static final String ERR_FAILED_TO_FETCH_CAPABILITIES = "Failed to get Capabilities data";
    private static final String ERR_LAYER_NOT_FOUND_IN_CAPABILITIES = "Could not find layer from Capabilities";
    private static final String ERR_FAILED_TO_PARSE_CAPABILITIES = "Failed to parse Capabilities";

    private final OskariLayerService layerService;

    public CapabilitiesUpdateService(OskariLayerService layerService) {
        this.layerService = layerService;
    }

    public List<CapabilitiesUpdateResult> updateCapabilities(List<OskariLayer> layersToUpdate, Set<String> systemCRSs) {
        List<CapabilitiesUpdateResult> results = new ArrayList<>(layersToUpdate.size());

        List<OskariLayer> updateableLayers = new ArrayList<>();
        for (OskariLayer layer : layersToUpdate) {
            if (canUpdate(layer.getType())) {
                updateableLayers.add(layer);
            } else {
                results.add(CapabilitiesUpdateResult.err(layer, ERR_LAYER_TYPE_UNSUPPORTED));
            }
        }

        Map<UrlTypeVersion, List<OskariLayer>> layersByUTV = updateableLayers.stream()
                .collect(groupingBy(layer -> new UrlTypeVersion(layer)));

        for (UrlTypeVersion utv : layersByUTV.keySet()) {
            List<OskariLayer> layers = layersByUTV.get(utv);
            updateCapabilities(utv, layers, systemCRSs, results);
        }

        return results;
    }

    protected static boolean canUpdate(String type) {
        switch (type) {
        case OskariLayer.TYPE_WMS:
        case OskariLayer.TYPE_WMTS:
            return true;
        default:
            return false;
        }
    }

    private void updateCapabilities(UrlTypeVersion utv,
            List<OskariLayer> layers, Set<String> systemCRSs, List<CapabilitiesUpdateResult> results) {


        OskariLayer credentialsLayer = layers.get(0);
        // include possible apikeys etc
        final String url = IOHelper.constructUrl(utv.url, JSONHelper.getObjectAsMap(credentialsLayer.getParams()));
        ServiceConnectInfo info = new ServiceConnectInfo(url, utv.type, utv.version);
        info.setCredentials(credentialsLayer.getUsername(), credentialsLayer.getPassword());

        final String type = info.getType();
        final String version = info.getVersion();

        int[] ids = layers.stream().mapToInt(OskariLayer::getId).toArray();
        LOG.debug("Updating Capabilities for a group of layers - url:", url,
                "type:", type, "version:", version, "ids:", Arrays.toString(ids));

        try {
            Map<String, LayerCapabilities> capabilities = CapabilitiesService.getLayersFromService(info);
            updateLayers(layers, capabilities, systemCRSs, results);
        } catch (IOException e) {
            LOG.warn(e, "Error accessing Capabilities for service, url:", url,
                    "type:", type, "version:", version, "ids:", Arrays.toString(ids));
            for (OskariLayer layer : layers) {
                results.add(CapabilitiesUpdateResult.err(layer, ERR_FAILED_TO_FETCH_CAPABILITIES));
            }
            return;
        } catch (ServiceException e) {
            LOG.warn(e, "Failed to parse GetCapabilities for layerIds:", Arrays.toString(ids));
            for (OskariLayer layer : layers) {
                results.add(CapabilitiesUpdateResult.err(layer, ERR_FAILED_TO_PARSE_CAPABILITIES));
            }
            return;
        }

    }

    private void updateLayers(List<OskariLayer> layers, Map<String, LayerCapabilities> capabilities,
                              Set<String> systemCRSs, List<CapabilitiesUpdateResult> results) {
        for (OskariLayer layer : layers) {
            try {
                LayerCapabilities caps = capabilities.get(layer.getName());
                if (caps == null) {
                    results.add(CapabilitiesUpdateResult.err(layer, ERR_LAYER_NOT_FOUND_IN_CAPABILITIES));
                    continue;
                }
                layer.setCapabilities(CapabilitiesService.toJSON(caps, systemCRSs));
                layerService.update(layer);
                results.add(CapabilitiesUpdateResult.ok(layer));
            } catch (IllegalArgumentException e) {
                results.add(CapabilitiesUpdateResult.err(layer, e.getMessage()));
            }
        }
    }

    private static class UrlTypeVersion {

        private final String url;
        private final String type;
        private final String version;

        private UrlTypeVersion(OskariLayer layer) {
            url = layer.getSimplifiedUrl(true);
            type = layer.getType();
            version = layer.getVersion();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof UrlTypeVersion)) {
                return false;
            }
            if (o == this) {
                return true;
            }
            UrlTypeVersion s = (UrlTypeVersion) o;
            return url.equals(s.url)
                    && type.equals(s.type)
                    && version.equals(s.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, type, version);
        }

    }

}
