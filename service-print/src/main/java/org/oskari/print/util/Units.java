package org.oskari.print.util;

public interface Units {

    public static final double MM_PER_INCH = 25.4;

    public static final double PDF_DPI = 72.0;

    // OGC Standardised pixel size
    public static final double OGC_PIXEL_SIZE_MM = 0.28;
    public static final double OGC_PIXEL_SIZE_METRE = OGC_PIXEL_SIZE_MM / 1000;
    public static final double OGC_DPI = MM_PER_INCH / OGC_PIXEL_SIZE_MM;

}
