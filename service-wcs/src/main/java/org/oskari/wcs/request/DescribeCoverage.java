package org.oskari.wcs.request;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.oskari.wcs.capabilities.Capabilities;
import org.oskari.wcs.capabilities.Operation;
import org.oskari.wcs.util.small.SmallMap;

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
        return new SmallMap(
                "service", "WCS",
                "version", "2.0.1",
                "request", "DescribeCoverage",
                "coverageId", coverageId);
    }

    /**
     * Get the HTTP endpoint declared in GetCapabilities for DescribeCoverage
     * request
     *
     * @param wcs capabilities of the service
     * @param get true if you want the GET endpoint, false if POST
     * @return the endpoint which might not exist for your binding
     */
    public static Optional<String> getDescribeCoverageEndPoint(Capabilities wcs, boolean get) {
        Objects.requireNonNull(wcs);
        if (get) {
            if (!wcs.supportsGET()) {
                return Optional.empty();
            }
        } else {
            if (!wcs.supportsPOST()) {
                return Optional.empty();
            }
        }
        Operation op = wcs.findOperation("DescribeCoverage")
                .orElseThrow(() -> new IllegalArgumentException(
                        "The OperationsMetadata component shall contain three "
                                + "Operation instances with case-sensitive name values "
                                + "'GetCapabilities', 'DescribeCoverage', and “GetCoverage'"));
        return get ? op.getGet() : op.getPost();
    }

}
