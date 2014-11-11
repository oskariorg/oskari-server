package fi.nls.oskari.search.channel;

import java.net.HttpURLConnection;

/**
 * Interface for abstracting connection creation
 */
public interface ConnectionProvider {

    public HttpURLConnection getConnection();

    public HttpURLConnection getConnection(String baseUrl);
}
