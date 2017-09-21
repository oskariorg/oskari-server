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

    public List<String> getExtensions(Extensions ext) {
        return extensions.get(getNamespaceLocalName(ext.ns, ext.name));
    }

    public List<String> getExtensions(String ns, String extension) {
        return extensions.get(getNamespaceLocalName(ns, extension));
    }

    public static String getNamespaceLocalName(String ns, String localName) {
        if (ns == null || ns.isEmpty()) {
            return localName;
        }
        return String.format("{%s}%s", ns, localName);
    }

}
