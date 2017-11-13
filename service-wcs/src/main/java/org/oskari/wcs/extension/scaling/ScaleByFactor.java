package org.oskari.wcs.extension.scaling;

public class ScaleByFactor extends Scaling {

    public final double scaleFactor;

    public ScaleByFactor(double scaleFactor) {
        if (scaleFactor <= 0.0) {
            throw new IllegalArgumentException("scaleFactor must be positive");
        }
        this.scaleFactor = scaleFactor;
    }

}
