package org.oskari.wcs.extension.scaling;

public class TargetAxisExtent extends AxisBasedScaling {

    public final double high;
    public final double low;

    protected TargetAxisExtent(String axis, double high, double low) {
        super(axis);
        if (high < low) {
            throw new IllegalArgumentException("high must be greater than or equal to to low");
        }
        this.high = high;
        this.low = low;
    }

}
