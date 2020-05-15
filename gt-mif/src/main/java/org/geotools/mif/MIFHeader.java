package org.geotools.mif;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.geotools.mif.column.MIDColumn;
import org.geotools.mif.util.MIFUtil;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class MIFHeader {

    private final int version;
    private final Charset charset;
    private final char delimiter;
    private final CoordinateReferenceSystem coordSys;
    private final double[] transform;
    private final MIDColumn[] columns;

    public int getVersion() {
        return version;
    }

    public Charset getCharset() {
        return charset;
    }

    public char getDelimiter() {
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

    public MIFHeader(File mif) throws IOException {
        int version = 0;
        Charset charset = null;
        char delimiter = '\t';
        // "When no COORDSYS clause is specified, data is assumed to be stored in longitude/latitude forms"
        CoordinateReferenceSystem coordSys = DefaultGeographicCRS.WGS84;
        double[] transform = new double[] { 1, 1, 0, 0 };
        MIDColumn[] columns = null;
        
        boolean foundData = false;
        
        try (BufferedReader r = Files.newBufferedReader(mif.toPath(), StandardCharsets.US_ASCII)) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (MIFUtil.startsWithIgnoreCase(line, "DATA")) {
                    foundData = true;
                    break;
                } else if (MIFUtil.startsWithIgnoreCase(line, "VERSION")) {
                    version = parseVersion(line);
                } else if (MIFUtil.startsWithIgnoreCase(line, "CHARSET")) {
                    charset = parseCharset(line);
                } else if (MIFUtil.startsWithIgnoreCase(line, "DELIMITER")) {
                    delimiter = parseDelimiter(line);
                } else if (MIFUtil.startsWithIgnoreCase(line, "COORDSYS")) {
                    coordSys = parseCoordSys(line, r);
                } else if (MIFUtil.startsWithIgnoreCase(line, "TRANSFORM")) {
                    transform = parseTransform(line);
                } else if (MIFUtil.startsWithIgnoreCase(line, "COLUMNS")) {
                    columns = parseColumns(line, r);
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
        return MIFUtil.parseInt(line.substring("VERSION ".length()));
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

    private char parseDelimiter(String line) {
        // DELIMITER "<c>"
        int i = line.indexOf('"');
        return line.charAt(i + 1);
    }

    private CoordinateReferenceSystem parseCoordSys(String line, BufferedReader r) {
        // TODO: IMPLEMENT!
        return null;
    }

    private double[] parseTransform(String line) {
        String transform = line.substring("TRANSFORM ".length()).trim();
        String[] a = transform.split(",");
        double sx = Double.parseDouble(a[0]);
        double sy = Double.parseDouble(a[1]);
        double tx = Double.parseDouble(a[2]);
        double ty = Double.parseDouble(a[3]);

        // The zeroes instruct MapInfo to ignore that parameter
        if (sx == 0.0) {
            sx = 1.0;
        }
        if (sy == 0.0) {
            sy = 0.0;
        }

        double[] t = { sx, sy, tx, ty };
        return t;
    }

    private MIDColumn[] parseColumns(String line, BufferedReader r) throws IOException {
        int numColumns = MIFUtil.parseInt(line.substring("COLUMNS ".length()));
        MIDColumn[] columns = new MIDColumn[numColumns];
        for (int i = 0; i < numColumns; i++) {
            columns[i] = MIDColumn.create(r.readLine());
        }
        return columns;
    }

}
