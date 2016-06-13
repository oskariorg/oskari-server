package fi.nls.oskari.spatineo.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.spatineo.dto.OskariMapLayerDto;
import fi.nls.oskari.spatineo.dto.SpatineoMonitoringResponseDto;
import fi.nls.oskari.util.IOHelper;
import java.io.IOException;
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
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class) 
@PrepareForTest(value = {HttpClient.class, PostMethod.class, HttpClientParams.class})
public class SpatineoMonitoringDaoTest {
    
    public static final String MONITORING_URL = "http://localhost";
    //public static final String MONITORING_URL = "https://monitor.spatineo.com/api/public/monitoringAPI?privateAccessKey=missing";
    
    String jsonOk;
    String jsonError;

    //String jsonOk = "{\"version\":\"1.0\",\"status\":\"OK\",\"configuration\":{\"group\":4},\"result\":[{\"service\":{\"id\":\"50ea942c3b3ebfcbd0e1edff\",\"serviceId\":6470,\"serviceType\" : \"WMS\",\"serviceUrl\":\"http://maps3.sgu.se:80/geoserver/berg/ows\",\"meters\":[{\"id\":\"51aee5b7dd0068709c84bf77\",\"layerName\":\"Jarvet\",\"crs\":\"EPSG:3067\",\"imageHeight\":256,\"imageWidth\":256,\"format\":\"image/png\",\"operation\":\"GetMap\",\"monitorLink\":\"https://monitor.spatineo.com/#/monitoring/metrics?service=50ea942c3b3ebfcbd0e1edff&meter=51aee5b7dd0068709c84bf77\",\"indicator\":{\"status\":\"ALERT\",\"lastChange\":\"2016041812:12:00Z\"}},{\"id\":\"50fda1633b3ebfcbd0f6a35d\",\"layerName\":\"Network.HydroNode\",\"crs\":\"EPSG:3067\",\"imageHeight\":256,\"imageWidth\":256,\"format\":\"image/png\",\"operation\":\"GetMap\",\"monitorLink\":\"https://monitor.spatineo.com/#/monitoring/metrics?service=50ea942c3b3ebfcbd0e1edff&meter=50fda1633b3ebfcbd0f6a35d\",\"indicator\":{\"status\":\"ALERT\",\"lastChange\":\"2016041812:13:08Z\"}}]}},{\"service\":{\"id\":\"52e74edbf239c6430a19fc63\",\"serviceId\":16161,\"serviceType\":\"WMS\",\"serviceUrl\":\"http://geoportalvg.de/wms/lkvg_gebaeude\", \"meters\":[{\"id\":\"52f9c1bcf239c6430a1d4ec9\",\"layerName\":\"regismv_gebaeude\",\"crs\":\"EPSG:25833\",\"imageHeight\":256,\"imageWidth\":256,\"format\":\"image/png\",\"operation\":\"GetMap\",\"link\":\"https://monitor.spatineo.com/#/monitoring/metrics?service=52e74edbf239c6430a19fc63&meter=52f9c1bcf239c6430a1d4ec9\",\"status\":{\"code\":\"ALERT\",\"lastChange\":\"2016041812:12:00Z\"}}]}}]}";
    //String jsonError = "{\"version\":\"1.0\",\"status\":\"ERROR\",\"statusMessage\":\"No such API key\"}";
    
    public SpatineoMonitoringDaoTest() throws IOException {
        this.jsonError = IOHelper.readString(getClass().getResourceAsStream("responseError.json"));
        this.jsonOk = IOHelper.readString(getClass().getResourceAsStream("responseOk.json"));
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
    public void testCheckServiceStatus() {
        MockHttpClient mockClient = new MockHttpClient(200, jsonOk);        
        final SpatineoMonitoringDao monitoringDao = new SpatineoMonitoringDao(MONITORING_URL, mockClient);
        
        List<OskariMapLayerDto> layers = new ArrayList<>();
        final SpatineoMonitoringResponseDto monitoringResponse = monitoringDao.checkServiceStatus(layers);
        
        assertEquals("1.0", monitoringResponse.version);
        assertEquals("OK", monitoringResponse.getStatus());
        assertEquals(false, monitoringResponse.isError());
    }

    @Test
    public void testCheckServiceStatusError() {
        MockHttpClient mockClient = new MockHttpClient(200, jsonError);        
        final SpatineoMonitoringDao monitoringDao = new SpatineoMonitoringDao(MONITORING_URL, mockClient);
        
        List<OskariMapLayerDto> layers = new ArrayList<>();
        final SpatineoMonitoringResponseDto monitoringResponse = monitoringDao.checkServiceStatus(layers);
        
        assertEquals("1.0", monitoringResponse.version);
        assertEquals("ERROR", monitoringResponse.getStatus());
        assertEquals(true, monitoringResponse.isError());
    }
    
    @Test
    public void testCheckJackson() throws IOException {
         SpatineoMonitoringResponseDto dto = new ObjectMapper().readValue(jsonOk, SpatineoMonitoringResponseDto.class);
         System.err.println(dto.version);
    }
}
