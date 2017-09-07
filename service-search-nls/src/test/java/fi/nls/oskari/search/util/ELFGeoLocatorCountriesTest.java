package fi.nls.oskari.search.util;

import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by SMAKINEN on 7.9.2017.
 */
public class ELFGeoLocatorCountriesTest {

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
    public void testCountrySearch() throws Exception {
        PropertyUtil.addProperty("search.channel.ELFGEOLOCATOR_CHANNEL.service.url", "http://dummy.url");
        ELFGeoLocatorCountries countries = ELFGeoLocatorCountries.getInstance();
        String response = IOHelper.readString(getClass().getResourceAsStream("geolocator-country-filter-response.xml"));
        Map<String, String> map = countries.parseCountryMap(response);

        assertEquals("Should get 9 values", 9 , map.size());
        countries.setCountryMap(map);

        Set<String> countrySet = countries.getCountries();
        assertEquals("Should get 8 values", 8 , countrySet.size());

        String countryName = countries.getAdminCountry(new Locale("en"), "Norway polar - GN");
        assertEquals("Countryname should match expected", "Norway", countryName);

        String adminFilter = countries.getAdminNamesFilter("no");

        String expectedFilter = IOHelper.readString(getClass().getResourceAsStream("geolocator-admin-filter-expected.xml"));
        Diff xmlDiff = new Diff(adminFilter, expectedFilter);
        assertTrue("Should get expected admin filter " + xmlDiff, xmlDiff.similar());
    }
}