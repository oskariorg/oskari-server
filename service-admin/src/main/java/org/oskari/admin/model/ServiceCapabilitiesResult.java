package org.oskari.admin.model;

import fi.nls.oskari.util.PropertyUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Used to write JSON for admin functionality when adding layers
 */
public class ServiceCapabilitiesResult {
    private String title;
    private List<MapLayerAdminOutput> layers;
    private List<String> layersWithErrors;
    private Map<String, List<Integer>> existingLayers;
    private String version;
    private String currentSrs = PropertyUtil.get("oskari.native.srs", "EPSG:4326");

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, List<Integer>> getExistingLayers() {
        return existingLayers;
    }

    public void setExistingLayers(Map<String, List<Integer>> existingLayers) {
        this.existingLayers = existingLayers;
    }

    public List<String> getLayersWithErrors() {
        return layersWithErrors;
    }

    public void setLayersWithErrors(List<String> layersWithErrors) {
        this.layersWithErrors = layersWithErrors;
    }

    /**
     * Returns layers not in list but JSON object with name as key and the layer JSON as value
     * @return
     */
    public Map<String, MapLayerAdminOutput> getLayers() {
        return layers.stream().collect(
                Collectors.toMap(MapLayerAdminOutput::getName, Function.identity()));
    }

    public void setLayers(List<MapLayerAdminOutput> layers) {
        this.layers = layers;
    }

    public void setCurrentSrs(String currentSrs) {
        this.currentSrs = currentSrs;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getCapabilitiesFailed() {
        if (layers == null) {
            return Collections.emptyList();
        }
        return layers.stream()
                .filter(l -> l.getCapabilities() == null || l.getCapabilities().isEmpty())
                .map(l -> l.getName())
                .collect(Collectors.toList());
    }

    public List<String> getUnsupportedLayers() {
        if (layers == null) {
            return Collections.emptyList();
        }
        return layers.stream()
                .filter(l -> !getCapabilitiesFailed().contains(l.getName()))
                .filter(l -> l.getCapabilities() != null)
                .filter(l -> !supportsSRS(l, currentSrs))
                .map(l -> l.getName())
                .collect(Collectors.toList());
    }

    private boolean supportsSRS(MapLayerAdminOutput layer, String srs) {
        List<String> supportedSRS = (List<String>) layer.getCapabilities()
                .getOrDefault("srs", Collections.emptyList());
        return supportedSRS.contains(srs);
    }
}
