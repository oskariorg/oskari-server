package org.oskari.wcs.geotiff;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Very basic TIFF file reader
 *
 * For a nice summary of the TIFF file format
 * @see http://www.fileformat.info/format/tiff/egff.htm
 */
public class TIFFReader {

    private final ByteBuffer bb;
    private final List<IFD> ifds;

    public TIFFReader(byte[] b) throws IllegalArgumentException {
        this(ByteBuffer.wrap(b));
    }

    public TIFFReader(ByteBuffer bb) throws IllegalArgumentException {
        this.bb = bb;
        parseHeader();
        ifds = new ArrayList<>();
        parseIFDs();
    }

    private void parseHeader() throws IllegalArgumentException {
        ByteOrder order;

        bb.position(0);
        int bom = bb.getShort() & 0xffff;
        switch (bom) {
        case 0x4949:
            order = ByteOrder.LITTLE_ENDIAN;
            break;
        case 0x4D4D:
            order = ByteOrder.BIG_ENDIAN;
            break;
        default:
            throw new IllegalArgumentException("Invalid byte order value " + bom);
        }

        bb.order(order);

        if ((bb.getShort() & 0xffff) != 42) {
            throw new IllegalArgumentException("Magic version number wasn't 42!");
        }
    }

    private void parseIFDs() {
        // TIFF file can contain zero images
        // offset should be handled as uint32
        int off;
        while ((off = bb.getInt()) != 0) {
            ifds.add(new IFD(bb, off));
        }
    }

    public int getIFDCount() {
        return ifds.size();
    }

    public IFD getIFD(int i) {
        return ifds.get(i);
    }

    public void readStrip(int ifdIdx, int stripIdx, float[] dst) {
        IFD ifd = ifds.get(ifdIdx);

        if (ifd.getStripOffsets() == null) {
            throw new IllegalArgumentException("Specified IFD is not striped");
        }

        for (int sf : ifd.getSampleFormat()) {
            if (sf != 3) {
                throw new IllegalArgumentException("Specified IFD sampleFormat is not Float32");
            }
        }

        ByteBuffer.wrap(getStripData(ifd, stripIdx, null))
                .order(bb.order())
                .asFloatBuffer()
                .get(dst);
    }

    private byte[] getStripData(IFD ifd, int stripIdx, byte[] data)
            throws IllegalArgumentException {
        int off = ifd.getStripOffsets()[stripIdx];
        int len = ifd.getStripByteCounts()[stripIdx];

        int c = ifd.getCompression();
        switch (c) {
        case 1:
            if (data == null || data.length != len) {
                data = new byte[len];
            }
            bb.position(off);
            bb.get(data);
            break;
        default:
            throw new IllegalArgumentException("Can't decompress compression " + c);
        }

        return data;
    }

    public void readTile(int ifdIdx, int tileIdx, float[] dst)
            throws IllegalArgumentException {
        IFD ifd = ifds.get(ifdIdx);

        if (ifd.getTileOffsets() == null) {
            throw new IllegalArgumentException("Specified IFD is not tiled");
        }

        for (int sf : ifd.getSampleFormat()) {
            if (sf != 3) {
                throw new IllegalArgumentException("Specified IFD sampleFormat is not Float32");
            }
        }

        ByteBuffer.wrap(getTileData(ifd, tileIdx, null))
                .order(bb.order())
                .asFloatBuffer()
                .get(dst);
    }

    private byte[] getTileData(IFD ifd, int tileIdx, byte[] data)
            throws IllegalArgumentException {
        int off = ifd.getTileOffsets()[tileIdx];
        int len = ifd.getTileByteCounts()[tileIdx];

        int c = ifd.getCompression();
        switch (c) {
        case 1:
            if (data == null || data.length != len) {
                data = new byte[len];
            }
            bb.position(off);
            bb.get(data);
            break;
        default:
            throw new IllegalArgumentException("Can't decompress compression " + c);
        }

        return data;
    }

}
