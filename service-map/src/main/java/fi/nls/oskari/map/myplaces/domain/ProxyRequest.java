package fi.nls.oskari.map.myplaces.domain;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ProxyRequest {
    
    private String url;
    private String method;
    private String userName;
    private String password;
    private String postData;
    private Map<String, String> params = new HashMap<String, String>();
    private Map<String, String> headers = new HashMap<String, String>();
    
    public void addParam(final String key, final String value) {
        params.put(key, value);
    }

    public void addHeader(final String key, final String value) {
        headers.put(key, value);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }    
    
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPostData() {
        return postData;
    }

    public void setPostData(String postData) {
        this.postData = postData;
    }

    public String getParamsAsQueryString() {

        final StringBuffer qsb = new StringBuffer();
        final Iterator<String> parmNames = params.keySet().iterator();
        while (parmNames.hasNext()) {
            final String key = parmNames.next();
            qsb.append(key);
            qsb.append("=");
            final String value = params.get(key).replace(' ', '+'); // URLEncoder
            qsb.append(value);
            if (parmNames.hasNext()) {
                qsb.append("&");
            }
        }
        return qsb.toString();
    }
    
}
