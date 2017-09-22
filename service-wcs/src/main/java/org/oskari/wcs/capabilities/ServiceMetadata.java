package org.oskari.wcs.capabilities;

import java.util.List;
import java.util.Map;

public class ServiceMetadata {

    private final List<String> supportedFormats;
    private final Map<String, List<String>> extensions;

    public ServiceMetadata(List<String> supportedFormats, Map<String, List<String>> extensions) {
        this.supportedFormats = supportedFormats;
        this.extensions = extensions;
    }

    public List<String> getSupportedFormats() {
        return supportedFormats;
    }

    public List<String> getExtensions(String extension) {
        return extensions.get(extension);
    }

}
