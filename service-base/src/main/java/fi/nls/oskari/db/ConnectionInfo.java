package fi.nls.oskari.db;


import fi.nls.oskari.util.ConversionHelper;

/**
 * Created by SMAKINEN on 11.6.2015.
 */
public class ConnectionInfo {

    public String driver;
    public String url;
    public String user;
    public String pass;

    private static final int DEFAULT_PORT = 5432;

    public String getHost() {
        String[] parts = getUrlToParts();
        if(parts == null || parts.length == 0) {
            return null;
        }
        int separator = parts[0].indexOf(':');
        return parts[0].substring(0, separator);
    }

    public int getPort() {
        String[] parts = getUrlToParts();
        if(parts == null || parts.length == 0) {
            return DEFAULT_PORT;
        }
        int separator = parts[0].indexOf(':');
        return ConversionHelper.getInt(parts[0].substring(separator + 1), DEFAULT_PORT);
    }
    public String getDBName() {
        String[] parts = getUrlToParts();
        if(parts == null || parts.length < 2) {
            return null;
        }
        return parts[1];
    }

    private String[] getUrlToParts() {
        if(url == null) {
            return null;
        }
        String data = url.substring(url.indexOf("://") + 3);
        String[] parts = data.split("/");
        return parts;
    }
}
