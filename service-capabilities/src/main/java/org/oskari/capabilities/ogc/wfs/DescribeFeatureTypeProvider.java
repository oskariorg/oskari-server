package org.oskari.capabilities.ogc.wfs;

import fi.nls.oskari.util.IOHelper;

import java.io.IOException;
import java.net.HttpURLConnection;

// separated provider to help mocking in JUnit test
public class DescribeFeatureTypeProvider {
    public String getDescribeContent(String url, String user, String pass) throws IOException {
        HttpURLConnection con = IOHelper.getConnection(url, user, pass);
        con = IOHelper.followRedirect(con, user, pass, 5);
        int sc = con.getResponseCode();
        if (sc != HttpURLConnection.HTTP_OK) {
            String msg = "Unexpected status code: " + sc  + " from: " + url;
            throw new IOException(msg);
        }

        return IOHelper.readString(con);
    }
}
