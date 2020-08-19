package org.geotools.mif.util;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;

public class TransformFilter implements CoordinateSequenceFilter {

    private final double sx;
    private final double sy;
    private final double tx;
    private final double ty;

    public TransformFilter(double sx, double sy, double tx, double ty) {
        this.sx = sx;
        this.sy = sy;
        this.tx = tx;
        this.ty = ty;
    }

    @Override
    public void filter(CoordinateSequence seq, int i) {
        double x = seq.getOrdinate(i, 0);
        double y = seq.getOrdinate(i, 1);
        x = sx * x + tx;
        y = sy * y + ty;
        seq.setOrdinate(i, 0, x);
        seq.setOrdinate(i, 1, y);
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean isGeometryChanged() {
        return true;
    }

}
