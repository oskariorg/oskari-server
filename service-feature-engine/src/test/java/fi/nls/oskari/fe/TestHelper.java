package fi.nls.oskari.fe;

import java.io.File;
import java.io.IOException;

public class TestHelper {

    public File getTempFile(String name, String suffix) throws IOException {

        return File.createTempFile(name, suffix);
    }

}
