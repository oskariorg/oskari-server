package org.oskari.wcs.geotiff;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IFD {

    private int width;
    private int height;
    private int[] bitsPerSample;
    private int samplesPerPixel;
    private int photometricInterpration;
    private int compression;

    private int rowsPerStrip;
    private int[] stripOffsets;
    private int[] stripByteCounts;
    private int orientation;

    private int tileWidth;
    private int tileHeight;
    private int[] tileOffsets;
    private int[] tileByteCounts;

    private int planarConfiguration;
    private int predictor;

    private long xResolution;
    private long yResolution;
    private int resolutionUnit;

    private int[] sampleFormat;

    private int subfileType;

    private int minSampleValue;
    private int maxSampleValue;
    private int sMinSampleValue;
    private int sMaxSampleValue;

    private int[] geoKeyDirectory;
    private double[] modelTransformation;


    public IFD(ByteBuffer bb, int pos) {
        // Set the initial position
        bb.position(pos);
        final int tagCount = bb.getShort() & 0xffff;
        for (int i = 0; i < tagCount; i++) {
            // Mark current position
            pos = bb.position();
            // id and type are uint16
            int id = bb.getShort() & 0xFFFF;
            int type = bb.getShort() & 0xFFFF;
            int count = bb.getInt();
            int offset = bb.getInt();
            parseTag(bb, id, type, count, offset);
            // Move to next position
            bb.position(pos + 12);
        }
    }

    private void parseTag(ByteBuffer bb, int id, int type, int count, int offset) {
        int typeLen = getFieldTypeLength(type);
        int valueSize = typeLen * count;
        if (valueSize <= 4) {
            int value;
            if (bb.order() == ByteOrder.BIG_ENDIAN) {
                int shift = (4 - typeLen) * 8;
                value = offset >>> shift;
            } else {
                value = offset;
            }
            setValue(id, value);
        } else {
            bb.position(offset);
            parseValue(bb, id, type, count);
        }
    }

    private void parseValue(ByteBuffer bb, int id, int type, int count) {
        if (count == 1) {
            parseSingleValue(bb, id, type);
        } else {
            parseMultipleValues(bb, id, type, count);
        }
    }

    private void parseSingleValue(ByteBuffer bb, int id, int type) {
        // Ignore types of <= 4 bytes
        switch (type) {
        case 5:
        case 10:
            setValue(id, bb.getLong());
            break;
        }
    }

    private void parseMultipleValues(ByteBuffer bb, int id, int type, int count) {
        switch (type) {
        case 3:
            setValue(id, getUShortArray(bb, count));
            break;
        case 4:
        case 9:
            setValue(id, getIntArray(bb, count));
            break;
        case 12:
            setValue(id, getDoubleArray(bb, count));
            break;
        }
    }

    private static int[] getUShortArray(ByteBuffer bb, int count) {
        int[] arr = new int[count];
        for (int i = 0; i < count; i++) {
            arr[i] = bb.getShort() & 0xFFFF;
        }
        return arr;
    }

    private static int[] getIntArray(ByteBuffer bb, int count) {
        int[] arr = new int[count];
        bb.asIntBuffer().get(arr);
        return arr;
    }

    private static double[] getDoubleArray(ByteBuffer bb, int count) {
        double[] arr = new double[count];
        bb.asDoubleBuffer().get(arr);
        return arr;
    }

    private static final int getFieldTypeLength(int type) throws IllegalArgumentException {
        switch (type) {
        case 1:
        case 2:
        case 6:
        case 7:
            return 1;
        case 3:
        case 8:
            return 2;
        case 4:
        case 9:
        case 11:
            return 4;
        case 5:
        case 10:
        case 12:
            return 8;
        }
        throw new IllegalArgumentException("Invalid field type " + type);
    }

    private void setValue(int id, int value) {
        switch (id) {
        case 254:
            subfileType = value;
            break;
        case 256:
            width = value;
            break;
        case 257:
            height = value;
            break;
        case 258:
            bitsPerSample = new int[] { value };
            break;
        case 259:
            compression = value;
            break;
        case 262:
            photometricInterpration = value;
            break;
        case 273:
            stripOffsets = new int[] { value };
            break;
        case 274:
            orientation = value; 
            break;
        case 277:
            samplesPerPixel = value;
            break;
        case 278:
            rowsPerStrip = value;
            break;
        case 279:
            stripByteCounts = new int[] { value };
            break;
        case 280:
            minSampleValue = value;
            break;
        case 281:
            maxSampleValue = value;
            break;
        case 284:
            planarConfiguration = value;
            break;
        case 296:
            resolutionUnit = value;
            break;
        case 317:
            predictor = value;
            break;
        case 322:
            tileWidth = value;
            break;
        case 323:
            tileHeight = value;
            break;
        case 324:
            tileOffsets = new int[] { value };
            break;
        case 325:
            tileByteCounts = new int[] { value };
            break;
        case 339:
            sampleFormat = new int[] { value };
            break;
        case 340:
            sMinSampleValue = value;
            break;
        case 341:
            sMaxSampleValue = value;
            break;
        }
    }

    private void setValue(int id, long value) {
        switch (id) {
        case 282:
            xResolution = value;
            break;
        case 283:
            yResolution = value;
            break;
        }
    }

    private void setValue(int id, int[] arr) {
        switch (id) {
        case 258:
            bitsPerSample = arr;
            break;
        case 273:
            stripOffsets = arr;
            break;
        case 279:
            stripByteCounts = arr;
            break;
        case 324:
            tileOffsets = arr;
            break;
        case 325:
            tileByteCounts = arr;
            break;
        case 339:
            sampleFormat = arr;
            break;
        case 34735:
            geoKeyDirectory = arr;
            break;
        }
    }

    private void setValue(int id, double[] arr) {
        switch (id) {
        case 34264:
            modelTransformation = arr;
            break;
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getBitsPerSample() {
        return bitsPerSample;
    }

    public int getSamplesPerPixel() {
        return samplesPerPixel;
    }

    public int getPhotometricInterpration() {
        return photometricInterpration;
    }

    public int getCompression() {
        return compression;
    }

    public int getRowsPerStrip() {
        return rowsPerStrip;
    }

    public int[] getStripOffsets() {
        return stripOffsets;
    }

    public int[] getStripByteCounts() {
        return stripByteCounts;
    }

    public int getOrientation() {
        return orientation;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public int[] getTileOffsets() {
        return tileOffsets;
    }

    public int[] getTileByteCounts() {
        return tileByteCounts;
    }

    public int getPlanarConfiguration() {
        return planarConfiguration;
    }

    public int getPredictor() {
        return predictor;
    }

    public long getXResolution() {
        return xResolution;
    }

    public long getYResolution() {
        return yResolution;
    }

    public int getResolutionUnit() {
        return resolutionUnit;
    }

    public int[] getSampleFormat() {
        return sampleFormat;
    }

    public int getSubfileType() {
        return subfileType;
    }

    public int getMinSampleValue() {
        return minSampleValue;
    }

    public int getMaxSampleValue() {
        return maxSampleValue;
    }

    public int getSMinSampleValue() {
        return sMinSampleValue;
    }

    public int getSMaxSampleValue() {
        return sMaxSampleValue;
    }

    public int[] getGeoKeyDirectory() {
        return geoKeyDirectory;
    }

    public double[] getModelTransformation() {
        return modelTransformation;
    }

}
