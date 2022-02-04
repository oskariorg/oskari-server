package org.oskari.capabilities;

import fi.nls.oskari.domain.map.OskariLayer;

import java.util.Objects;

public class ServiceConnectInfo {

    private final String url;
    private final String type;
    private final String version;
    private String user;
    private String pass;

    public ServiceConnectInfo(String url, String type, String version) {
        this.url = url;
        this.type = type;
        this.version = version;
    }

    public void setCredentials(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public static ServiceConnectInfo fromLayer(OskariLayer layer) {
        ServiceConnectInfo info = new ServiceConnectInfo(layer.getUrl(), layer.getType(), layer.getVersion());
        info.setCredentials(layer.getUsername(), layer.getPassword());
        return info;
    }

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ServiceConnectInfo)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        ServiceConnectInfo s = (ServiceConnectInfo) o;
        return url.equals(s.url)
                && type.equals(s.type)
                && version.equals(s.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, type, version);
    }
}
