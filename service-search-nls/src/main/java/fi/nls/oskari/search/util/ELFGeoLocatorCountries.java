package fi.nls.oskari.search.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.channel.ELFGeoLocatorSearchChannel;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;

/**
 * Parses countries map for ELF search implementations.
 * Based on NLS GeoLocator search that provides a CountryFilter request
 */
public class ELFGeoLocatorCountries {
    private Logger log = LogFactory.getLogger(this.getClass());

    public static final String DEFAULT_GETCOUNTRIES_TEMPLATE = "?SERVICE=WFS&VERSION=2.0.0&REQUEST=CountryFilter";
    // 30 minutes
    public long REFRESH_INTERVAL = 30 * 60 * 1000;

    private long lastSync;
    private String serviceURL;
    private Map<String, String> countryMap = new HashMap<>();
    private static ELFGeoLocatorCountries self = null;

    public static ELFGeoLocatorCountries getInstance() {
        if(self == null) {
            self = new ELFGeoLocatorCountries();
        }
        return self;
    }

    private ELFGeoLocatorCountries() {
        serviceURL = PropertyUtil.getOptional(ELFGeoLocatorSearchChannel.PROPERTY_SERVICE_URL);
        if (serviceURL == null) {
            log.warn("ServiceURL not configured. Add property with key", ELFGeoLocatorSearchChannel.PROPERTY_SERVICE_URL);
        }
        loadCountryMap();
    }

    /**
     * The result rarely changes. So the map is cached "forever" and refreshed only
     * when a referenced country/admin value is NOT found on the map.
     */
    private synchronized void loadCountryMap () {
        // not properly configured
        if (serviceURL == null) {
            return;
        }
        // synced recently
        if(System.currentTimeMillis() < lastSync + REFRESH_INTERVAL) {
            return;
        }

        Map<String, String> updatedMap = parseCountryMap(getCountryData());
        if(updatedMap.isEmpty()) {
            log.warn("Got empty result from countries update!");
            // try again after small delay based on interval
            lastSync = System.currentTimeMillis() - Math.round(REFRESH_INTERVAL * 0.90);
        } else {
            setCountryMap(updatedMap);
            lastSync = System.currentTimeMillis();
        }
    }

    protected String getCountryData() {
        String countriesUrl = serviceURL + DEFAULT_GETCOUNTRIES_TEMPLATE;
        log.debug("Server request: " + countriesUrl);
        try {
            HttpURLConnection conn = IOHelper.getConnection(countriesUrl);
            return IOHelper.readString(conn);
        } catch (IOException e) {
            log.error("Could not load countries data:", e.getMessage());
        }
        return null;
    }

    /**
     * For testing purposes
     * @param map
     */
    protected void setCountryMap(Map<String, String> map) {
        countryMap = map;
    }

    protected Map<String, String> parseCountryMap(String response) {
        final Map<String, String> resultMap = new HashMap<>();
        if (response == null || response.isEmpty()) {
            return resultMap;
        }

        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(response.getBytes(IOHelper.CHARSET_UTF8)));

            NodeList nList = doc.getElementsByTagName("Country");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element eElement = (Element) nNode;
                NodeList elements = eElement.getElementsByTagName("administrator");
                String countryCode = eElement.getElementsByTagName("code").item(0).getTextContent();
                for(int i=0; i<elements.getLength(); i++) {
                    resultMap.put(
                            elements.item(i).getTextContent(),
                            countryCode);
                }
            }
        } catch (Exception e) {
            log.error(e, "Error parsing countries with ELFGeolocator, got exception");
        }
        return resultMap;
    }

    public Set<String> getCountries() {
        if(countryMap.isEmpty()) {
            loadCountryMap();
        }

        return new HashSet<>(countryMap.values());
    }

    public String getAdminCountry(Locale locale, String admin_name) {
        return getAdminCountry(locale, admin_name, true);
    }

    private String getAdminCountry(Locale locale, String admin_name, boolean reloadIfNotFound) {
        if (countryMap.containsKey(admin_name)) {
            String countryCode = countryMap.get(admin_name);
            Locale obj = new Locale("", countryCode);
            return obj.getDisplayCountry(locale);
        }
        if(!reloadIfNotFound) {
            // loading didn't get us the requested country so just return empty string
            return "";
        }
        // reload countries map contents and try again
        loadCountryMap();
        return getAdminCountry(locale, admin_name, false);
    }

    private List<String> getAdminName(String country_code, boolean reloadIfNotFound) {
        List<String> adminNameList = new ArrayList<>();
        for (Map.Entry<String, String> entry : countryMap.entrySet()) {
            String country = entry.getValue();
            if (country.equalsIgnoreCase(country_code)) {
                adminNameList.add(entry.getKey());
                reloadIfNotFound = false;
            }
        }
        if(!reloadIfNotFound) {
            // loading didn't get us the requested country so just return empty string
            return adminNameList;
        }
        // reload countries map contents and try again
        loadCountryMap();
        return getAdminName(country_code, false);
    }

    // FIXME: The filter MUST include the original user input as well... refactor...
    public String getAdminNamesFilter(String country) {
        List<String> adminNameList = getAdminName(country, true);
        if(adminNameList.isEmpty()) {
            throw new ServiceRuntimeException("Couldn't find admin(s) for country " + country);
        }
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        Configuration cfg = new org.geotools.filter.v2_0.FESConfiguration();
        Encoder encoder = new Encoder(cfg);
        encoder.setOmitXMLDeclaration(true);
        List<Filter> filterList = new ArrayList<>();

        for (String admin : adminNameList) {
            filterList.add(ff.equals(ff.property("iso19112:administrator/gmdsf1:CI_ResponsibleParty/gmdsf1:organizationName"), ff.literal(admin)));
        }

        Filter filter;
        if(filterList.size() > 1) {
            filter = ff.or(filterList);
        } else {
            filter =  filterList.get(0);
        }
        try {
            return encoder.encodeAsString(filter, org.geotools.filter.v2_0.FES.Filter);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Error encoding filter for country " + country, e);
        }
    }

}