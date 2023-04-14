package org.oskari.capabilities;

import java.io.Serializable;

public class RawCapabilitiesResponse implements Serializable {

    private final String url;
    private byte[] response;
    private String encoding;
    private String contentType;

    public RawCapabilitiesResponse(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }


    public String getEncoding() {
        return encoding;
    }

    public byte[] getResponse() {
        return response;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setResponse(byte[] resp, String encoding) {
        this.response = resp;
        this.encoding = encoding;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append(",url=").append(url);
        sb.append(",bytes=").append(response != null ? response.length : -1);
        sb.append('}');
        return sb.toString();
    }
}
