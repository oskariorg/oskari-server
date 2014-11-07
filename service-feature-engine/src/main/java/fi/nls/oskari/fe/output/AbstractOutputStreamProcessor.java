package fi.nls.oskari.fe.output;

import java.io.OutputStream;

public abstract class AbstractOutputStreamProcessor extends
        AbstractOutputProcessor implements OutputStreamProcessor {

    protected OutputStream outs;

    public void setOutput(OutputStream out) {
        outs = out;

    }

}
