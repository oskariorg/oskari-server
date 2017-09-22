package org.oskari.wcs.capabilities;

import java.util.List;

public class OperationsMetadata {

    private List<Operation> operation;

    public OperationsMetadata(List<Operation> operation) {
        this.operation = operation;
    }

    public List<Operation> getOperation() {
        return operation;
    }

}
