package org.oskari.wcs.capabilities;

import org.oskari.ows.capabilities.OperationsMetadata;
import org.oskari.ows.capabilities.ServiceIdentification;

public class WCSCapabilities {

    private final String updateSequence;
    private final ServiceIdentification serviceIdentification;
    private final OperationsMetadata operationsMetadata;
    private final ServiceMetadata serviceMetadata;
    private final Contents contents;

    public WCSCapabilities(String updateSequence, ServiceIdentification serviceIdentification,
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

}
