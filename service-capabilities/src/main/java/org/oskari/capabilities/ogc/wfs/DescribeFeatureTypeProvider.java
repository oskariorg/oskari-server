package org.oskari.capabilities.ogc.wfs;

import fi.nls.oskari.util.IOHelper;

import java.io.IOException;

// separated provider to help mocking in JUnit test
public class DescribeFeatureTypeProvider {
    public String getDescribeContent(String url, String user, String pass) throws IOException {
        // TODO: follow redirects
        return IOHelper.getURL(url, user, pass);
    }
}
