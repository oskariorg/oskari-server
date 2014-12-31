package fi.nls.oskari.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created by TMIKKOLAINEN on 30.12.2014.
 */
public interface TabularFileStreamer {
    public void writeToStream(String[] headers, Object[][] data, Map<String, Object> additionalFields, OutputStream out) throws IOException;
}
