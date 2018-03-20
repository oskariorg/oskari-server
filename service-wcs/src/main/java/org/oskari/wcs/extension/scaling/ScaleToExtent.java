package org.oskari.wcs.extension.scaling;

public class ScaleToExtent extends Scaling {

    public final TargetAxisExtent[] axisExtents;

    public ScaleToExtent(String[] axes, double[] highs, double[] lows) {
        checkAxes(axes);
        int n = axes.length;
        if (n != highs.length) {
            throw new IllegalArgumentException("Number of axes must match number highs");
        }
        if (n != lows.length) {
            throw new IllegalArgumentException("Number of axes must match number lows");
        }
        axisExtents = new TargetAxisExtent[n];
        for (int i = 0; i < n; i++) {
            axisExtents[i] = new TargetAxisExtent(axes[i], highs[i], lows[i]);
        }
    }

    public ScaleToExtent(TargetAxisExtent[] axisExtents) {
        checkAxes(axisExtents);
        this.axisExtents = axisExtents;
    }

}
