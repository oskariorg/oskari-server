package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
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

public class ELFGeoLocatorSearchChannelTest {

    private String wildcardQueryXML;
    
    public ELFGeoLocatorSearchChannelTest() throws IOException {
        this.wildcardQueryXML = IOHelper.readString(getClass().getResourceAsStream("ELFWildcardQuery.xml"));
    }

    @BeforeClass
    public static void setUp() {
        // use relaxed comparison settings
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setIgnoreAttributeOrder(true);
    }

    @After
    public void tearDown() {
        PropertyUtil.clearProperties();
    }

    @Test
    public void testWildcardQueryCreation() throws Exception {
        System.out.println("getData");

        PropertyUtil.addProperty("search.channel.ELFGEOLOCATOR_CHANNEL.service.url", "http://dummy.url");

        ELFGeoLocatorSearchChannel channel = new ELFGeoLocatorSearchChannel();
        channel.init();

        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setSearchString("Hel*ki");

        assertTrue("Channel should detect wildcard in query", channel.hasWildcard(searchCriteria.getSearchString()));

        String xml = channel.getWildcardQuery(searchCriteria);
        Diff xmlDiff = new Diff(wildcardQueryXML, xml);
        assertTrue("Should get expected query " + xmlDiff, xmlDiff.similar());
    }

}
