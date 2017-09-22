package org.oskari.wcs.request;

import org.oskari.wcs.response.Capabilities;

import java.util.Optional;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.oskari.ows.capabilities.Operation;

public class DescribeCoverage {

    /**
     * Check if the coverage appears in the GetCapabilities response
     * 
     * @param wcs
     *            capabilities of the service
     * @param coverageId
     *            of the coverage
     * @return true if exists, false if not
     */
    public static boolean coverageExists(Capabilities wcs, String coverageId) {
        Objects.requireNonNull(wcs);
        Objects.requireNonNull(coverageId);
        return wcs.getContents().getCoverageSummary().stream()
                .anyMatch(c -> coverageId.equals(c.getCoverageId()));
    }

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
        Map<String, String> params = new TreeMap<>();
        params.put("service", "WCS");
        params.put("version", "2.0.1");
        params.put("request", "DescribeCoverage");
        params.put("coverageId", coverageId);
        return params;
    }

    /**
     * Get the HTTP endpoint declared in GetCapabilities for DescribeCoverage
     * request
     * 
     * @param wcs
     *            capabilities of the service
     * @param get
     *            true if you want the GET endpoint, false if POST
     * @return the endpoint which might not exist for your binding or if the
     *         service did not declare support for DescribeCoverage at all
     */
    public static Optional<String> getDescribeCoverageEndPoint(Capabilities wcs, boolean get) {
        Optional<Operation> opt = getDescribeCoverage(wcs);
        if (opt.isPresent()) {
            Operation op = opt.get();
            String endPoint = get ? op.getGet() : op.getPost();
            if (endPoint != null && !endPoint.isEmpty()) {
                return Optional.of(endPoint);
            }
        }
        return Optional.empty();
    }

    private static Optional<Operation> getDescribeCoverage(Capabilities wcs) {
        Objects.requireNonNull(wcs);
        return wcs.getOperationsMetadata().getOperation().stream()
                .filter(op -> "DescribeCoverage".equals(op.getName())).findAny();
    }

}
