package org.geotools.mif;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.geotools.mif.column.MIDColumn;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class MIFHeader {

    private static final char DEFAULT_DELIMITER = '\t';

    private final int version;
    private final Charset charset;
    private final char delimiter;
    private final CoordinateReferenceSystem coordSys;
    private final MIDColumn[] columns;

    public MIFHeader(File mif) throws IOException {
        try (BufferedReader r = Files.newBufferedReader(mif.toPath(), StandardCharsets.US_ASCII)) {
            this.version = parseVersion(r.readLine());
            this.charset = parseCharset(r.readLine());

            // Next headers are all optional (but they should appear in order)
            String line = r.readLine();
            if (line.startsWith("DELIMITER")) {
                this.delimiter = parseDelimiter(line);
                line = r.readLine();
            } else {
                this.delimiter = DEFAULT_DELIMITER;
            }
            if (line.startsWith("UNIQUE")) {
                line = r.readLine(); // Ignore, move to next line
            }
            if (line.startsWith("INDEX")) {
                line = r.readLine(); // Ignore, move to next line
            }
            if (line.startsWith("COORDSYS")) {
                coordSys = parseCoordSys(line, r);
            } else {
                // "When no COORDSYS clause is specified, data is assumed to be stored in longitude/latitude forms"
                coordSys = DefaultGeographicCRS.WGS84;
            }

            if (line.startsWith("TRANSFORM")) {
                // TODO: Must implement, can not ignore!
                line = r.readLine();
            }

            if (!line.startsWith("COLUMNS")) {
                throw new IllegalArgumentException("Could not find COLUMNS");
            }

            int numColumns = Integer.parseInt(line.substring("COLUMNS ".length()));
            this.columns = new MIDColumn[numColumns];
            for (int i = 0; i < numColumns; i++) {
                columns[i] = MIDColumn.create(r.readLine());
            }

            if (!"DATA".equals(r.readLine())) {
                throw new IllegalArgumentException("Could not find DATA");
            }
        }
    }

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

    public MIDColumn[] getColumns() {
        return columns;
    }

    private int parseVersion(String line) {
        if (line.startsWith("VERSION ")) {
            throw new IllegalArgumentException("Missing VERSION");
        }
        return Integer.parseInt(line.substring("VERSION ".length()));
    }

    private Charset parseCharset(String line) {
        if (line.startsWith("Charset \"")) {
            throw new IllegalArgumentException("Missing Charset");
        }
        int i = "Charset \"".length() + 1;
        int j = line.indexOf('"', i);
        String charset = line.substring(i, j);
        switch (charset) {
        case "WindowsLatin1":
            return StandardCharsets.ISO_8859_1;
        case "MacRoman":
            return Charset.forName("MacRoman");
        case "Neutral":
            return StandardCharsets.US_ASCII;
        default:
            throw new IllegalArgumentException("Unknown charset");
        }
    }

    private char parseDelimiter(String line) {
        return line.charAt("DELIMITER \"".length() + 1);
    }

    private CoordinateReferenceSystem parseCoordSys(String line, BufferedReader r) {
        // TODO: IMPLEMENT!
        return null;
    }

}
