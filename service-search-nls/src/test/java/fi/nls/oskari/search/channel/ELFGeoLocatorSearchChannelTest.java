package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.search.util.ELFGeoLocatorCountries;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.FilterTransformer;
import org.geotools.filter.Filters;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.json.JSONObject;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Locale;

import static org.junit.Assert.assertTrue;

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


    @Test
    public void testCountrySearch() throws Exception {
        ELFGeoLocatorCountries countries = new ELFGeoLocatorCountries();
        String response = IOHelper.readString(getClass().getResourceAsStream("ASDIService.xml"));
        countries.parseCountryMap(response);

        String adminNames = countries.getAdminNamesFilter("no");

        String countryName = countries.getAdminCountry(new Locale("en"), "Norway polar - GN", true);
        assertTrue(countryName.equals("Norway"));
    }

    @Test
    public void getElasticQuery() {
        ELFGeoLocatorSearchChannel channel = new ELFGeoLocatorSearchChannel();
        final JSONObject expected = JSONHelper.createJSONObject("{\"query\":{\"match\":{\"name\":{\"analyzer\":\"standard\",\"query\":\"\\\"}, {\\\"break\\\": []\"}}}}");
        final JSONObject actual = JSONHelper.createJSONObject(channel.getElasticQuery("\"}, {\"break\": []"));
        assertTrue("JSON should not break", JSONHelper.isEqual(expected, actual));
    }

}
