package org.oskari.wcs.extension.scaling;

public class TargetAxisSize extends AxisBasedScaling {

    public final int targetSize;

    protected TargetAxisSize(String axis, int targetSize) {
        super(axis);
        if (targetSize < 1) {
            throw new IllegalArgumentException("targetSize must be positive");
        }
        this.targetSize = targetSize;
    }

}
