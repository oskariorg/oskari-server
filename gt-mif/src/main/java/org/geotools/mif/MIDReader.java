package org.geotools.mif;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.mif.column.MIDColumn;
import org.geotools.mif.util.FieldSplitter;

/**
 * Read MapInfo MID File
 *
 * The MID file contains data, one record of data per row, delimited by the character
 * specified in the delimiter statement. The default delimiter is Tab. Each row in the MID
 * file is associated with a corresponding object in the MIF file; first row with first object,
 * second row with second object.
 * If delimiter character is included as part of the data in a field, enclose the field in
 * quotation marks.
 *
 * The MID file is an optional file. When there is no MID file, all fields are blank.
 */
public class MIDReader implements Closeable {

    private final BufferedReader r;
    private final MIFHeader header;

    public MIDReader(File mid, MIFHeader header) throws IOException {
        this.r = Files.newBufferedReader(mid.toPath(), header.getCharset());
        this.header = header;
    }

    public void next(SimpleFeatureBuilder builder) throws IOException {
        String line = r.readLine();
        FieldSplitter split = new FieldSplitter(line, header.getDelimiter());
        for (MIDColumn col : header.getColumns()) {
            builder.set(col.getName(), col.parse(split.next()));
        }
    }

    @Override
    public void close() {
        try {
            r.close();
        } catch (IOException ignore) {
            // NOP
        }
    }

}
