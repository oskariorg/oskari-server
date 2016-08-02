package fi.nls.oskari.spatineo.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.spatineo.SpatineoServalUpdateService;
import fi.nls.oskari.spatineo.dto.OskariMapLayerDto;
import fi.nls.oskari.spatineo.dto.SpatineoMonitoringResponseDto;
import fi.nls.oskari.util.IOHelper;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MockHttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class) 
@PrepareForTest(value = {HttpClient.class, PostMethod.class, HttpClientParams.class})
public class SpatineoMonitoringDaoTest {

    public static final String MONITORING_URL = "http://localhost";

    String jsonOk;
    String jsonEmpty;
    String jsonError;

    public SpatineoMonitoringDaoTest() throws IOException {
        this.jsonOk = IOHelper.readString(getClass().getResourceAsStream("responseOk.json"));
        this.jsonEmpty = IOHelper.readString(getClass().getResourceAsStream("responseEmpty.json"));
        this.jsonError = IOHelper.readString(getClass().getResourceAsStream("responseError.json"));
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    @Ignore
    public void testCheckServiceStatusOk() {
        MockHttpClient mockClient = new MockHttpClient(200, jsonOk);
        final SpatineoMonitoringDao dao = new SpatineoMonitoringDao(MONITORING_URL, mockClient, false);

        List<OskariMapLayerDto> layers = new ArrayList<>();
        final SpatineoMonitoringResponseDto monitoringResponse = dao.checkServiceStatus();

        assertEquals("1.0", monitoringResponse.version);
        assertEquals("OK", monitoringResponse.getStatus());
        assertEquals(false, monitoringResponse.isError());
    }

    @Test
    @Ignore
    public void testCheckServiceStatusEmpty() {
        MockHttpClient mockClient = new MockHttpClient(200, jsonEmpty);
        final SpatineoMonitoringDao dao = new SpatineoMonitoringDao(MONITORING_URL, mockClient, false);

        List<OskariMapLayerDto> layers = new ArrayList<>();
        final SpatineoMonitoringResponseDto monitoringResponse = dao.checkServiceStatus();

        assertEquals("1.0", monitoringResponse.version);
        assertEquals("OK", monitoringResponse.getStatus());
        assertEquals(false, monitoringResponse.isError());
        assertEquals(0, monitoringResponse.result.size());
    }

    @Test
    @Ignore
    public void testCheckServiceStatusError() {
        MockHttpClient mockClient = new MockHttpClient(200, jsonError);
        final SpatineoMonitoringDao dao = new SpatineoMonitoringDao(MONITORING_URL, mockClient, false);

        List<OskariMapLayerDto> layers = new ArrayList<>();
        final SpatineoMonitoringResponseDto monitoringResponse = dao.checkServiceStatus();

        assertEquals("1.0", monitoringResponse.version);
        assertEquals("ERROR", monitoringResponse.getStatus());
        assertEquals(true, monitoringResponse.isError());
    }

    @Test
    @Ignore
    public void testCheckJackson() throws IOException {
        SpatineoMonitoringResponseDto dto = new ObjectMapper().readValue(jsonOk, SpatineoMonitoringResponseDto.class);
        System.err.println("dto = " + dto.toString());
        
    }

    @Test
    @Ignore
    public void testIntegration() throws MalformedURLException, ProtocolException, IOException {
        String url = "https://beta.spatineo-devops.com/api/public/monitoringAPI";
//        String url = "https://monitor.spatineo.com/api/public/monitoringAPI?includeAllMeters=true";

        String key = System.getProperty("spatineo.monitoring.key");
        if (key != null) {
            url += "?privateAccessKey=" + key;
            final SpatineoMonitoringDao dao = new SpatineoMonitoringDao(url, new HttpClient(), true);
            SpatineoMonitoringResponseDto dto = dao.checkServiceStatus();
            System.err.println("dto = " + dto);
        } else {
            fail("Environment key <spatineo.monitoring.key> not set");
        }
    }
    
    @Test
    @Ignore
    public void testIntegrationScheduled() throws Exception {
        SpatineoServalUpdateService.scheduledServiceCall();
    }
}
