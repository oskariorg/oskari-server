package org.oskari.wcs.response;

import org.oskari.wcs.capabilities.Contents;
import org.oskari.wcs.capabilities.ServiceMetadata;

import org.oskari.ows.capabilities.OperationsMetadata;
import org.oskari.ows.capabilities.ServiceIdentification;

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

}
