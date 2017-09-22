package org.oskari.wcs.coverage.subtype;

import org.oskari.wcs.gml.Envelope;
import org.oskari.wcs.gml.RectifiedGrid;

import org.oskari.wcs.coverage.CoverageDescription;
import org.oskari.wcs.coverage.function.GridFunction;

public class RectifiedGridCoverage extends CoverageDescription {

    private final GridFunction gridFunction;
    private final RectifiedGrid domainSet;

    public RectifiedGridCoverage(String coverageId, Envelope boundedBy, String nativeFormat,
            GridFunction gridFunction, RectifiedGrid domainSet) {
        super(coverageId, boundedBy, nativeFormat);
        this.gridFunction = gridFunction;
        this.domainSet = domainSet;
    }

    public GridFunction getGridFunction() {
        return gridFunction;
    }

    public RectifiedGrid getDomainSet() {
        return domainSet;
    }

}
