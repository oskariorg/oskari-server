package org.geotools.mif;

import static org.geotools.mif.util.MIFUtil.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

import org.geotools.mif.column.MIDColumn;
import org.geotools.mif.util.QueueBufferedReader;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class MIFHeader {

    private final int version;
    private final Charset charset;
    private final String delimiter;
    private final CoordinateReferenceSystem coordSys;
    private final double[] transform;
    private final MIDColumn[] columns;

    public int getVersion() {
        return version;
    }

    public Charset getCharset() {
        return charset;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public CoordinateReferenceSystem getCoordSys() {
        return coordSys;
    }

    public double[] getTransform() {
        return transform;
    }

    public MIDColumn[] getColumns() {
        return columns;
    }

    public MIFHeader(File mif, CoordinateReferenceSystem forceCRS) throws IOException {
        int version = 0;
        Charset charset = null;
        String delimiter = "\t";
        // "When no COORDSYS clause is specified, data is assumed to be stored in longitude/latitude forms"
        CoordinateReferenceSystem coordSys = forceCRS == null ? DefaultGeographicCRS.WGS84 : forceCRS;
        double[] transform = new double[] { 1, 1, 0, 0 };
        MIDColumn[] columns = null;

        boolean foundData = false;

        try (QueueBufferedReader q = new QueueBufferedReader(Files.newBufferedReader(mif.toPath(), StandardCharsets.US_ASCII))) {
            String line;
            while ((line = q.poll()) != null) {
                line = line.trim();
                if (startsWithIgnoreCase(line, "DATA")) {
                    foundData = true;
                    break;
                } else if (startsWithIgnoreCase(line, "VERSION")) {
                    version = parseVersion(line);
                } else if (startsWithIgnoreCase(line, "CHARSET")) {
                    charset = parseCharset(line);
                } else if (startsWithIgnoreCase(line, "DELIMITER")) {
                    delimiter = parseDelimiter(line);
                } else if (startsWithIgnoreCase(line, "COORDSYS")) {
                    coordSys = parseCoordSys(line, q);
                } else if (startsWithIgnoreCase(line, "TRANSFORM")) {
                    transform = parseTransform(line);
                } else if (startsWithIgnoreCase(line, "COLUMNS")) {
                    columns = parseColumns(line, q);
                }
            }
        }

        if (version == 0) {
            throw new IllegalArgumentException("Could not find VERSION header");
        }
        if (charset == null) {
            throw new IllegalArgumentException("Could not find Charset header");
        }
        if (columns == null) {
            throw new IllegalArgumentException("Could not find COLUMNS header");
        }
        if (coordSys == null) {
            if (forceCRS == null) {
                throw new IllegalArgumentException("Could not parse CoordSys header and no force CRS available");
            }
            coordSys = forceCRS;
        }
        if (!foundData) {
            throw new IllegalArgumentException("Could not find DATA header");
        }

        this.version = version;
        this.charset = charset;
        this.delimiter = delimiter;
        this.coordSys = coordSys;
        this.transform = transform;
        this.columns = columns;
    }

    private int parseVersion(String line) {
        // VERSION n
        String[] a = line.split("\\s+");
        return Integer.parseInt(a[1]);
    }

    private Charset parseCharset(String line) {
        // Charset "characterSetName"
        int i = line.indexOf('"');
        int j = line.indexOf('"', i + 1);
        if (i < 0 || j < 0) {
            throw new IllegalArgumentException("Invalid Charset header");
        }
        String charset = line.substring(i + 1, j).toLowerCase();
        switch (charset) {
        case "windowslatin1":
            return StandardCharsets.ISO_8859_1;
        case "macroman":
            return Charset.forName("MacRoman");
        case "neutral":
            return StandardCharsets.US_ASCII;
        default:
            throw new IllegalArgumentException("Unknown charset");
        }
    }

    private String parseDelimiter(String line) {
        // DELIMITER "<c>"
        int i = line.indexOf('"');
        int j = line.indexOf('"', i + 1);
        return line.substring(i + 1, j);
    }

    private CoordinateReferenceSystem parseCoordSys(String line, QueueBufferedReader q) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(line);
        while (true) {
            line = q.peek().trim();
            if (startsWithIgnoreCase(line, "TRANSFORM") || startsWithIgnoreCase(line, "COLUMNS")) {
                break;
            }
            sb.append(' ');
            sb.append(line);
            q.poll();
        }
        String coordSys = sb.toString();
        try {
            return parseCoordSys(coordSys);
        } catch (FactoryException e) {
            throw new IOException(e);
        }
    }

    private CoordinateReferenceSystem parseCoordSys(String coordSys) throws FactoryException {
        if (startsWithIgnoreCase(coordSys, "COORDSYS")) {
            coordSys = coordSys.substring("COORDSYS".length() + 1);
        }
        coordSys = coordSys.replace(',', ' ');
        String[] parts = coordSys.split("\\s+");

        int boundsIndex = indexOfIgnoreCase(parts, "Bounds");
        if (boundsIndex > 0) {
            parts = Arrays.copyOf(parts, boundsIndex);
        }

        if ("Earth".equalsIgnoreCase(parts[0])) {
            if (parts.length >= 5 && "Projection".equalsIgnoreCase(parts[1])) {
                // [ Projection type, datum, uniname [, origin latitude ], ... ]
                int type = Integer.parseInt(parts[2]);
                int datumId = Integer.parseInt(parts[3]);
                // String unitname = parts[4];

                // Projection numbers in the MAPINFOW.PRJ may be modified by the addition of a constant value to the
                // base number listed in the Projection table, above. Valid values and their meanings are in the next table:
                // 1000, System has affine transformations
                // 2000, System has explicit bounds
                // 3000, System with both affine and bounds
                // -- just ignore these
                type %= 1000;

                int n = parts.length - 5;
                String[] projectionParams = new String[n];
                for (int i = 0; i < n; i++) {
                    projectionParams[i] = parts[5 + i];
                }
                MIFProjection projection = MIFProjection.find(type);
                MIFDatum datum = MIFDatum.find(datumId);
                return projection.toCRS(datum, projectionParams);
            }
        }

        return null;
    }

    private int indexOfIgnoreCase(String[] arr, String key) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equalsIgnoreCase(key)) {
                return i;
            }
        }
        return -1;
    }

    private double[] parseTransform(String line) {
        // TRANSFORM x1,y1,x2,y2
        String[] a = line.split("\\s+");
        a = a[1].split(",");
        double sx = Double.parseDouble(a[0].trim());
        double sy = Double.parseDouble(a[1].trim());
        double tx = Double.parseDouble(a[2].trim());
        double ty = Double.parseDouble(a[3].trim());

        // The zeroes instruct MapInfo to ignore that parameter
        if (sx == 0.0) {
            sx = 1.0;
        }
        if (sy == 0.0) {
            sy = 1.0;
        }

        double[] t = { sx, sy, tx, ty };
        return t;
    }

    private MIDColumn[] parseColumns(String line, QueueBufferedReader q) throws IOException {
        // COLUMNS 3
        String[] a = line.split("\\s+");
        int numColumns = Integer.parseInt(a[1]);
        MIDColumn[] columns = new MIDColumn[numColumns];
        for (int i = 0; i < numColumns; i++) {
            columns[i] = MIDColumn.create(q.poll());
        }
        return columns;
    }

}
