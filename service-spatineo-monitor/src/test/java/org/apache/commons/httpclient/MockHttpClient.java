package org.apache.commons.httpclient;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

/**
 * 
 * This class is for testing Apache HttpClient related classes, which is
 * difficult otherwise.
 * 
 * Code adapted from:
 *
 * https://blog.newsplore.com/2010/02/09/unit-testing-with-httpclient
 *
 */
public class MockHttpClient extends HttpClient {

    private final int expectedResponseStatus;
    private final String expectedResponseBody;

    public MockHttpClient(int responseStatus, String responseBody) {
        this.expectedResponseStatus = responseStatus;
        this.expectedResponseBody = responseBody;
    }

    @Override
    public int executeMethod(HttpMethod method) throws UnsupportedEncodingException {
        try {
            ((HttpMethodBase) method).setResponseStream(new ByteArrayInputStream(expectedResponseBody.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        return expectedResponseStatus;
    }
}
