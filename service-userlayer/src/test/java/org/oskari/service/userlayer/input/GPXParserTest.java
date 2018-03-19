package org.oskari.service.userlayer.input;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Ignore;
import org.junit.Test;

import fi.nls.oskari.service.ServiceException;

public class GPXParserTest {
    
    @Test
    @Ignore("Requires GDAL")
    public void testParse() throws ServiceException, URISyntaxException {
        GPXParser gpx = new GPXParser();
        File file = new File(getClass().getResource("run.gpx").toURI());
        gpx.parse(file);
    }

}
