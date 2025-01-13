package org.oskari.permissions.model;

import java.util.List;
import java.util.Optional;

public class PermissionSet {

    private final List<Resource> resources;

    public PermissionSet(List<Resource> resources) {
        this.resources = resources;
    }

    public Optional<Resource> get(ResourceType type, String mapping) {
        return get(type.name(), mapping);
    }

    public Optional<Resource> get(String type, String mapping) {
        return resources.stream()
                .filter(res-> res.isOfType(type))
                .filter(res-> res.getMapping().equals(mapping))
                .findFirst();
    }
}
