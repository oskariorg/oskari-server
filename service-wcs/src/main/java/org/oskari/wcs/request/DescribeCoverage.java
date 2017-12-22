package org.oskari.wcs.request;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.oskari.utils.common.Maps;
import org.oskari.wcs.WCS;
import org.oskari.wcs.capabilities.Capabilities;
import org.oskari.wcs.capabilities.Operation;

public class DescribeCoverage {

    private DescribeCoverage() { /* Block */ }

    /**
     * Create query parameters for a GET encoded DescribeCoverage request You
     * can use {@link #coverageExists(Capabilities, String)} to see if the
     * coverage exists
     *
     * @param coverageId
     *            id of the coverage
     * @return Map with query parameters
     */
    public static Map<String, String> toQueryParameters(String coverageId) {
        return Maps.of(
                "service", "WCS",
                "version", WCS.VERSION_201,
                "request", "DescribeCoverage",
                "coverageId", coverageId);
    }

    /**
     * Get the HTTP endpoint declared in GetCapabilities for
     * GET encoded DescribeCoverage request
     *
     * @param wcs capabilities of the service
     * @return the endpoint which might not exist for GET binding
     */
    public static Optional<String> getDescribeCoverageEndPointGET(Capabilities wcs) {
        Objects.requireNonNull(wcs);
        if (!wcs.supportsGET()) {
            return Optional.empty();
        }
        Operation op = wcs.findOperation("DescribeCoverage")
                .orElseThrow(() -> new IllegalArgumentException(
                        "The OperationsMetadata component shall contain three "
                                + "Operation instances with case-sensitive name values "
                                + "'GetCapabilities', 'DescribeCoverage', and 'GetCoverage'"));
        return op.getGet();
    }

    /**
     * Get the HTTP endpoint declared in GetCapabilities for
     * POST encoded DescribeCoverage request
     *
     * @param wcs capabilities of the service
     * @return the endpoint which might not exist for POST binding
     */
    public static Optional<String> getDescribeCoverageEndPointPOST(Capabilities wcs) {
        Objects.requireNonNull(wcs);
        if (!wcs.supportsPOST()) {
            return Optional.empty();
        }
        Operation op = wcs.findOperation("DescribeCoverage")
                .orElseThrow(() -> new IllegalArgumentException(
                        "The OperationsMetadata component shall contain three "
                                + "Operation instances with case-sensitive name values "
                                + "'GetCapabilities', 'DescribeCoverage', and 'GetCoverage'"));
        return op.getPost();
    }

}
