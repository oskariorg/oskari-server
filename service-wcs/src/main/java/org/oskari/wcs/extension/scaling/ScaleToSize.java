package org.oskari.wcs.extension.scaling;

public class ScaleToSize extends Scaling {

    public final TargetAxisSize[] targetAxisSizes;

    protected ScaleToSize(String[] axes, int[] targetSizes) {
        checkAxes(axes);
        int n = axes.length;
        if (n != targetSizes.length) {
            throw new IllegalArgumentException("Number of axes must match number of targetSizes");
        }
        targetAxisSizes = new TargetAxisSize[n];
        for (int i = 0; i < n; i++) {
            targetAxisSizes[i] = new TargetAxisSize(axes[i], targetSizes[i]);
        }
    }

    public ScaleToSize(TargetAxisSize[] targetAxisSizes) {
        checkAxes(targetAxisSizes);
        this.targetAxisSizes = targetAxisSizes;
    }

}
