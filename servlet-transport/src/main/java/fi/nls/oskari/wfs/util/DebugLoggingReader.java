package fi.nls.oskari.wfs.util;

import java.io.IOException;
import java.io.Reader;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class DebugLoggingReader extends Reader {

    private static final Logger LOG = LogFactory.getLogger(DebugLoggingReader.class);

    private final Reader in;
    private final char[] arr;
    private final int len;
    private int pos;

    public DebugLoggingReader(Reader in) throws IOException {
        char[] buf = new char[8192];
        int read = 0;
        while (true) {
            if (read == buf.length) {
                char[] tmp = new char[buf.length * 2];
                System.arraycopy(buf, 0, tmp, 0, buf.length);
                buf = tmp;
            }
            int n = in.read(buf, read, buf.length - read);
            if (n == -1) {
                break;
            }
            read += n;
        }
        this.in = in;
        this.arr = buf;
        this.len = read;
        LOG.debug(new String(arr, 0, len));
    }

    @Override
    public int read() {
        if (pos == len) {
            return -1;
        }
        return arr[pos++] & 0xFF;
    }

    @Override
    public int read(char[] cbuf) {
        return read(cbuf, 0, cbuf.length);
    }

    @Override
    public int read(char[] cbuf, int off, int len) {
        int left = this.len - this.pos;
        if (left == 0) {
            return -1;
        }
        if (left < len) {
            len = left;
        }
        System.arraycopy(arr, pos, cbuf, off, len);
        pos += len;
        return len;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

}
