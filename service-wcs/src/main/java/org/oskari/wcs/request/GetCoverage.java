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
    private List<String> interpolationPerAxis;

    private String subsettingCRS;
    private String outputCRS;

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
        checkDimension(dimension);
        subset.add(String.format("%s(%d,%d)", dimension, low, high));
        return this;
    }

    public GetCoverage subset(String dimension, int point) {
        checkDimension(dimension);
        subset.add(String.format("%s(%d)", dimension, point));
        return this;
    }

    public GetCoverage subset(String dimension, double low, double high) {
        checkDimension(dimension);
        subset.add(String.format("%s(%.f,%.f)", dimension, low, high));
        return this;
    }

    public GetCoverage subset(String dimension, double point) {
        checkDimension(dimension);
        subset.add(String.format("%s(%.f)", dimension, point));
        return this;
    }

    private void checkDimension(String dimension) {
        if (!desc.hasAxis(dimension)) {
            throw new UnsupportedOperationException();
        }
    }

    public GetCoverage interpolation(Interpolation interp) {
        if (!wcs.supportsInterpolation()
                || !wcs.supportsInterpolation(interp)) {
            throw new UnsupportedOperationException();
        }
        this.interpolation = interp;
        return this;
    }

    public GetCoverage interpolationPerAxis(String axis, Interpolation interp) {
        if (!wcs.supportsInterpolation()
                || !wcs.supportsInterpolation(interp)
                || !desc.hasAxis(axis)) {
            throw new UnsupportedOperationException();
        }
        if (interpolationPerAxis == null) {
            interpolationPerAxis = new ArrayList<>();
        }
        interpolationPerAxis.add(String.format("%s,%s", axis, interp.method));
        return this;
    }

    public GetCoverage subsettingCRS(String crs) {
        if (!wcs.supportsCRS()
                || !wcs.supportsCRS(crs)) {
            throw new UnsupportedOperationException();
        }
        this.subsettingCRS = crs;
        return this;
    }

    public GetCoverage outputCRS(String crs) {
        if (!wcs.supportsCRS()
                || !wcs.supportsCRS(crs)) {
            throw new UnsupportedOperationException();
        }
        this.outputCRS = crs;
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
        if (subset.size() > 0) {
            kvp.put("subset", subset.toArray(new String[subset.size()]));
        }
        if (interpolation != null) {
            put(kvp, "interpolation", interpolation.method);
        }
        if (interpolationPerAxis != null) {
            kvp.put("interpolationPerAxis",
                    interpolationPerAxis.toArray(new String[interpolationPerAxis.size()]));
        }
        if (subsettingCRS != null) {
            put(kvp, "subsettingCRS", subsettingCRS);
        }
        if (outputCRS != null) {
            put(kvp, "outputCRS", outputCRS);
        }
        return kvp;
    }

    private static void put(Map<String, String[]> map, String key, String value) {
        map.put(key, new String[] { value });
    }

}
