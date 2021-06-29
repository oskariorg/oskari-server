package org.oskari.control.layer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.nls.oskari.domain.map.DataProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LayerListResponse {

    // TODO: groups
    private List<LayerOutput> layers;
    private Map<String, String> providers = new HashMap<>();

    public LayerListResponse(List<LayerOutput> layers) {
        this.layers = layers;
    }

    public void setupProviders(List<DataProvider> allProviders, String language) {
        Set<Integer> referencedProviders = getProviderIds(layers);

        allProviders.stream()
                .forEach(provider -> {
                    int id = provider.getId();
                    if (referencedProviders.contains(id)) {
                        providers.put(Integer.toString(id), provider.getName(language));
                    }
                });
    }

    /**
     * Constructs a set of dataprovider ids that are used in the layers that will be returned to the user.
     */
    @JsonIgnore
    private Set<Integer> getProviderIds(List<LayerOutput> models) {
        return models.stream()
                .map(m -> m.dataprovider)
                .collect(Collectors.toSet());
    }
}
