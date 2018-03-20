package org.oskari.wcs.gml;

public class GridEnvelope {

    private final int[] low;
    private final int[] high;

    public GridEnvelope(int[] low, int[] high) {
        this.low = low;
        this.high = high;
    }

    public int[] getLow() {
        return low;
    }

    public int[] getHigh() {
        return high;
    }

}
