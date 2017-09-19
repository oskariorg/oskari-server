package org.oskari.wcs.capabilities;

import java.util.List;
import java.util.Map;

public class ServiceMetadata {

    private final List<String> formatSupported;
    private final Map<String, List<String>> extensions;

    public ServiceMetadata(List<String> formatSupported, Map<String, List<String>> extensions) {
        this.formatSupported = formatSupported;
        this.extensions = extensions;
    }

    public List<String> getFormatSupported() {
        return formatSupported;
    }

    public List<String> getExtensions(String extension) {
        return getExtensions(null, extension);
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
