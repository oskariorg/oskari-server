package org.oskari.wcs.extension.scaling;

public class ScaleAxis extends AxisBasedScaling {

    public final double scaleFactor;

    protected ScaleAxis(String axis, double scaleFactor) {
        super(axis);
        if (scaleFactor <= 0.0) {
            throw new IllegalArgumentException("scaleFactor must be positive");
        }
        this.scaleFactor = scaleFactor;
    }

}
