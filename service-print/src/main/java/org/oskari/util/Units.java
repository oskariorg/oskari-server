package org.oskari.util;

public class Units {
    
    private Units() {}

    public static final double MM_PER_INCH = 25.4;

    public static final double METRES_PER_MILE = 1609.344;
    public static final double METRES_PER_FOOT = 0.3048;

    public static final double PDF_DPI = 72.0;

    // OGC Standardised pixel size
    public static final double OGC_PIXEL_SIZE_MM = 0.28;
    public static final double OGC_PIXEL_SIZE_METRE = OGC_PIXEL_SIZE_MM / 1000;
    public static final double OGC_DPI = MM_PER_INCH / OGC_PIXEL_SIZE_MM;

}
