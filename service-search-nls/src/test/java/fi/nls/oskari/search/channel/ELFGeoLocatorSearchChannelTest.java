package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class) 
@PrepareForTest(value = {HttpClient.class, PostMethod.class, HttpClientParams.class})
public class ELFGeoLocatorSearchChannelTest {
    
    private String resultXML;
    
    public ELFGeoLocatorSearchChannelTest() throws IOException {
        this.resultXML = IOHelper.readString(getClass().getResourceAsStream("geolocator-wildcard-result.json"));
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
        PropertyUtil.clearProperties();
    }

    @Test
    public void testGetDataWithWildcard() throws Exception {
        System.out.println("getData");
        
        PropertyUtil.addProperty("search.channel.ELFGEOLOCATOR_CHANNEL.service.url", "http://54.247.101.37/elf/GeolocatorService");
        
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setSearchString("Hel*ki");

        InputStream is = new ByteArrayInputStream(resultXML.getBytes());

        HttpURLConnection conn = Mockito.mock(HttpURLConnection.class);
        Mockito.when(conn.getInputStream()).thenReturn(is);
        
        ELFGeoLocatorSearchChannel channel = new ELFGeoLocatorSearchChannel(conn);
        channel.init();
        
        String result = channel.getData(searchCriteria);
        assertEquals(resultXML, result);
    }

    @Test
    public void getElasticQuery() {
        ELFGeoLocatorSearchChannel channel = new ELFGeoLocatorSearchChannel();
        final JSONObject expected = JSONHelper.createJSONObject("{\"query\":{\"match\":{\"name\":{\"analyzer\":\"standard\",\"query\":\"\\\"}, {\\\"break\\\": []\"}}}}");
        final JSONObject actual = JSONHelper.createJSONObject(channel.getElasticQuery("\"}, {\"break\": []"));
        assertTrue("JSON should not break", JSONHelper.isEqual(expected, actual));
    }

}
