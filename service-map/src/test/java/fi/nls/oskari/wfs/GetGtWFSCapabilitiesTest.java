package fi.nls.oskari.wfs;

import org.junit.Test;

import fi.nls.oskari.service.ServiceException;

public class GetGtWFSCapabilitiesTest {
    
    @Test
    public void test() throws ServiceException {
        // String type = OskariLayer.TYPE_WFS;
        String url = "https://geo.stat.fi/geoserver/vaestoalue/wfs?";
        String version = "1.1.0";
        String user = null;
        String pw = null;
        String currentCrs = "EPSG:3067";
        System.out.println(GetGtWFSCapabilities.getWFSCapabilities(url, version, user, pw, currentCrs).toString());
    }

}
