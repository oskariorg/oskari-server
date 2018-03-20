package org.oskari.wcs.extension.scaling;

import java.util.HashSet;
import java.util.Set;

public abstract class Scaling {

    protected void checkAxes(String[] axes) {
        int n = axes.length;
        if (n == 0) {
            throw new IllegalArgumentException("Number of axes must be positive");
        }
        Set<String> unique = new HashSet<>();
        for (String axis : axes) {
            unique.add(axis);
        }
        if (unique.size() != axes.length) {
            throw new IllegalArgumentException("Axis names must be unique");
        }
    }

    protected void checkAxes(AxisBasedScaling[] axisBasedScalings) {
        int n = axisBasedScalings.length;
        if (n == 0) {
            throw new IllegalArgumentException("Number of axes must be positive");
        }
        Set<String> unique = new HashSet<>();
        for (AxisBasedScaling axis : axisBasedScalings) {
            unique.add(axis.axis);
        }
        if (unique.size() != n) {
            throw new IllegalArgumentException("Axis names must be unique");
        }
    }

}
