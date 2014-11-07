package fi.nls.oskari.fe.output;

import java.io.OutputStream;

public interface OutputStreamProcessor extends OutputProcessor {

    public void setOutput(OutputStream out);
}
