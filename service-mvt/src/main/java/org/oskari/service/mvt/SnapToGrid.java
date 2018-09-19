package org.oskari.service.mvt;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;

public class SnapToGrid implements CoordinateSequenceFilter {
    
    private final double tx;
    private final double ty;
    private final double sx;
    private final double sy;
    
    public SnapToGrid(double tx, double ty, double sx, double sy) {
        this.tx = tx;
        this.ty = ty;
        this.sx = sx;
        this.sy = sy;
    }

    @Override
    public void filter(CoordinateSequence seq, int i) {
        seq.setOrdinate(i, 0, Math.round(sx * (seq.getOrdinate(i, 0) - tx)));
        seq.setOrdinate(i, 1, Math.round(sy * (seq.getOrdinate(i, 1) - ty)));
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean isGeometryChanged() {
        return false;
    }

}
