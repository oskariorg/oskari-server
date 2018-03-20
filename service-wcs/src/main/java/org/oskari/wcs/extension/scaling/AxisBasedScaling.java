package org.oskari.wcs.extension.scaling;

public abstract class AxisBasedScaling {

    public final String axis;

    protected AxisBasedScaling(String axis) {
        if (axis == null || axis.isEmpty()) {
            throw new IllegalArgumentException("Axis names must be non-null and non-empty");
        }
        this.axis = axis;
    }

}
