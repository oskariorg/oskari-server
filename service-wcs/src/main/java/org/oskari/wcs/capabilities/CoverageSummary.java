package org.oskari.wcs.capabilities;

import java.util.List;

public class CoverageSummary {

    private final String coverageId;
    private final String coverageSubType;
    private final BoundingBox wgs84BoundingBox;
    private final List<BoundingBox> boundingBoxes;

    public CoverageSummary(String coverageId, String coverageSubType, BoundingBox wgs84BoundingBox,
            List<BoundingBox> boundingBoxes) {
        this.coverageId = coverageId;
        this.coverageSubType = coverageSubType;
        this.wgs84BoundingBox = wgs84BoundingBox;
        this.boundingBoxes = boundingBoxes;
    }

    public String getCoverageId() {
        return coverageId;
    }

    public String getCoverageSubType() {
        return coverageSubType;
    }

    public BoundingBox getWgs84BoundingBox() {
        return wgs84BoundingBox;
    }

    public List<BoundingBox> getBoundingBoxes() {
        return boundingBoxes;
    }

}
