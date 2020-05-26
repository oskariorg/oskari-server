package org.geotools.mif.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;

/**
 * Peek into BufferedReader
 */
public class QueueBufferedReader implements Closeable {

    private String rem;
    private BufferedReader reader;

    public QueueBufferedReader(BufferedReader reader) {
        this.rem = null;
        this.reader = reader;
    }

    public String peek() throws IOException {
        if (rem == null) {
            rem = reader.readLine();
        }
        return rem;
    }

    public String poll() throws IOException {
        if (rem != null) {
            String tmp = rem;
            rem = null;
            return tmp;
        }
        return reader.readLine();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

}
