package org.oskari.wcs.extension.scaling;

public class ScaleAxesByFactor extends Scaling {

    public final ScaleAxis[] scaleAxes;

    public ScaleAxesByFactor(String[] axes, double[] factors) {
        checkAxes(axes);
        int n = axes.length;
        if (n != factors.length) {
            throw new IllegalArgumentException("Number of axes must match number of scaleFactors");
        }
        scaleAxes = new ScaleAxis[n];
        for (int i = 0; i < n; i++) {
            scaleAxes[i] = new ScaleAxis(axes[i], factors[i]);
        }
    }

    public ScaleAxesByFactor(ScaleAxis[] scaleAxes) {
        checkAxes(scaleAxes);
        this.scaleAxes = scaleAxes;
    }

}
