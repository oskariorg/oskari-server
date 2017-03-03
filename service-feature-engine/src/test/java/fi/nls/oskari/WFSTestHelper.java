package fi.nls.oskari;

import com.google.common.io.ByteStreams;

import java.io.OutputStream;

/**
 * Created by SMAKINEN on 3.3.2017.
 */
public class WFSTestHelper {

    public static OutputStream getTestOutputStream() {
        // for console stream
        //return System.out;
        // for /dev/null stream
        return ByteStreams.nullOutputStream();
    }
}
