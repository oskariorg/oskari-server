package org.oskari.wcs.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.oskari.wcs.capabilities.Capabilities;
import org.oskari.wcs.coverage.CoverageDescription;
import org.oskari.wcs.extension.Interpolation;

public class GetCoverage {

    public static final String MEDIA_TYPE_MULTI_PART = "multipart/related";

    private final Capabilities wcs;
    private final CoverageDescription desc;

    private final String coverageId;
    private final String format;
    private final List<String> subset;
    private boolean multiPart;
    private Interpolation interpolation;

    private GetCoverage(Capabilities wcs, CoverageDescription desc) {
        this(wcs, desc, desc.getNativeFormat());
    }

    private GetCoverage(Capabilities wcs, CoverageDescription desc, String format)
            throws IllegalArgumentException {
        if (!wcs.supportsFormat(format)) {
            throw new IllegalArgumentException("Invalid format");
        }
        this.wcs = wcs;
        this.desc = desc;
        this.coverageId = desc.getCoverageId();
        this.format = format;
        this.subset = new ArrayList<>();
    }

    public GetCoverage multiPart() {
        this.multiPart = true;
        return this;
    }

    public GetCoverage subset(String dimension, int low, int high) {
        subset.add(String.format("%s(%d,%d)", dimension, low, high));
        return this;
    }

    public GetCoverage subset(String dimension, int point) {
        subset.add(String.format("%s(%d)", dimension, point));
        return this;
    }

    public GetCoverage subset(String dimension, double low, double high) {
        subset.add(String.format("%s(%.f,%.f)", dimension, low, high));
        return this;
    }

    public GetCoverage subset(String dimension, double point) {
        subset.add(String.format("%s(%.f)", dimension, point));
        return this;
    }

    public GetCoverage interpolation(Interpolation interpolation) {
        if (!wcs.supportsInterpolation()
                || !wcs.supportsInterpolation(interpolation)) {
            throw new UnsupportedOperationException();
        }
        this.interpolation = interpolation;
        return this;
    }

    public Map<String, String[]> toKVP() {
        Map<String, String[]> kvp = new HashMap<>();
        put(kvp, "service", "WCS");
        put(kvp, "version", "2.0.1");
        put(kvp, "request", "GetCoverage");
        put(kvp, "coverageId", coverageId);
        put(kvp, "format", format);
        if (multiPart) {
            put(kvp, "mediaType", MEDIA_TYPE_MULTI_PART);
        }
        kvp.put("subset", subset.toArray(new String[subset.size()]));
        if (interpolation != null) {
            put(kvp, "interpolation", interpolation.method);
        }
        return kvp;
    }

    private static void put(Map<String, String[]> map, String key, String value) {
        map.put(key, new String[] { value });
    }

}
