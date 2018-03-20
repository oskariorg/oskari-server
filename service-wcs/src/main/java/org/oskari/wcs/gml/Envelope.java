package org.oskari.wcs.gml;

public class Envelope {

    private final String srsName;
    private final int srsDimension;
    private final String[] axisLabels;
    private final String[] uomLabels;
    private final double[] lowerCorner;
    private final double[] upperCorner;

    public Envelope(String srsName, int srsDimension, String[] axisLabels, String[] uomLabels,
            double[] lowerCorner, double[] upperCorner) {
        this.srsName = srsName;
        this.srsDimension = srsDimension;
        this.axisLabels = axisLabels;
        this.uomLabels = uomLabels;
        this.lowerCorner = lowerCorner;
        this.upperCorner = upperCorner;
    }

    public String getSrsName() {
        return srsName;
    }

    public int getSrsDimension() {
        return srsDimension;
    }

    public String[] getAxisLabels() {
        return axisLabels;
    }

    public String[] getUomLabels() {
        return uomLabels;
    }

    public double[] getLowerCorner() {
        return lowerCorner;
    }

    public double[] getUpperCorner() {
        return upperCorner;
    }

}
