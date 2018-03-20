package org.oskari.wcs.capabilities;

import java.util.Optional;
import org.oskari.wcs.WCS;
import org.oskari.wcs.extension.Interpolation;

public class Capabilities {

    private final String updateSequence;
    private final ServiceIdentification serviceIdentification;
    private final OperationsMetadata operationsMetadata;
    private final ServiceMetadata serviceMetadata;
    private final Contents contents;

    public Capabilities(String updateSequence, ServiceIdentification serviceIdentification,
            OperationsMetadata operationsMetadata, ServiceMetadata serviceMetadata,
            Contents contents) {
        this.updateSequence = updateSequence;
        this.serviceIdentification = serviceIdentification;
        this.operationsMetadata = operationsMetadata;
        this.serviceMetadata = serviceMetadata;
        this.contents = contents;
    }

    public String getUpdateSequence() {
        return updateSequence;
    }

    public ServiceIdentification getServiceIdentification() {
        return serviceIdentification;
    }

    public OperationsMetadata getOperationsMetadata() {
        return operationsMetadata;
    }

    public ServiceMetadata getServiceMetadata() {
        return serviceMetadata;
    }

    public Contents getContents() {
        return contents;
    }

    /**
     * Check if the coverage appears in the GetCapabilities response
     */
    public boolean servesCoverage(String coverageId) {
        if (coverageId == null || coverageId.isEmpty()) {
            return false;
        }
        return contents.getCoverageSummary().stream()
                .anyMatch(c -> coverageId.equals(c.getCoverageId()));
    }

    /**
     * Check if the format appears in the GetCapabilities response
     */
    public boolean supportsFormat(String format) {
        if (format == null || format.isEmpty()) {
            return false;
        }
        return serviceMetadata.getSupportedFormats().stream()
                .anyMatch(f -> format.equals(f));
    }

    public boolean supportsGET() {
        return supportsProfile(WCS.PROFILE_KVP);
    }

    public boolean supportsPOST() {
        return supportsProfile(WCS.PROFILE_XML_POST);
    }

    public boolean supportsProfile(String profile) {
        if (profile == null || profile.isEmpty()) {
            return false;
        }
        return serviceIdentification.getProfile().stream()
                .anyMatch(p -> profile.equals(p));
    }

    public Optional<Operation> findOperation(String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }
        return operationsMetadata.getOperation().stream()
                .filter(o -> name.equals(o.getName()))
                .findAny();
    }

    public boolean supportsInterpolation() {
        return serviceIdentification.getProfile().stream()
                .anyMatch(p -> WCS.PROFILE_EXT_INTERP.equals(p));
    }

    public boolean supportsInterpolation(Interpolation interp) {
        if (interp == null) {
            return false;
        }
        String interpProfile = interp.profile;
        return serviceIdentification.getProfile().stream()
                .anyMatch(p -> interpProfile.equals(p));
    }

    public boolean supportsCRS() {
        return serviceIdentification.getProfile().stream()
                .anyMatch(p -> WCS.PROFILE_EXT_CRS.equals(p));
    }

    public boolean supportsCRS(String crs) {
        return serviceIdentification.getProfile().stream()
                .anyMatch(p -> WCS.PROFILE_EXT_CRS.equals(p));
    }

}
