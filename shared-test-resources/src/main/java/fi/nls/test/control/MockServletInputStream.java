package fi.nls.test.control;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by SMAKINEN on 9.7.2015.
 */
public class MockServletInputStream extends ServletInputStream {

    InputStream payload;

    public MockServletInputStream(final InputStream payload) {
        this.payload = payload;
    }
    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int read() throws IOException {
        return payload.read();
    }
}
