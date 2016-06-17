package fi.nls.oskari.spatineo.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spatineo.dto.OskariMapLayerDto;
import fi.nls.oskari.spatineo.dto.SpatineoMonitoringResponseDto;
import fi.nls.oskari.util.IOHelper;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.List;

/**
 * Data access to the Spatineo new geographical data monitoring service
 */
public class SpatineoMonitoringDao {

    private static final Logger log = LogFactory.getLogger(SpatineoServalDao.class);

    private String serviceUrl;
    private final HttpClient httpClient;
    private final boolean lean = false;     // set this to true in production

    private static final HttpMethodRetryHandler NO_RETRY =  new HttpMethodRetryHandler() {
        @Override
        public boolean retryMethod(HttpMethod method, IOException exception, int executionCount) {
            return false;
        }
    };

    /**
     * Constructor
     * 
     * @param client        The Apache HttpClient instance to be used - needed for testing
     * @param allMeters     Include all meters regardless whether they have Indicators or not?
     */
    public SpatineoMonitoringDao(String serviceUrl, HttpClient client, boolean allMeters) {
        this.serviceUrl = serviceUrl;
        this.httpClient = client;
        if (allMeters) this.serviceUrl += "&includeAllMeters=true";
        if (lean) this.serviceUrl += "&lean=true";
    }

    public SpatineoMonitoringResponseDto checkServiceStatus() {
        httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, NO_RETRY);

        if (null != System.getProperty("http.proxyHost") && null != System.getProperty("http.proxyPort")) {
            httpClient.getHostConfiguration().setProxy(System.getProperty("http.proxyHost"),
                    Integer.parseInt(System.getProperty("http.proxyPort")));
        }

        final PostMethod post = new PostMethod(serviceUrl);
        post.addRequestHeader("Origin", "http://www.paikkatietoikkuna.fi");
        post.addRequestHeader("X-Requested-With", getClass().getName());
        
        try {
            final int status = httpClient.executeMethod(post);
            
//            HttpURLConnection conn = IOHelper.getConnection(serviceUrl);
//            IOHelper.trustAllCerts(conn);
//            IOHelper.trustAllHosts(conn);
//
//            conn.setRequestMethod("GET");
//            conn.setDoOutput(true);
//            conn.setDoInput(true);
//            HttpURLConnection.setFollowRedirects(false);
//            conn.setUseCaches(false);
////            conn.setRequestProperty(HEADER_CONTENT_TYPE, PARAM_POST_CONTENTTYPE);
//            IOHelper.writeToConnection(conn, "");
//            String resultString = IOHelper.readString(conn);
            
            System.err.println("status = " + status);
            if (200 == status) {
                return new ObjectMapper().readValue(post.getResponseBodyAsString(), SpatineoMonitoringResponseDto.class);
//                return new ObjectMapper().readValue(resultString, SpatineoMonitoringResponseDto.class);
            } else {
                return null;
            }
        } catch (HttpException e) {
            // ignore
            log.debug(e.getMessage());
        } catch (final IOException e) {
            // ignore
            log.debug(e.getMessage());
        } finally {
            post.releaseConnection();
        }
        return null;
    }
}
