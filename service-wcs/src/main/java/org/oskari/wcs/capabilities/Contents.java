package org.oskari.wcs.capabilities;

import java.util.List;

public class Contents {

    private final List<CoverageSummary> coverageSummary;

    public Contents(List<CoverageSummary> coverageSummary) {
        this.coverageSummary = coverageSummary;
    }

    public List<CoverageSummary> getCoverageSummary() {
        return coverageSummary;
    }

}
