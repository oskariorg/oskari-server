package fi.nls.oskari.spatineo.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spatineo.dto.SpatineoMonitoringResponseDto;
import org.apache.commons.httpclient.*;

import java.io.IOException;

/**
 * Data access to the Spatineo new geographical data monitoring service
 */
public class SpatineoMonitoringDao {

    private static final Logger log = LogFactory.getLogger(SpatineoMonitoringDao.class);

    private String serviceUrl;
    private final HttpClient httpClient;
    private final HttpRequester httpRequester = new HttpRequester();
    private final boolean lean = false;     // set this to true in production

    private static final HttpMethodRetryHandler NO_RETRY = new HttpMethodRetryHandler() {
        @Override
        public boolean retryMethod(HttpMethod method, IOException exception, int executionCount) {
            return false;
        }
    };

    /**
     * Constructor
     *
     * @param client The Apache HttpClient instance to be used - needed for
     * testing
     * @param allMeters Include all meters regardless whether they have
     * Indicators or not?
     */
    public SpatineoMonitoringDao(String serviceUrl, HttpClient client, boolean allMeters) {
        this.serviceUrl = serviceUrl;
        this.httpClient = client;
        if (allMeters) {
            this.serviceUrl += "&includeAllMeters=true";
        }
        if (lean) {
            this.serviceUrl += "&lean=true";
        }
    }

    /**
     * This is called by SpatineoServalUpdateService class
     * (Scheduled in portal-ext.properties, every 5min)
     * 
     * @return Deserialized server response 
     */
    public SpatineoMonitoringResponseDto checkServiceStatus() {
        log.debug("proxyHost=" + System.getProperty("http.proxyHost") + ", proxyPort=" + System.getProperty("http.proxyPort"));
        String response = httpRequester.GET(serviceUrl);
        if (response != null) {
            System.err.println("response = " + response);
            try {
                return new ObjectMapper().readValue(response, SpatineoMonitoringResponseDto.class);
            } catch (IOException ex) {
                log.debug(ex.getMessage());
                log.debug(ex.getStackTrace());
                for (Throwable t : ex.getSuppressed()) {
                    log.debug(t.getMessage());
                }
                log.debug("Mapping Spatineo Monitoring API response to a DTO failed!");
            }
        }
        return null;
    }
}
