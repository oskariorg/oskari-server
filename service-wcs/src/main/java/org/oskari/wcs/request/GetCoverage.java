package org.oskari.wcs.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.oskari.utils.common.StringUtils;
import org.oskari.wcs.WCS;
import org.oskari.wcs.capabilities.Capabilities;
import org.oskari.wcs.coverage.CoverageDescription;
import org.oskari.wcs.coverage.RectifiedGridCoverage;
import org.oskari.wcs.extension.Interpolation;
import org.oskari.wcs.extension.scaling.ScaleAxesByFactor;
import org.oskari.wcs.extension.scaling.ScaleAxis;
import org.oskari.wcs.extension.scaling.ScaleByFactor;
import org.oskari.wcs.extension.scaling.ScaleToExtent;
import org.oskari.wcs.extension.scaling.ScaleToSize;
import org.oskari.wcs.extension.scaling.Scaling;
import org.oskari.wcs.extension.scaling.TargetAxisExtent;
import org.oskari.wcs.extension.scaling.TargetAxisSize;

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

    private Scaling scaling;

    public GetCoverage(Capabilities wcs, CoverageDescription desc) {
        this(wcs, desc, desc.getNativeFormat());
    }

    public GetCoverage(Capabilities wcs, CoverageDescription desc, String format)
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
        subset.add(String.format(Locale.US, "%s(%f,%f)", dimension, low, high));
        return this;
    }

    public GetCoverage subset(String dimension, double point) {
        checkDimension(dimension);
        subset.add(String.format(Locale.US, "%s(%f)", dimension, point));
        return this;
    }

    private void checkDimension(String dimension) {
        if (!desc.hasAxis(dimension)) {
            throw new UnsupportedOperationException();
        }
    }

    public GetCoverage multipart(boolean multiPart) {
        this.multiPart = multiPart;
        return this;
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

    public GetCoverage scaling(Scaling scaling) {
        // TODO: once we support other types of GridCoverages should be removed
        if (!(desc instanceof RectifiedGridCoverage)) {
            throw new IllegalArgumentException("Scaling extension only defined for Grid based coverages");
        }
        // TODO: check that axes exist
        this.scaling = scaling;
        return this;
    }

    public Map<String, String[]> toKVP() {
        Map<String, String[]> kvp = new HashMap<>();
        put(kvp, "service", "WCS");
        put(kvp, "version", WCS.VERSION_201);
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
        if (scaling != null) {
            if (scaling instanceof ScaleByFactor) {
                ScaleByFactor sbf = (ScaleByFactor) scaling;
                put(kvp, "SCALEFACTOR", Double.toString(sbf.scaleFactor));
            } else if (scaling instanceof ScaleAxesByFactor) {
                put(kvp, "SCALEAXES", scaleAxesKVP((ScaleAxesByFactor) scaling));
            } else if (scaling instanceof ScaleToSize) {
                put(kvp, "SCALESIZE", scaleAxesKVP((ScaleToSize) scaling));
            } else if (scaling instanceof ScaleToExtent) {
                put(kvp, "SCALEEXTENT", scaleAxesKVP((ScaleToExtent) scaling));

            }
        }
        return kvp;
    }

    private static void put(Map<String, String[]> map, String key, String value) {
        map.put(key, new String[] { value });
    }

    /**
     * SCALEAXES=a1(s1),...,an(sn) where, for 1<=i<=n,
     * - ai is an axis abbreviation;
     * - si is a scaleFactor expressed as the ASCII representation of a positive floating-point number
     */
    private static String scaleAxesKVP(ScaleAxesByFactor sabf) {
        ScaleAxis[] scaleAxes = sabf.scaleAxes;
        int n = scaleAxes.length;
        String[] values = new String[n];
        for (int i = 0; i < n; i++) {
            values[i] = String.format(Locale.US, "%s(%f)", scaleAxes[i].axis, scaleAxes[i].scaleFactor);
        }
        return StringUtils.join(values, ',');
    }

    /**
     * SCALESIZE=a1(s1),...,an(sn) where, for 1<=i<=n,
     * - ai is an axis abbreviation;
     * - si are sizes
     */
    private static String scaleAxesKVP(ScaleToSize sts) {
        TargetAxisSize[] targetAxisSizes = sts.targetAxisSizes;
        int n = targetAxisSizes.length;
        String[] values = new String[n];
        for (int i = 0; i < n; i++) {
            values[i] = String.format("%s(%d)", targetAxisSizes[i].axis, targetAxisSizes[i].targetSize);
        }
        return StringUtils.join(values, ',');
    }

    /**
     * SCALEEXTENT=a1(lo1:hi1),...,an(lon:hin) where, for 1<=i<=n,
     * - ai is an axis abbreviation;
     * - loi and hii are low and high, respectively, each of them represented as either a string,
     *               enclosed in double quotes, or a number
     */
    private static String scaleAxesKVP(ScaleToExtent ste) {
        TargetAxisExtent[] scaleAxes = ste.axisExtents;
        int n = scaleAxes.length;
        String[] values = new String[n];
        for (int i = 0; i < n; i++) {
            values[i] = String.format(Locale.US, "%s(%f:%f)", scaleAxes[i].axis, scaleAxes[i].low, scaleAxes[i].high);
        }
        return StringUtils.join(values, ',');
    }

}
